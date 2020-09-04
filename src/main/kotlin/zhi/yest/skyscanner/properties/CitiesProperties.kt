package zhi.yest.skyscanner.properties

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding

@ConfigurationProperties(prefix = "cities")
@ConstructorBinding
data class CitiesProperties(val from: List<String>, val to: List<String>)