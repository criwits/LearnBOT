package top.criwits.learnbot

import com.typesafe.scalalogging.Logger
import jakarta.servlet.http.{
  HttpServlet,
  HttpServletRequest,
  HttpServletResponse
}
import org.apache.pdfbox.pdmodel.{PDDocument, PDPage}
import org.apache.pdfbox.rendering.{ImageType, PDFRenderer}
import top.criwits.learnbot.json.{Content, Msg}

import java.io.{File, PrintWriter}
import java.util.UUID
import javax.imageio.ImageIO

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
      msg.header != null &&
      msg.header.token != null &&
      msg.header.token == Config.verificationToken &&
      msg.event != null && msg.event.sender != null &&
      msg.event.sender.senderID != null && msg.event.sender.senderID.userID != null
    ) {
      // A message
      if (
        msg.event.message != null && msg.event.message.content != null && !msg.event.message.content.isBlank
      ) {
        val thread = new Thread(() => {
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
                s"嗨！我是「青年大学习机器人」。你可以在 https://github.com/criwits/LearnBOT 找到我的源码。\\\\n---\\\\n好句推荐：${SentenceGenerator.getNetworkSentence
                  .getOrElse("句子获取失败。")}",
                senderID
              )
            }
          }

          // File message
          // For PrintCentre
          if (contentBody.fileKey != null && !contentBody.fileKey.isBlank) {
            // check filename
            val fileName = contentBody.fileName.toLowerCase()
            if (fileName.endsWith(".pdf")) {
              // save file
              val uuid = UUID.randomUUID().toString
              val baseName = s"./print/${uuid}"
              val save = FeishuAPI.downloadChatFile(
                msg.event.message.messageID,
                contentBody.fileKey,
                baseName + ".pdf"
              )
              if (save) {
                FeishuAPI.sendSingleMessage("您的文件已被接收，正在处理，请稍等。", senderID)
                try {
                  val document = PDDocument.load(new File(baseName + ".pdf"))
                  val pages = document.getNumberOfPages

                  if (pages > 1) { // only process multi-page pdf
                    val allPages = document.getDocumentCatalog.getPages
                    var pageCnt = document.getNumberOfPages
                    if (pageCnt % 2 == 1) {
                      val blankPage = new PDPage(allPages.get(0).getMediaBox)
                      allPages.add(blankPage)
                      pageCnt += 1
                    }
                    val oddPages = new PDDocument()
                    val evenPages = new PDDocument()
                    for (i <- 0 until pageCnt) {
                      val page = allPages.get(i)
                      if ((i + 1) % 2 == 1) {
                        oddPages.addPage(page)
                      }
                    }

                    for (i <- 0 until pageCnt) {
                      val j = pageCnt - i - 1
                      val page = allPages.get(j)
                      if ((j + 1) % 2 == 0) {
                        page.setRotation(180)
                        evenPages.addPage(page)
                      }
                    }

                    oddPages.save(baseName + "-odd.pdf")
                    evenPages.save(baseName + "-even.pdf")
                    oddPages.close()
                    evenPages.close()

                    // execute pdf2printable again for even pages
                    val pdf2printableEven = new ProcessBuilder(
                      "/usr/bin/pdf2printable",
                      "-f",
                      "urf",
                      "-r",
                      "600",
                      "-c",
                      "gray8",
                      "-q",
                      "normal",
                      "-aa",
                      baseName + "-even.pdf",
                      baseName + "-even.urf"
                    )
                    val pdf2printableEvenProcess = pdf2printableEven.start()
                    pdf2printableEvenProcess.waitFor()
                    if (pdf2printableEvenProcess.exitValue() != 0) {
                      throw new RuntimeException("pdf2printable failed")
                    }

                    // execute pdf2printable again for odd pages
                    val pdf2printableOdd = new ProcessBuilder(
                      "/usr/bin/pdf2printable",
                      "-f",
                      "urf",
                      "-r",
                      "600",
                      "-c",
                      "gray8",
                      "-q",
                      "normal",
                      "-aa",
                      baseName + "-odd.pdf",
                      baseName + "-odd.urf"
                    )
                    val pdf2printableOddProcess = pdf2printableOdd.start()
                    pdf2printableOddProcess.waitFor()
                    if (pdf2printableOddProcess.exitValue() != 0) {
                      throw new RuntimeException("pdf2printable failed")
                    }
                  }

                  // Write metadata (pages) into <base>.meta
                  val meta = new PrintWriter(new File(baseName + ".meta"))
                  meta.println(document.getNumberOfPages)
                  meta.close()

                  // create image from First page
                  val renderer = new PDFRenderer(document)
                  val image = renderer.renderImageWithDPI(0, 300, ImageType.RGB)
                  // write image to file
                  ImageIO.write(
                    image,
                    "png",
                    new File(baseName + "-preview.png")
                  )
                  document.close()

                  // execute pdf2printable -f urf -r 600 -c gray8 -q normal -aa <.pdf> <.urf> and wait
                  val pdf2printable = new ProcessBuilder(
                    "/usr/bin/pdf2printable",
                    "-f",
                    "urf",
                    "-r",
                    "600",
                    "-c",
                    "gray8",
                    "-q",
                    "normal",
                    "-aa",
                    baseName + ".pdf",
                    baseName + ".urf"
                  )
                  val pdf2printableProcess = pdf2printable.start()
                  pdf2printableProcess.waitFor()
                  if (pdf2printableProcess.exitValue() != 0) {
                    throw new RuntimeException("pdf2printable failed")
                  }

                  // Generate UUID QR code as <uuid>.png
                  val qrCode = new ProcessBuilder(
                    "/usr/bin/qrencode",
                    "-s",
                    "10",
                    "-o",
                    baseName + ".png",
                    uuid
                  )
                  val qrCodeProcess = qrCode.start()
                  qrCodeProcess.waitFor()
                  if (qrCodeProcess.exitValue() != 0) {
                    throw new RuntimeException("qrencode failed")
                  }

                  // setup a thread to delete all files starts with <base> in 72 h
                  val deleteThread = new Thread(() => {
                    Thread.sleep(72 * 60 * 60 * 1000)
                    val delete = new ProcessBuilder(
                      "/usr/bin/find",
                      "./print",
                      "-name",
                      uuid + "*",
                      "-delete"
                    )
                    val deleteProcess = delete.start()
                    deleteProcess.waitFor()
                  })
                  deleteThread.start()

                  // send message to user
                  FeishuAPI.sendSingleMessage(
                    s"您的打印凭证二维码如下。请在打印终端扫描二维码以进行打印。请注意，您的文件将在 72 小时后被自动删除。",
                    senderID
                  )
                  // send image to user
                  FeishuAPI.sendImage(new File(baseName + ".png"), senderID)
                } catch {
                  case e: Exception =>
                    FeishuAPI.sendSingleMessage(
                      s"无法处理您的文件。您可以试试重新上传，或者携带 U 盘来手动打印。",
                      senderID
                    )
                }

              } else {
                FeishuAPI.sendSingleMessage(
                  s"无法下载您的文件，这可能是由于文件过大或其他原因。您可以试试重新上传，或者携带 U 盘来手动打印。",
                  senderID
                )
              }
            } else {
              // Not valid file
              FeishuAPI.sendSingleMessage(
                "非常抱歉，目前仅支持打印 PDF 格式的文件！您可以携带 U 盘前来手动打印，或将文档自行转换格式后再上传！",
                senderID
              )
            }
          }
        })
        thread.start() // handle the request in a new thread
      }
    }

    // To simplify the implementation, all events all be responded with '200 OK'.
    resp.setContentType("application/json")
    resp.setStatus(HttpServletResponse.SC_OK)

  }
}
