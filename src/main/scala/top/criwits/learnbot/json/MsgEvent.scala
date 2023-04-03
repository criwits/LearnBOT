package top.criwits.learnbot.json

import com.fasterxml.jackson.annotation.JsonProperty
import top.criwits.learnbot.json.MsgEvent.{Message, Sender}

case class MsgEvent(
    @JsonProperty("sender") sender: Sender,
    @JsonProperty("message") message: Message
)

object MsgEvent {
  case class Sender(
      @JsonProperty("sender_id") senderID: UserID,
      @JsonProperty("sender_type") senderType: String,
      @JsonProperty("tenant_key") tenantKey: String
  )

  case class UserID(
      @JsonProperty("union_id") unionID: String,
      @JsonProperty("open_id") openID: String,
      @JsonProperty("user_id") userID: String
  )

  case class Mention(
      @JsonProperty("key") key: String,
      @JsonProperty("id") id: UserID,
      @JsonProperty("name") name: String,
      @JsonProperty("tenant_key") tenantKey: String
  )

  case class Message(
      @JsonProperty("message_id") messageID: String,
      @JsonProperty("root_id") rootID: String,
      @JsonProperty("parent_id") parentID: String,
      @JsonProperty("create_time") createTime: String,
      @JsonProperty("chat_id") chatID: String,
      @JsonProperty("chat_type") chatType: String,
      @JsonProperty("message_type") messageType: String,
      @JsonProperty("content") content: String,
      @JsonProperty("mentions") mentions: Seq[Mention]
  )
}
