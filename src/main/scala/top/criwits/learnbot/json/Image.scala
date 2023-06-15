package top.criwits.learnbot.json

import com.fasterxml.jackson.annotation.JsonProperty
import top.criwits.learnbot.json.Image.Data

case class Image(
    @JsonProperty("code") code: Int,
    @JsonProperty("data") data: Data,
    @JsonProperty("msg") msg: String
)

object Image {
  case class Data(
      @JsonProperty("image_key") imageKey: String
  )
}
