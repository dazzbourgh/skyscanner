package zhi.yest.skyscanner.service

import com.google.cloud.pubsub.v1.Publisher
import com.google.protobuf.ByteString
import com.google.pubsub.v1.PubsubMessage
import java.util.concurrent.TimeUnit

fun completionNotification(init: CompletionNotification.() -> MessageId): MessageId {
    val completionNotification = CompletionNotification()
    return completionNotification.init()
}

class CompletionNotification {
    private lateinit var publisher: Publisher
    private lateinit var message: String

    fun publisher(init: () -> Publisher) {
        publisher = init()
    }

    fun message(init: () -> String) {
        message = init()
    }

    fun notifyCompletion(): MessageId {
        try {
            val data: ByteString = ByteString.copyFromUtf8(message)
            val pubsubMessage = PubsubMessage.newBuilder().setData(data).build()
            val messageIdFuture = publisher.publish(pubsubMessage)
            return MessageId(messageIdFuture.get())
        } finally {
            publisher.shutdown()
            publisher.awaitTermination(1, TimeUnit.MINUTES)
        }
    }
}

data class MessageId(val messageId: String)
