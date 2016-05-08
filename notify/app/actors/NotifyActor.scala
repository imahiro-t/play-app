package actors

import dao.NotificationDAO
import akka.actor._
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.mailer.{MailerClient, Email}
import com.typesafe.config.ConfigFactory

import play.api.Logger

object NotifyActor {
  def props(dao: NotificationDAO, mc: MailerClient) = Props(classOf[NotifyActor], dao, mc)
}

class NotifyActor(dao: NotificationDAO, mc: MailerClient) extends Actor {

  val config = ConfigFactory.load()
  val mailFrom = config.getString("notify.mail.from")
  val mailTo = config.getString("notify.mail.to")

  def receive = {
    case "NOTIFY" =>
      Logger.debug((new java.util.Date).toString)
      Logger.debug("Message(NOTIFY) received")
      dao.getSendList().map { notifications =>
        Logger.debug("notifications.size -> " + notifications.size)
        for (notification <- notifications) {
          Logger.debug("notification.id -> " + notification.id)
          val email = Email(
            notification.subject,
            mailFrom,
            Seq(mailTo),
            bodyText = Some("[件名]" +
              "\n" +
              notification.subject +
              "\n" +
              "[日付]" +
              "\n" +
              notification.actionDate +
              "\n" +
              "[時間]" +
              "\n" +
              notification.actionTime +
              "\n" +
              "[通知]" +
              "\n" +
              notification.notificationDate +
              "\n" +
              "[説明]" +
              "\n" +
              notification.summary)
          )
          mc.send(email)
          Logger.debug("Mail sent")
          dao.sent(notification.id.getOrElse(0))
          Logger.debug("Model(notification) updated")
        }
      }
  }
}