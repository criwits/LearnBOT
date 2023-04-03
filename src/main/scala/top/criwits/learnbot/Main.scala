package top.criwits.learnbot

import com.typesafe.scalalogging.Logger
import org.eclipse.jetty.server.Server
import org.eclipse.jetty.servlet.ServletHandler

object Main {
  val LOG = Logger("Main")

  def main(args: Array[String]): Unit = {
    LOG.info("This is LearnBOT!")

    val server = new Server(Config.port)
    val handler = new ServletHandler
    server.setHandler(handler)

    handler.addServletWithMapping(classOf[EventServlet], "/")

    server.start()
    server.join()
  }
}