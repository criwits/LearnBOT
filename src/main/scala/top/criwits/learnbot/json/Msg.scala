package top.criwits.learnbot.json

import com.fasterxml.jackson.annotation.JsonProperty

case class Msg(
    // Event registration: https://open.feishu.cn/document/ukTMukTMukTM/uYDNxYjL2QTM24iN0EjN/event-subscription-configure-/request-url-configuration-case
    @JsonProperty("challenge") challenge: String,
    @JsonProperty("token") token: String,
    @JsonProperty("type") `type`: String,
    // Message received: https://open.feishu.cn/document/uAjLw4CM/ukTMukTMukTM/reference/im-v1/message/events/receive
    @JsonProperty("schema") schema: String,
    @JsonProperty("header") header: MsgHeader,
    @JsonProperty("event") event: MsgEvent
)
