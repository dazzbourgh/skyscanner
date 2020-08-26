package zhi.yest.skyscanner.data

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonProperty
import java.time.LocalDate

data class PlaceRequest(val query: String,
                        val country: String,
                        val currency: String,
                        val locale: String)

data class QuoteRequest(val country: String,
                        val currency: String,
                        val locale: String,
                        @JsonProperty("originplace")
                        val originPlace: String,
                        @JsonProperty("destinationplace")
                        val destinationPlace: String,
                        @JsonProperty("outboundpartialdate")
                        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
                        val outboundPartialDate: LocalDate,
                        @JsonProperty("inboundpartialdate")
                        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
                        val inboundPartialDate: LocalDate? = null)

data class RouteRequest(val country: String,
                        val currency: String,
                        val locale: String,
                        @JsonProperty("originplace")
                        val originPlace: String,
                        @JsonProperty("destinationplace")
                        val destinationPlace: String,
                        @JsonProperty("outboundpartialdate")
                        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
                        val outboundPartialDate: LocalDate,
                        @JsonProperty("inboundpartialdate")
                        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
                        val inboundPartialDate: LocalDate? = null)