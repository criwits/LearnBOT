package top.criwits.learnbot.json

import com.fasterxml.jackson.annotation.{JsonIgnoreProperties, JsonProperty}

@JsonIgnoreProperties(ignoreUnknown = true)
case class Content(
    @JsonProperty("text") text: String
)
