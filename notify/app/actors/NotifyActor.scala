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
      Logger.info((new java.util.Date).toString)
      Logger.info("Message(NOTIFY) received")
      dao.getSendList().map { notifications =>
        Logger.info("notifications.size -> " + notifications.size)
        for (notification <- notifications) {
          Logger.info("notification.id -> " + notification.id)
          val email = Email(
            notification.subject,
            mailFrom,
            Seq(mailTo),
            bodyText = Some("[件名]" +
              "\n" +
              notification.subject +
              "\n" +
              "[日時]" +
              "\n" +
              notification.actionDate +
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
          Logger.info("Mail sent")
          dao.sent(notification.id.getOrElse(0))
          Logger.info("Model(notification) updated")
        }
      }
  }
}