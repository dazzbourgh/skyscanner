package zhi.yest.skyscanner.service

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.google.cloud.bigquery.BigQuery
import com.google.cloud.bigquery.InsertAllRequest
import com.google.cloud.bigquery.TableId
import org.springframework.stereotype.Service
import zhi.yest.skyscanner.data.Quote
import zhi.yest.skyscanner.properties.BigQueryProperties

@Service
class BigQueryService(private val bigQuery: BigQuery, private val bigQueryProperties: BigQueryProperties) {
    fun save(quotes: List<Quote>) {
        val requestBuilder = InsertAllRequest.newBuilder(TableId.of(bigQueryProperties.datasetId, bigQueryProperties.tableId))
        quotes.map { it.toFieldMap() }.forEach { requestBuilder.addRow(it) }
        bigQuery.insertAll(requestBuilder.build()).takeIf { it.hasErrors() }?.also { println(it) }
    }
}

fun Quote.toFieldMap(): Map<String, Any> {
    val mapper = ObjectMapper()
    return mapper.writeValueAsString(this).let { mapper.readValue(it, object : TypeReference<HashMap<String, Any>>() {}) }
            .also { it["departure"] = this.departure.toString() }
}
