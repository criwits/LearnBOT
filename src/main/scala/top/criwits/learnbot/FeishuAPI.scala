package top.criwits.learnbot

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.typesafe.scalalogging.Logger
import org.apache.http.client.methods.{HttpGet, HttpPost}
import org.apache.http.entity.StringEntity
import org.apache.http.impl.client.HttpClientBuilder
import top.criwits.learnbot.json.{BatchResponse, BatchStatus, GroupInfo, GroupMembers, TenantAcessToken}

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

object FeishuAPI {
  var myTenantAcessToken: Option[(Long, TenantAcessToken)] = None
  // (timestamp, list[id, name], sentence)
  var previousLoadedList: Option[(Long, Seq[(String, String)], String)] = None
  val LOG = Logger("Handler")

  def handle(admin: String, msg: String): Unit = {
    // Check access token status
    if (
      myTenantAcessToken.isEmpty || (myTenantAcessToken.get._1 + myTenantAcessToken.get._2.expire - 1200) < System
        .currentTimeMillis() / 1000
    ) {
      LOG.info("Need to update key")
      if (!updateTenantToken())
        return
    }

    // Process message
    msg.trim match {
      case "Y" | "y" =>
        // Load list
        if (
          previousLoadedList.isEmpty || (previousLoadedList.get._1 + 300) < (System
            .currentTimeMillis() / 1000)
        ) {
          LOG.info("User replied 'Y'. However, the list is too old.")
          sendSingleMessage("上次请求已过期。请重新上传发送名单。", admin)
          return
        }
        val r = {
          sendGroupMessage(previousLoadedList.get._3, previousLoadedList.get._2.map(_._1))
        }
        if (r.code != 0) {
          LOG.error(
            s"Failed to send message. Code: ${r.code}, Message: ${r.msg}"
          )
          sendSingleMessage(s"发送消息失败了。服务器返回的原因是：${r.msg}", admin)
          return
        }
        previousLoadedList = None
        sendSingleMessage("消息发送成功，辛苦啦！10 分钟后我会收集消息的接收情况。", admin)
        val runnable: Runnable = () => {
          val msgID = r.data.messageID
          Thread.sleep(600000)

          val status = checkMessageStatus(msgID)
          if (status.code != 0) {
            sendSingleMessage("未能查询到消息的接收情况。服务器返回的消息是: " + status.msg, admin)
          } else {
            sendSingleMessage(
              s"消息状态如下：已读人数：${status.data.readUser.readCount}，总发送成功人数：${status.data.readUser.totalCount}。",
              admin
            )
          }
        }
        new Thread(runnable, s"${r.data.messageID}-status").start()

      case _ =>
        previousLoadedList = None
        // Split names
        val names = msg.trim.split("\\s")
        val all = getAllClassmates
        if (all.isEmpty) {
          sendSingleMessage("获取同学列表失败，这应该是故障。", admin)
          return
        }
        val sendList = all.filter(x => names.contains(x._2))
        if (sendList.isEmpty) {
          sendSingleMessage("没有找到这些同学。", admin)
          return
        }

        // Generate sentence
        val chosenSentence = SentenceGenerator.randomYouthStudySentence
        val displaySentence = chosenSentence._2
        val realSentence = {if (chosenSentence._1) displaySentence.replace("\\\\", "\\") else displaySentence} + "  (" + DateTimeFormatter.ISO_LOCAL_DATE.format(LocalDateTime.now()) + ")"
        previousLoadedList = Some((System.currentTimeMillis() / 1000, sendList, realSentence))
        sendSingleMessage(
          s"即将向如下 ${sendList.length} 个同学发送提醒。在 5 分钟内回复字母 Y 继续。\\\\n${sendList.map(_._2).mkString(", ")}。\\\\n本次使用的消息为：${displaySentence}",
          admin
        )
    }

  }

  def updateTenantToken(): Boolean = {
    val URL =
      "https://open.feishu.cn/open-apis/auth/v3/tenant_access_token/internal"
    val body = s"""{
                  |  "app_id": "${Config.appID}",
                  |  "app_secret": "${Config.appSecret}"
                  |}""".stripMargin

    val post = new HttpPost(URL)
    post.setHeader("Content-Type", "application/json, charset=utf-8")
    post.setEntity(new StringEntity(body, "UTF-8"))

    val client = HttpClientBuilder.create().build()
    val response = client.execute(post)
    val responseBody =
      scala.io.Source.fromInputStream(response.getEntity.getContent).mkString
    response.close()
    client.close()

    val tenantAcessToken = JsonHelper(responseBody, classOf[TenantAcessToken])
    if (tenantAcessToken.code != 0) {
      LOG.error("Cannot get tenant access token: " + tenantAcessToken.msg)
      false
    } else {
      LOG.info("Got tenant access token: " + tenantAcessToken.tenantAccessToken)
      myTenantAcessToken = Some(
        (System.currentTimeMillis() / 1000, tenantAcessToken)
      )
      true
    }
  }

