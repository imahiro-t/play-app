package controllers

import models.Notification

import java.sql.Time
import java.util.Date

import play.api.data.Form
import play.api.data.Forms._

case class NotificationForm(command: Option[String], notification: Notification)

object Forms {
  def notificationForm = Form(
    mapping(
      "command" -> optional(text),
      "db" ->
        mapping(
          "id" -> optional(longNumber),
          "subject" -> nonEmptyText,
          "actionDate" -> sqlDate("yyyy-MM-dd"),
          "actionTime" -> date("HH:mm"),
          "notifyBefore" -> number,
          "summary" -> text
        )
        ((id, subject, actionDate, actionTime, notifyBefore, summary)
        => new Notification(id, subject, actionDate, actionTime.convert, notifyBefore, summary, new Date, false))
        ((n: Notification) => Some((n.id, n.subject, n.actionDate, n.actionTime.convert, n.notifyBefore, n.summary)))
    )(NotificationForm.apply)(NotificationForm.unapply)
  )

  implicit class DateToTimeConversion(date: Date) {
    def convert: Time = {
      if (date != null) new Time(date.getTime())
      else new Time(System.currentTimeMillis())
    }
  }

  implicit class TimeToDateConversion(time: Time) {
    def convert: Date = {
      if (time != null) new Date(time.getTime())
      else new Date(System.currentTimeMillis())
    }
  }

}
