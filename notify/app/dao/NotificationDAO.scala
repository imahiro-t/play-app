package dao

import scala.concurrent.Future
import java.util.Date
import java.util.Calendar
import java.sql.Time
import java.sql.Timestamp
import javax.inject.Inject
import javax.inject.Singleton
import models.Notification
import play.api.db.slick.DatabaseConfigProvider
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import slick.driver.JdbcProfile

@Singleton
class NotificationDAO @Inject()(dbConfigProvider: DatabaseConfigProvider) {

  val dbConfig = dbConfigProvider.get[JdbcProfile]

  import dbConfig.driver.api._

  implicit def javaDateTimestampMapper = MappedColumnType.base[Date, Timestamp](
    dt => new Timestamp(dt.getTime),
    ts => new Date(ts.getTime)
  )

  private class NotificationTable(tag: Tag) extends Table[Notification](tag, "notification") {
    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
    def subject = column[String]("subject")
    def actionDate = column[java.sql.Date]("action_date")
    def actionTime = column[java.sql.Time]("action_time")
    def notifyBefore = column[Int]("notify_before")
    def summary = column[String]("summary")
    def notificationDate = column[Date]("notification_date")
    def sent = column[Boolean]("sent", O.Default(false))
    def * = (id.?, subject, actionDate, actionTime, notifyBefore, summary, notificationDate, sent) <> ((Notification.apply _).tupled, Notification.unapply)
  }

  private val notifications = TableQuery[NotificationTable]

  def getNotificationsSent(): Future[List[Notification]] =
    dbConfig.db.run(notifications.filter(n => (n.sent === true)).result).map(_.toList)

  def getNotificationsSentNotYet(): Future[List[Notification]] =
    dbConfig.db.run(notifications.filter(n => (n.sent === false)).result).map(_.toList)

  def byId(id: Long): Future[Option[Notification]] = dbConfig.db.run(notifications.filter(_.id === id).result.headOption)

  def create(notification: Notification): Future[Int] = {
    val n = notification.copy(
      notificationDate = calcNotificationDate(notification.actionDate, notification.actionTime, notification.notifyBefore),
      sent = false
    )
    dbConfig.db.run(notifications += n)
  }

  def update(notification: Notification): Future[Int] = {
    dbConfig.db.run(notifications.filter(_.id === notification.id).map(
      n => (
        n.subject,
        n.actionDate,
        n.actionTime,
        n.notifyBefore,
        n.summary,
        n.notificationDate,
        n.sent
        )
    ).update(
      notification.subject,
      notification.actionDate,
      notification.actionTime,
      notification.notifyBefore,
      notification.summary,
      calcNotificationDate(notification.actionDate, notification.actionTime, notification.notifyBefore),
      false
     )
    )
  }

  def getSendList(): Future[List[Notification]] =
    dbConfig.db.run(notifications.filter(n => (n.sent === false) && (n.notificationDate < new Date)).result).map(_.toList)

  def sent(id: Long): Future[Int] =
    dbConfig.db.run(notifications.filter(_.id === id).map(n => (n.sent)).update(true))

  def delete(id: Long): Future[Int] =
    dbConfig.db.run(notifications.filter(_.id === id).delete)

  private def calcNotificationDate(actionDate: java.sql.Date, actionTime: Time, notifyBefore: Int): Date = {
    val cl = Calendar.getInstance
    val clTmp = Calendar.getInstance
    cl.setTimeInMillis(actionDate.getTime())
    clTmp.setTimeInMillis(actionTime.getTime())
    cl.set(Calendar.HOUR_OF_DAY, clTmp.get(Calendar.HOUR_OF_DAY))
    cl.set(Calendar.MINUTE, clTmp.get(Calendar.MINUTE))
    cl.add(Calendar.MINUTE, -notifyBefore)
    cl.getTime
  }

}