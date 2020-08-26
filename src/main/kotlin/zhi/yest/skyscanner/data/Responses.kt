package zhi.yest.skyscanner.data

import com.fasterxml.jackson.annotation.JsonProperty

data class PlaceResponse(@JsonProperty("Places") val places: List<PlaceDto>)

data class QuoteResponse(@JsonProperty("Quotes") val quotes: List<QuoteDto>,
                         @JsonProperty("Places") val places: List<PlaceQuoteDto>,
                         @JsonProperty("Carriers") val carriers: List<CarrierDto>)

data class RouteResponse(@JsonProperty("Routes") val routes: List<RouteDto>,
                         @JsonProperty("Quotes") val quotes: List<QuoteDto>,
                         @JsonProperty("Places") val places: List<PlaceQuoteDto>,
                         @JsonProperty("Carriers") val carriers: List<CarrierDto>)