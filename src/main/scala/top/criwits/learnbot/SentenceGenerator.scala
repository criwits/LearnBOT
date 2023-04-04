package top.criwits.learnbot

import org.apache.http.client.HttpClient
import org.apache.http.client.methods.HttpGet
import org.apache.http.impl.client.HttpClientBuilder
import top.criwits.learnbot.json.Sentence

object SentenceGenerator {
  val youthStudySentences: Seq[(Boolean, String)] = Seq(
    (false, "看青年大学习喵，看青年大学习谢谢喵！"),
    (false, "让我看看谁还没有看青年大学习？"),
    (true, "我在这里放一只猫猫，看完青年大学习的可以摸，没看完青年大学习的不可以摸><\\\\n　　　　　 ＿＿\\\\n　　　　／＞　　フ\\\\n　　　　|  　_　 _ l\\\\n　 　　／` ミ＿xノ\\\\n　 　 /　　　 　 |\\\\n　　 /　 ヽ　　 ﾉ\\\\n　   │　　|　|　|\\\\n ／￣|　　 |　|　|\\\\n | (￣ヽ＿_ヽ_)__)\\\\n ＼二つ  "),
    (false, "请看青年大学习，质量非常好，孩子们都特别喜欢吃，版型很好，面料细腻，尺码合适，沾杯还持妆久，橘色系更加显白，洗完很柔顺头发一点也不油，屏幕清晰运行流畅，敏感肌也可以用，握感属实不伤牙龈，每次炒菜我都会放一点，很满意。"),
    (false, "今天是青年大学习瘾发作最严重的一次， 我躺在床上会想青年大学习，我出门会想青年大学习，我走路会想青年大学习，我坐车会想青年大学习我玩手机会想青年大学习，青年大学习就是我的生命之光是我黯淡人生唯一的意义是我干涸希望的一丝甘甜是我无趣生活仅有的美好是红丝绒上冰冷的宝石是尖锐塔顶的刺眼反光是我命悬一线无力攀上的浮木是我心脏上的针是我头顶的绞架——"),
    (false, "【青年大学习】您本周的青年大学习还没有观看，请您在百忙之中打开「广东共青团」公众号，收看本期的精彩视频。")
  )

  def randomYouthStudySentence: (Boolean, String) = {
    val randomIndex = (Math.random() * youthStudySentences.length).toInt
    youthStudySentences(randomIndex)
  }

  // (timestamp, sentence)
  // Update sentence by 60 sec
  var lastSentence: Option[(Long, String)] = None

  def getNetworkSentence: Option[String] = this.synchronized {
    val now = System.currentTimeMillis() / 1000
    if (lastSentence.isEmpty || now - lastSentence.get._1 > 30) {
      // Need to fetch...
      val URL = "https://v1.hitokoto.cn/?c=d&c=i&c=k"

      val get = new HttpGet(URL)
      val client = HttpClientBuilder.create().build()
      val response = client.execute(get)
      val responseBody =
        scala.io.Source.fromInputStream(response.getEntity.getContent).mkString
      response.close()
      client.close()

      val body = JsonHelper(responseBody, classOf[Sentence])
      if (body.hitokoto != null) {
        lastSentence = Some((now, body.hitokoto + "(" + {if (body.from != null) {body.from} else {"未知"}} + ")"))
        Some(lastSentence.get._2)
      } else {
        None
      }
    } else {
      Some(lastSentence.get._2)
    }
  }
}
