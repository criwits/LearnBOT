package top.criwits.learnbot.json

import com.fasterxml.jackson.annotation.JsonProperty
import top.criwits.learnbot.json.GroupInfo.Data

case class GroupInfo(code: Int, msg: String, data: Data)

object GroupInfo {
  case class Data(
      @JsonProperty("items") items: Seq[Info],
      @JsonProperty("page_token") pageToken: String,
      @JsonProperty("has_more") hasMore: Boolean
  )
  case class Info(
      @JsonProperty("chat_id") chatID: String,
      @JsonProperty("avatar") avatar: String,
      @JsonProperty("name") name: String,
      @JsonProperty("description") description: String,
      @JsonProperty("owner_id") ownerID: String,
      @JsonProperty("owner_id_type") ownerIDType: String,
      @JsonProperty("external") external: Boolean,
      @JsonProperty("tenant_key") tenantKey: String
  )
}
