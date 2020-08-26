package zhi.yest.skyscanner

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.reactive.awaitSingle
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication
import org.springframework.context.annotation.PropertySource
import org.springframework.web.reactive.function.client.WebClient
import zhi.yest.skyscanner.data.*
import zhi.yest.skyscanner.data.mapper.toQuotes
import zhi.yest.skyscanner.properties.BigQueryProperties
import zhi.yest.skyscanner.service.BigQueryService
import java.time.LocalDate


@SpringBootApplication
@PropertySource("classpath:cities.yml")
@EnableConfigurationProperties(BigQueryProperties::class)
class SkyscannerApplication(@Value("\${cities}") private val cities: List<String>,
                            @Value("\${sm://X-RapidAPI-Key}") private val apiKey: String,
                            @Value("\${delay:1500}") private val delay: Long,
                            private val bigQueryService: BigQueryService) : CommandLineRunner {
    private val webClient = this.webClient()

    @FlowPreview
    override fun run(vararg args: String?) = runBlocking {
        cities.asFlow()
                .take(1)
                .map { PlaceRequest(it, "US", "USD", "en-US") }
                .map { requestPlace(it) }
                .flatMapMerge { it.places.asFlow() }
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
                    async { requestRoute(it) }.also { req -> req.start() }
                }
                .map { it.await() }
                .filter { it.quotes.isNotEmpty() }
                .map { it.toQuotes() }
                .flowOn(Dispatchers.Default)
                .onEach { bigQueryService.save(it) }
                .flowOn(Dispatchers.IO)
                .catch { it.printStackTrace() }
                .collect()
    }

    private suspend fun requestRoute(routeRequest: RouteRequest) = webClient.get()
            .uri {
                val uri = it.host("skyscanner-skyscanner-flight-search-v1.p.rapidapi.com")
                        .scheme("https")
                        .path("apiservices/browseroutes/v1.0/${routeRequest.country}/${routeRequest.currency}/${routeRequest.locale}/${routeRequest.originPlace}/${routeRequest.destinationPlace}/${routeRequest.outboundPartialDate}")
                if (routeRequest.inboundPartialDate != null)
                    uri.queryParam("inboundpartialdate", routeRequest.inboundPartialDate)
                uri.build()
            }
            .request(RouteResponse::class.java)

    private suspend fun requestQuote(quoteRequest: QuoteRequest) = webClient.get()
            .uri {
                val uri = it.host("skyscanner-skyscanner-flight-search-v1.p.rapidapi.com")
                        .scheme("https")
                        .path("apiservices/browsequotes/v1.0/${quoteRequest.country}/${quoteRequest.currency}/${quoteRequest.locale}/${quoteRequest.originPlace}/${quoteRequest.destinationPlace}/${quoteRequest.outboundPartialDate}")
                if (quoteRequest.inboundPartialDate != null)
                    uri.queryParam("inboundpartialdate", quoteRequest.inboundPartialDate)
                uri.build()
            }
            .request(QuoteResponse::class.java)

    private suspend fun requestPlace(placeRequest: PlaceRequest) = webClient.get()
            .uri {
                it.host("skyscanner-skyscanner-flight-search-v1.p.rapidapi.com")
                        .scheme("https")
                        .path("apiservices/autosuggest/v1.0/${placeRequest.country}/${placeRequest.currency}/${placeRequest.locale}/")
                        .queryParam("query", placeRequest.query)
                        .build()
            }
            .request(PlaceResponse::class.java)

    private suspend fun <T> WebClient.RequestHeadersSpec<*>.request(clazz: Class<T>) =
            header("x-rapidapi-host", "skyscanner-skyscanner-flight-search-v1.p.rapidapi.com")
                    .header("x-rapidapi-key", apiKey)
                    .header("useQueryString", "true")
                    .retrieve()
                    .bodyToMono(clazz)
                    .awaitSingle()

    fun webClient() = WebClient.create()
}

fun main(args: Array<String>) {
    runApplication<SkyscannerApplication>(*args)
}
