package top.criwits.learnbot.json

import com.fasterxml.jackson.annotation.JsonProperty

case class Content(
    @JsonProperty("text") text: String
)
