package models

import java.util.Date

case class Notification (
  id: Option[Long],
  subject: String,
  actionDate: Date,
  notifyBefore: Int,
  summary: String,
  notificationDate: Date,
  sent: Boolean
)
