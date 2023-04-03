package top.criwits.learnbot

/**
 * Global configuration for LearnBOT.
 * Feishu app ID, tokens, administrator information and group information are stored here.
 *
 * Fill information in before compiling!
 */
object Config {
  // Feishu app ID & tokens
  val appID: String = ""
  val appSecret: String = ""
  val verificationToken: String = ""

  // Administrator information
  val adminUsersID: Seq[String] = Seq()

  // Group information
  val groupChatID: String = ""

  // Server information
  val port = 21000
}
