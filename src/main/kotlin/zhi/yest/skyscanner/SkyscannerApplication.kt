package zhi.yest.skyscanner

import com.google.cloud.pubsub.v1.Publisher
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication
import org.springframework.context.annotation.PropertySource
import zhi.yest.skyscanner.data.*
import zhi.yest.skyscanner.data.mapper.toQuotes
import zhi.yest.skyscanner.properties.BigQueryProperties
import zhi.yest.skyscanner.properties.PubSubProperties
import zhi.yest.skyscanner.service.BigQueryService
import zhi.yest.skyscanner.service.SkyScannerService
import zhi.yest.skyscanner.service.completionNotification
import java.time.LocalDate

@SpringBootApplication
@PropertySource("classpath:cities.yml")
@EnableConfigurationProperties(BigQueryProperties::class, PubSubProperties::class)
class SkyscannerApplication(@Value("\${cities}") private val cities: List<String>,
                            @Value("\${delay:1500}") private val delay: Long,
                            @Value("\${days:60}") private val days: Int,
                            private val bigQueryService: BigQueryService,
                            private val skyScannerService: SkyScannerService,
                            private val pubSubProperties: PubSubProperties) : CommandLineRunner {
    private val log = LoggerFactory.getLogger(javaClass)

    @FlowPreview
    override fun run(vararg args: String?) = runBlocking<Unit> {
        cities.asFlow()
                .map { PlaceRequest(it, "US", "USD", "en-US") }
                .transform(toPlaces(skyScannerService))
                .flatMapMerge { it.places.asFlow() }
                .flatMapMerge { placeDto ->
                    generateSequence(LocalDate.now().plusDays(14)) { it.plusDays(1) }
                            .flatMap { date ->
                                sequenceOf(
                                        RouteRequest("US", "USD", "en-US", "LAX-sky", placeDto.placeId, date),
                                        RouteRequest("RU", "USD", "en-US", "LED-sky", placeDto.placeId, date))
                            }
                            .take(days)
                            .asFlow()
                }
                .map {
                    delay(delay)
                    async { skyScannerService.requestRoute(it) }.also { req -> req.start() }
                }
                .transform(toQuotes)
                .flowOn(Dispatchers.Default)
                .onEach {
                    log.info("Saving ${it.size} quotes")
                    bigQueryService.save(it)
                }
                .flowOn(Dispatchers.IO)
                .catch { it.printStackTrace() }
                .collect()
        completionNotification {
            publisher { Publisher.newBuilder(pubSubProperties.topic).build() }
            message { pubSubProperties.message }
            notifyCompletion()
        }
    }
}

val toPlaces: (SkyScannerService) -> suspend FlowCollector<PlaceResponse>.(PlaceRequest) -> Unit =
        { skyScannerService ->
            { placeRequest ->
                skyScannerService.requestPlace(placeRequest).takeIf { it != null }.also { emit(it as PlaceResponse) }
            }
        }

val toQuotes: suspend FlowCollector<List<Quote>>.(Deferred<RouteResponse?>) -> Unit = {
    it.await()?.toQuotes().also { list -> if (list?.isNotEmpty() == true) emit(list) }
}

fun main(args: Array<String>) {
    runApplication<SkyscannerApplication>(*args)
}
