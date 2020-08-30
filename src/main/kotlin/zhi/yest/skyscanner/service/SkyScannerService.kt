package zhi.yest.skyscanner.service

import kotlinx.coroutines.reactive.awaitFirstOrNull
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono
import zhi.yest.skyscanner.data.*

@Service
class SkyScannerService(private val webClient: WebClient,
                        @Value("\${sm://X-RapidAPI-Key}") private val apiKey: String) {

    suspend fun requestRoute(routeRequest: RouteRequest): RouteResponse? = webClient.get()
            .uri {
                val uri = it.host("skyscanner-skyscanner-flight-search-v1.p.rapidapi.com")
                        .scheme("https")
                        .path("apiservices/browseroutes/v1.0/${routeRequest.country}/${routeRequest.currency}/${routeRequest.locale}/${routeRequest.originPlace}/${routeRequest.destinationPlace}/${routeRequest.outboundPartialDate}")
                if (routeRequest.inboundPartialDate != null)
                    uri.queryParam("inboundpartialdate", routeRequest.inboundPartialDate)
                uri.build()
            }
            .request(RouteResponse::class.java)

    suspend fun requestQuote(quoteRequest: QuoteRequest): QuoteResponse? = webClient.get()
            .uri {
                val uri = it.host("skyscanner-skyscanner-flight-search-v1.p.rapidapi.com")
                        .scheme("https")
                        .path("apiservices/browsequotes/v1.0/${quoteRequest.country}/${quoteRequest.currency}/${quoteRequest.locale}/${quoteRequest.originPlace}/${quoteRequest.destinationPlace}/${quoteRequest.outboundPartialDate}")
                if (quoteRequest.inboundPartialDate != null)
                    uri.queryParam("inboundpartialdate", quoteRequest.inboundPartialDate)
                uri.build()
            }
            .request(QuoteResponse::class.java)

    suspend fun requestPlace(placeRequest: PlaceRequest): PlaceResponse? = webClient.get()
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
                    .onErrorResume { Mono.empty() }
                    .awaitFirstOrNull()
}