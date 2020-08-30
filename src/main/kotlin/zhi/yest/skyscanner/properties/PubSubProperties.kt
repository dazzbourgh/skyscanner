package zhi.yest.skyscanner.properties

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding

@ConfigurationProperties(prefix = "pubsub")
@ConstructorBinding
class PubSubProperties(val topic: String, val message: String)