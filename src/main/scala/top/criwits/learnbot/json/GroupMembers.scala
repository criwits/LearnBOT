package top.criwits.learnbot.json

import com.fasterxml.jackson.annotation.JsonProperty
import top.criwits.learnbot.json.GroupMembers.Data

case class GroupMembers(
    @JsonProperty("code") code: Int,
    @JsonProperty("data") data: Data,
    @JsonProperty("msg") msg: String
)

object GroupMembers {
  case class Data(
      @JsonProperty("has_more") hasMore: Boolean,
      @JsonProperty("items") items: Seq[Member],
      @JsonProperty("member_total") memberTotal: Int,
      @JsonProperty("page_token") pageToken: String
  )

  case class Member(
      @JsonProperty("member_id") memberID: String,
      @JsonProperty("member_id_type") memberIDType: String,
      @JsonProperty("name") name: String,
      @JsonProperty("tenant_key") tenantKey: String
  )
}
