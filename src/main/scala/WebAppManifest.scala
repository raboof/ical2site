case class Icon(
  src: String,
  _type: String,
  sizes: String
)

case class WebAppManifest(
  short_name: String,
  name: String,
  icons: List[Icon],
  background_color: String,
  theme_color: String,
  start_url: String
)
