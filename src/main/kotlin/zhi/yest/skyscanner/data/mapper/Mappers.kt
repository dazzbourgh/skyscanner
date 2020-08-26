package zhi.yest.skyscanner.data.mapper

import zhi.yest.skyscanner.data.Quote
import zhi.yest.skyscanner.data.QuoteResponse
import zhi.yest.skyscanner.data.RouteResponse

fun QuoteResponse.toQuotes(): List<Quote> {
    val placesMap = places.map { Pair(it.placeId, it.iataCode) }.toMap()
    val carriersMap = carriers.map { Pair(it.carrierId, it.name) }.toMap()
    return quotes.map { quoteDto ->
        Quote(
                quoteDto.outboundLeg.originId.let { placesMap[it] }!!,
                quoteDto.outboundLeg.destinationId.let { placesMap[it] }!!,
                quoteDto.minPrice,
                0,
                quoteDto.outboundLeg.departureDate,
                quoteDto.outboundLeg.carrierIds.map { carriersMap[it] }.joinToString())
    }
}

fun RouteResponse.toQuotes(): List<Quote> {
    val placesMap = places.map { Pair(it.placeId, it.iataCode) }.toMap()
    val carriersMap = carriers.map { Pair(it.carrierId, it.name) }.toMap()
    return quotes.map { quoteDto ->
        Quote(
                quoteDto.outboundLeg.originId.let { placesMap[it] }!!,
                quoteDto.outboundLeg.destinationId.let { placesMap[it] }!!,
                quoteDto.minPrice,
                0,
                quoteDto.outboundLeg.departureDate,
                quoteDto.outboundLeg.carrierIds.map { carriersMap[it] }.joinToString())
    }
}