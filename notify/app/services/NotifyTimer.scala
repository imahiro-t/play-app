package services

import dao.NotificationDAO
import actors.NotifyActor

import javax.inject.Inject
import javax.inject.Singleton

import java.util.Calendar

import akka.actor._
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global

import play.api.libs.mailer._

import play.api.Logger

@Singleton
class NotifyTimer @Inject() (dao: NotificationDAO, system: ActorSystem, mc: MailerClient) {

  Logger.info("start Actaor")
  val notifyActor = system.actorOf(NotifyActor.props(dao, mc), "notify-actor")
  var cl = Calendar.getInstance
  cl.set(Calendar.SECOND, 0)
  cl.set(Calendar.MILLISECOND, 0)
  cl.add(Calendar.MINUTE, 1)
  system.scheduler.schedule(
    (cl.getTimeInMillis - System.currentTimeMillis).milliseconds,
    1.minutes,
    notifyActor,
    "NOTIFY"
  )
  Logger.info("Actor has started")

}
