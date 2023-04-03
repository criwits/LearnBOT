package top.criwits.learnbot

import com.fasterxml.jackson.databind.json.JsonMapper
import com.fasterxml.jackson.module.scala.DefaultScalaModule

object JsonHelper {
  private val mapper = JsonMapper.builder().addModule(DefaultScalaModule).build()

  // Object -> JSON
  def apply(value: Any): String = mapper.writeValueAsString(value)

  // JSON -> Object
  def apply[T](string: String, clazz: Class[T]): T = mapper.readValue[T](string, clazz)
}
