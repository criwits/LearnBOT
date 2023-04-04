package top.criwits.learnbot.json

import com.fasterxml.jackson.annotation.JsonProperty
import top.criwits.learnbot.json.BatchStatus.Data

case class BatchStatus(
    @JsonProperty("code") code: Int,
    @JsonProperty("data") data: Data,
    @JsonProperty("msg") msg: String
)
object BatchStatus {
  case class Data(
      @JsonProperty("read_user") readUser: ReadUser
  )

  case class ReadUser(
      @JsonProperty("read_count") readCount: Int,
      @JsonProperty("total_count") totalCount: Int
  )
}
