package zhi.yest.skyscanner.data

import com.fasterxml.jackson.annotation.JsonFormat
import java.time.LocalDateTime

data class Quote(val from: String,
                 val to: String,
                 val price: Int,
                 val duration: Int,
                 @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
                 val departure: LocalDateTime,
                 val carrier: String)