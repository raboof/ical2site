import spray.json._

case class Config(mainTitle: String, subtitle: String, lang: String, manifest: JsObject, sources: List[Source])
