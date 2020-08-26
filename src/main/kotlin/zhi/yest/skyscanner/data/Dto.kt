package zhi.yest.skyscanner.data

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonProperty
import java.time.LocalDateTime

data class PlaceDto(@JsonProperty("PlaceId") val placeId: String,
                    @JsonProperty("PlaceName") val placeName: String,
                    @JsonProperty("CountryId") val countryId: String,
                    @JsonProperty("RegionId") val regionId: String,
                    @JsonProperty("CityId") val cityId: String,
                    @JsonProperty("CountryName") val countryName: String)

data class PlaceQuoteDto(@JsonProperty("PlaceId") val placeId: Int,
                         @JsonProperty("IataCode") val iataCode: String,
                         @JsonProperty("Name") val name: String,
                         @JsonProperty("Type") val type: String,
                         @JsonProperty("SkyscannerCode") val skyScannerCode: String,
                         @JsonProperty("CityName") val cityName: String,
                         @JsonProperty("CityId") val cityId: String,
                         @JsonProperty("CountryName") val countryName: String)

data class QuoteDto(@JsonProperty("QuoteId") val quoteId: Int,
                    @JsonProperty("MinPrice") val minPrice: Int,
                    @JsonProperty("Direct") val direct: Boolean,
                    @JsonProperty("OutboundLeg") val outboundLeg: LegDto,
                    @JsonProperty("InboundLeg") val inboundLeg: LegDto?,
                    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
                    @JsonProperty("QuoteDateTime") val quoteDateTime: LocalDateTime)

data class LegDto(@JsonProperty("CarrierIds") val carrierIds: List<Int>,
                  @JsonProperty("OriginId") val originId: Int,
                  @JsonProperty("DestinationId") val destinationId: Int,
                  @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
                  @JsonProperty("DepartureDate") val departureDate: LocalDateTime)

data class CarrierDto(@JsonProperty("CarrierId") val carrierId: Int,
                      @JsonProperty("Name") val name: String)

data class RouteDto(@JsonProperty("OriginId") val originId: Int,
                    @JsonProperty("DestinationId") val destinationId: Int,
                    @JsonProperty("QuoteIds") val quoteIds: List<Int>,
                    @JsonProperty("Price") val price: Int,
                    @JsonProperty("QuoteDateTime") val quoteDateTime: LocalDateTime)