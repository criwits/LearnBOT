package top.criwits.learnbot.json

import com.fasterxml.jackson.annotation.JsonProperty

case class MsgHeader(
    @JsonProperty("event_id") eventID: String,
    @JsonProperty("event_type") eventType: String,
    @JsonProperty("create_time") createTime: String,
    @JsonProperty("token") token: String,
    @JsonProperty("app_id") appID: String,
    @JsonProperty("tenant_key") tenantKey: String
)
