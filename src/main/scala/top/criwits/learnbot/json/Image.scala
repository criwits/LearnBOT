package top.criwits.learnbot.json

import com.fasterxml.jackson.annotation.JsonProperty

case class Image(
    @JsonProperty("code") code: Int,
    @JsonProperty("data") data: ImageData,
    @JsonProperty("msg") msg: String
)

case class ImageData(
    @JsonProperty("image_key") imageKey: String
)
