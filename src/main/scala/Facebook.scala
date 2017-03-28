import spray.json._

object Facebook {
  lazy val token = {
    val appId = sys.env("FB_CLIENT_ID")
    val appSecret = sys.env("FB_CLIENT_SECRET")
    val response = scala.io.Source
      .fromURL(
        s"https://graph.facebook.com/oauth/access_token?client_id=$appId&client_secret=$appSecret&grant_type=client_credentials"
      )
      .mkString
      .parseJson
    response match {
      case JsObject(map) => map("access_token") match {
        case JsString(string) => string
        case other => throw new IllegalStateException(s"Failed to parse FB access token: got $other")
      }
      case _ => throw new IllegalStateException(s"Failed to get FB access token: got $response")
    }
  }
}
