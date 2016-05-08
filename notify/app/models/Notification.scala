package models

case class Notification (
  id: Option[Long],
  subject: String,
  actionDate: java.sql.Date,
  actionTime: java.sql.Time,
  notifyBefore: Int,
  summary: String,
  notificationDate: java.util.Date,
  sent: Boolean
)
