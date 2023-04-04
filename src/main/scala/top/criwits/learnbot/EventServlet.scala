package top.criwits.learnbot

import com.typesafe.scalalogging.Logger
import jakarta.servlet.http.{
  HttpServlet,
  HttpServletRequest,
  HttpServletResponse
}
import top.criwits.learnbot.json.{Content, Msg}

/** This is the [[HttpServlet]] for Feishu events.
  * Events are callbacks from Feishu open APIs, mainly for message receiving.
  *
  * Generously, this servlet handles following POST requests:
  *  - Event Setup -- [[https://open.feishu.cn/document/ukTMukTMukTM/uYDNxYjL2QTM24iN0EjN/event-subscription-configure-/subscription-event-case]]
  *  - Message Received -- [[https://open.feishu.cn/document/uAjLw4CM/ukTMukTMukTM/reference/im-v1/message/events/receive]]
  */
final class EventServlet extends HttpServlet {
  private val LOG: Logger = Logger(classOf[EventServlet])

  override protected def doPost(
      req: HttpServletRequest,
      resp: HttpServletResponse
  ): Unit = {
    val requestBody = req.getReader.lines().toArray.mkString
    LOG.info(s"Received POST message $requestBody")
    val msg = JsonHelper(requestBody, classOf[Msg])

    if (msg.challenge != null && !msg.challenge.isBlank) {
      // Event registration
      if (msg.token != null && msg.token == Config.verificationToken) {
        resp.getWriter.println(s"""{
             |  "challenge": "${msg.challenge}"
             |}
             |""".stripMargin)
      }
    }

    if (
      msg.header != null && msg.header.token != null && msg.header.token == Config.verificationToken && msg.event != null && msg.event.sender != null && msg.event.sender.senderID != null && msg.event.sender.senderID.userID != null
    ) {
      // A message
      if (
        msg.event.message != null && msg.event.message.content != null && !msg.event.message.content.isBlank
      ) {
        // A message with content
        val content = msg.event.message.content
        val text = JsonHelper(content, classOf[Content])
        val senderID = msg.event.sender.senderID.openID
        LOG.info(s"Received message from $senderID: $text")
        if (text.text != null) {
          val contentText = text.text
          if (Config.adminUsersID.contains(senderID)) {
            // Message from administrator -- handle it!
            FeishuAPI.handle(senderID, contentText)
          } else {
            // Message from other student -- introduce myself!
            FeishuAPI.sendSingleMessage(
              s"嗨！我是「青年大学习机器人」。你可以在 https://github.com/criwits/LearnBOT 找到我的源码。",
              senderID)
          }
        }
      }
    }

    // To simplify the implementation, all events all be responded with '200 OK'.
    resp.setContentType("application/json")
    resp.setStatus(HttpServletResponse.SC_OK)

  }
}
