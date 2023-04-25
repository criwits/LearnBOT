package top.criwits.learnbot.json

import com.fasterxml.jackson.annotation.{JsonIgnoreProperties, JsonProperty}

@JsonIgnoreProperties(ignoreUnknown = true)
case class Content(
    @JsonProperty("text") text: String,
    @JsonProperty("file_key") fileKey: String,
    @JsonProperty("file_name") fileName: String
)
