package top.criwits.learnbot.json

import com.fasterxml.jackson.annotation.{JsonIgnoreProperties, JsonProperty}
import top.criwits.learnbot.json.BatchResponse.Data

@JsonIgnoreProperties(ignoreUnknown = true)
case class BatchResponse(
    @JsonProperty("code") code: Int,
    @JsonProperty("msg") msg: String,
    @JsonProperty("data") data: Data
)

object BatchResponse {
    @JsonIgnoreProperties(ignoreUnknown = true)
    case class Data(
        @JsonProperty("message_id") messageID: String
    )
}
