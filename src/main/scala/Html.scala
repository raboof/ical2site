import java.time._

import scalatags.Text.all._
import scalatags.Text.TypedTag

object Html {
  def list(events: Seq[Event]) = page(div(eventListHtml(events)))

  private def page(content: TypedTag[_]) =
    html(
      head(
        link(href := "https://fonts.googleapis.com/css?family=Lobster%20Two|Raleway", rel := "stylesheet"),
        link(href := "style.css", rel := "stylesheet")
      ),
      body(style := "font-family: 'Raleway', sans-serif")(
        div(cls := "heading")(
          div(cls := "title")(style := "font-family: 'Lobster Two', cursive; font-size: 62px")("Deventer.live"),
          div(cls := "subtitle")("Concerten en activiteiten in Deventer")
        ),
        content
      )
    )

  implicit val localDateOrder = new Ordering[LocalDate] {
    override def compare(x: LocalDate, y: LocalDate) = x.compareTo(y)
  }

  private def eventListHtml(events: Seq[Event]) =
    events.filter(!_.startDate.isBefore(LocalDate.now())).groupBy(_.startDate).toList.sortBy(_._1).map {
      case (date, events) =>
        div(
          h2(s"$date"),
          events.groupBy(_.source).toList.map {
            case (source, events) =>
              div(
                h3(s"${source.name}:"),
                events.map(evt => {
                  val uid = evt.data.getUid.getValue
                  div(cls := "evt", style := "cursor:hand")(
                    label(`for` := uid)(
                      div(cls := "evt-title")(s"${evt.data.getSummary.getValue}"),
                      input(cls := "togglebox", `type` := "checkbox", id := uid),
                      div(cls := "evt-detail", id := uid)(
                        s"${evt.description}",
                        evt.url.map(url => a(href := url)("site"))
                      )
                    )
                  )
                })
              )
          }
        )
    }
}
