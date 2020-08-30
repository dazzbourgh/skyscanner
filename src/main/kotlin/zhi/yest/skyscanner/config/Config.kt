package zhi.yest.skyscanner.config

import com.google.cloud.bigquery.BigQueryOptions
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.function.client.WebClient

@Configuration
class Config {
    @Bean
    fun bigQuery() = BigQueryOptions.getDefaultInstance().service

    @Bean
    fun webClient() = WebClient.create()
}