import spray.json._

case class Config(
  mainTitle: String,
  subtitle: String,
  lang: String,
  backgroundColor: String,
  themeColor: String,
  sources: List[Source])