  def getAllClassmates: Seq[(String, String)] = {
    // FIRST -- get all group chats!
    val URL = "https://open.feishu.cn/open-apis/im/v1/chats"

    val get = new HttpGet(URL)
    get.setHeader(
      "Authorization",
      "Bearer " + myTenantAcessToken.get._2.tenantAccessToken
    )

    val client = HttpClientBuilder.create().build()
    val response = client.execute(get)
    val responseBody =
      scala.io.Source.fromInputStream(response.getEntity.getContent).mkString
    response.close()
    client.close()

    val json = JsonHelper(responseBody, classOf[GroupInfo])
    if (json.code != 0 || json.data == null || json.data.items == null) {
      LOG.error("Cannot get all classmates: " + json.msg)
      Seq.empty
    } else {
      val groups = json.data.items
      val group =
        groups.find(_.name.contains(Config.groupKeyword)).map(_.chatID)
      if (group.isEmpty) {
        LOG.error("Cannot find group chat: " + Config.groupKeyword)
        Seq.empty
      } else {
        val groupID = group.get

        // SECOND -- get ALL people in this group
        val URL =
          "https://open.feishu.cn/open-apis/im/v1/chats/" + groupID + "/members?member_id_type=open_id&page_size=50"
        val get = new HttpGet(URL)
        get.setHeader(
          "Authorization",
          "Bearer " + myTenantAcessToken.get._2.tenantAccessToken
        )

        val client = HttpClientBuilder.create().build()
        val response = client.execute(get)
        val responseBody = scala.io.Source
          .fromInputStream(response.getEntity.getContent)
          .mkString
        response.close()
        client.close()

        val json = JsonHelper(responseBody, classOf[GroupMembers])
        if (json.code != 0 || json.data == null || json.data.items == null) {
          LOG.error("Cannot get all classmates: " + json.msg)
          Seq.empty
        } else {
          val members = json.data.items
          members.map(f => (f.memberID, f.name))
        }
      }
    }
  }

  def sendSingleMessage(msg: String, userID: String): Unit = {
    val URL =
      "https://open.feishu.cn/open-apis/im/v1/messages?receive_id_type=open_id"
    val body = s"""{
                  |  "receive_id": "$userID",
                  |  "msg_type": "text",
                  |  "content": "{\\"text\\": \\"$msg\\"}"
                  |}""".stripMargin

    LOG.info(s"Send message to $userID: $body")
    val post = new HttpPost(URL)
    post.setHeader("Content-Type", "application/json, charset=utf-8")
    post.setHeader(
      "Authorization",
      "Bearer " + myTenantAcessToken.get._2.tenantAccessToken
    )
    post.setEntity(new StringEntity(body, "UTF-8"))

    val client = HttpClientBuilder.create().build()
    val response = client.execute(post)
    val responseBody =
      scala.io.Source.fromInputStream(response.getEntity.getContent).mkString
    response.close()
    client.close()

    LOG.info("Send message to " + userID + ": " + responseBody)
  }

  def sendGroupMessage(msg: String, userIDs: Seq[String]) = {
    val URL = "https://open.feishu.cn/open-apis/message/v4/batch_send/"
    val body = s"""{
                  |  "open_ids": [ ${userIDs.map(f => {"\"" + f + "\""}).mkString(", ")} ],
                  |  "msg_type": "text",
                  |  "content": {"text": "$msg"}
                  |}""".stripMargin

    LOG.info(s"Send message to ${userIDs.mkString(", ")}: $body")

    val post = new HttpPost(URL)
    post.setHeader("Content-Type", "application/json, charset=utf-8")
    post.setHeader(
      "Authorization",
      "Bearer " + myTenantAcessToken.get._2.tenantAccessToken
    )
    post.setEntity(new StringEntity(body, "UTF-8"))

    val client = HttpClientBuilder.create().build()
    val response = client.execute(post)
    val responseBody =
      scala.io.Source.fromInputStream(response.getEntity.getContent).mkString
    response.close()
    client.close()

    LOG.info("Send message to " + userIDs.mkString(", ") + ": " + responseBody)

    val result = JsonHelper(responseBody, classOf[BatchResponse])
    result
  }

  def checkMessageStatus(msgID: String): BatchStatus = {
    val URL = "https://open.feishu.cn/open-apis/im/v1/batch_messages/" + msgID + "/read_user"
    val get = new HttpGet(URL)
    get.setHeader(
      "Authorization",
      "Bearer " + myTenantAcessToken.get._2.tenantAccessToken
    )

    val client = HttpClientBuilder.create().build()
    val response = client.execute(get)
    val responseBody =
      scala.io.Source.fromInputStream(response.getEntity.getContent).mkString
    response.close()
    client.close()

    LOG.info("Check message status: " + responseBody)
    val json = JsonHelper(responseBody, classOf[BatchStatus])
    json
  }
}
