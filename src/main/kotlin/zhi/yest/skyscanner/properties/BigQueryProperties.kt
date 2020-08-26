package zhi.yest.skyscanner.properties

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding

@ConfigurationProperties(prefix = "bigquery")
@ConstructorBinding
data class BigQueryProperties(val tableId: String, val datasetId: String)