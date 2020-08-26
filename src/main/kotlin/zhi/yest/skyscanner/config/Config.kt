package zhi.yest.skyscanner.config

import com.google.cloud.bigquery.BigQueryOptions
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class Config {
    @Bean
    fun bigQuery() = BigQueryOptions.getDefaultInstance().service
}