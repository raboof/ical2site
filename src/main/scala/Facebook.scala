object Facebook {
  lazy val token = {
    val appId = sys.env("FB_CLIENT_ID")
    val appSecret = sys.env("FB_CLIENT_SECRET")
    val response = scala.io.Source
      .fromURL(
        s"https://graph.facebook.com/oauth/access_token?client_id=$appId&client_secret=$appSecret&grant_type=client_credentials"
      )
      .mkString
    if (response.startsWith("access_token=")) {
      response.split("=")(1)
    } else {
      throw new IllegalStateException(s"Failed to get FB access token: got $response")
    }
  }
}
