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
        val contentBody = JsonHelper(content, classOf[Content])
        val senderID = msg.event.sender.senderID.openID
        LOG.info(s"Received message from $senderID: $contentBody")

        // Plain text message
        if (contentBody.text != null && !contentBody.text.isBlank) {
          if (Config.adminUsersID.contains(senderID)) {
            // Message from administrator -- handle it!
            if (contentBody.text != null) {
              val contentText = contentBody.text
              FeishuAPI.handle(senderID, contentText)
            }
          } else {
            // Message from other student -- introduce myself!
            FeishuAPI.sendSingleMessage(
              s"嗨！我是「青年大学习机器人」。你可以在 https://github.com/criwits/LearnBOT 找到我的源码。\\\\n---\\\\n${SentenceGenerator.getNetworkSentence.getOrElse("我好像出了点问题。")}",
              senderID
            )
          }
        }

        // File message
        if (contentBody.fileKey != null && !contentBody.fileKey.isBlank) {
          // check filename
          val fileName = contentBody.fileName.toLowerCase()
          if (fileName.endsWith(".doc") || fileName.endsWith(".docx")) {
            // who?
            val stuList = FeishuAPI.getAllClassmates
            val stu = stuList.find(_._1 == senderID)
            if (stu.isEmpty) {
               FeishuAPI.sendSingleMessage(
              "出现了一些问题……麻烦您直接将作业交给班长，谢谢！", senderID
              )
            } else {
              val name = stu.get._2
              val id = StuList.students.get(name)
              val fname = id + "-" + name + {if (fileName.endsWith(".docx")) ".docx" else ".doc"}

              // save file
              val save = FeishuAPI.downloadChatFile(msg.event.message.messageID, contentBody.fileKey, fname)
              if (save) {
                FeishuAPI.sendSingleMessage(s"已成功接收（${fname}）。", senderID)
              } else {
                FeishuAPI.sendSingleMessage(s"无法下载您的文件，这可能是由于文件过大或其他原因。麻烦您直接将作业交给班长，谢谢！", senderID)
              }
            }
          } else {
            // Not valid file
            FeishuAPI.sendSingleMessage(
              "文件格式好像不对。请向我发送扩展名为 `doc` 或 `docx` 的文件。", senderID
            )
          }
        }
      }
    }

    // To simplify the implementation, all events all be responded with '200 OK'.
    resp.setContentType("application/json")
    resp.setStatus(HttpServletResponse.SC_OK)

  }
}
