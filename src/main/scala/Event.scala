import java.time._

import biweekly.component.VEvent

case class Event(startDate: LocalDate, source: Source, description: String, url: String, data: VEvent)
