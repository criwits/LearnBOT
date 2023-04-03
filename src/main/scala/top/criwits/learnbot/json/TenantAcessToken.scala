package top.criwits.learnbot.json

import com.fasterxml.jackson.annotation.JsonProperty

case class TenantAcessToken(
    @JsonProperty("code") code: Int,
    @JsonProperty("msg") msg: String,
    @JsonProperty("tenant_access_token") tenantAccessToken: String,
    @JsonProperty("expire") expire: Int
)
