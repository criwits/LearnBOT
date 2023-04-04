package top.criwits.learnbot.json

import com.fasterxml.jackson.annotation.JsonProperty

case class Sentence(
    @JsonProperty("id") id: Int,
    @JsonProperty("uuid") uuid: String,
    @JsonProperty("hitokoto") hitokoto: String,
    @JsonProperty("type") `type`: String,
    @JsonProperty("from") from: String,
    @JsonProperty("from_who") fromWho: String,
    @JsonProperty("creator") creator: String,
    @JsonProperty("creator_uid") creatorUID: Int,
    @JsonProperty("reviewer") reviewer: Int,
    @JsonProperty("commit_from") commitFrom: String,
    @JsonProperty("created_at") createdAt: String,
    @JsonProperty("length") length: Int
)
