package top.criwits.learnbot.json

import com.fasterxml.jackson.annotation.{JsonIgnoreProperties, JsonProperty}

@JsonIgnoreProperties(ignoreUnknown = true)
case class BatchResponse(
    @JsonProperty("code") code: Int,
    @JsonProperty("msg") msg: String
)
