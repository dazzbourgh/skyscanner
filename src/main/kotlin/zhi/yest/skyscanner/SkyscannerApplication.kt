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
import zhi.yest.skyscanner.data.PlaceRequest
import zhi.yest.skyscanner.data.RouteRequest
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
                            private val bigQueryService: BigQueryService,
                            private val skyScannerService: SkyScannerService,
                            private val pubSubProperties: PubSubProperties) : CommandLineRunner {
    private val log = LoggerFactory.getLogger(javaClass)

    @FlowPreview
    override fun run(vararg args: String?) = runBlocking<Unit> {
        cities.asFlow()
                .map { PlaceRequest(it, "US", "USD", "en-US") }
                .map { skyScannerService.requestPlace(it) }
                .flatMapMerge { it?.places?.asFlow() ?: flowOf() }
                .flatMapMerge {
                    generateSequence(LocalDate.now().plusDays(14)) { it.plusDays(1) }
                            .flatMap { date ->
                                sequenceOf(
                                        RouteRequest("US", "USD", "en-US", "LAX-sky", it.placeId, date),
                                        RouteRequest("RU", "USD", "en-US", "LED-sky", it.placeId, date))
                            }
                            .asFlow()
                }
                .map {
                    delay(delay)
                    async { skyScannerService.requestRoute(it) }.also { req -> req.start() }
                }
                .map { it.await() }
                .filter { it?.quotes?.isNotEmpty() ?: false }
                .map { it!!.toQuotes() }
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

fun main(args: Array<String>) {
    runApplication<SkyscannerApplication>(*args)
}
