package controllers

import scala.concurrent.Future

import dao.NotificationDAO
import models.Notification

import java.util.Date

import javax.inject.Inject
import javax.inject.Singleton

import play.api.data.Form
import play.api.data.Forms.mapping
import play.api.data.Forms.{longNumber,text,date,number,nonEmptyText,optional}
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.mvc.Action
import play.api.mvc.Controller

import play.api.i18n.MessagesApi
import play.api.i18n.I18nSupport

import play.api.Logger

case class NotificationForm(command: Option[String], notification: Notification)

@Singleton
class Application @Inject() (val messagesApi: MessagesApi, dao: NotificationDAO) extends Controller with I18nSupport {

  val notificationForm = Form(
    mapping(
      "command" -> optional(text),
      "db" ->
        mapping(
          "id" -> optional(longNumber),
          "subject" -> nonEmptyText,
          "actionDate" -> date("yyyy-MM-dd'T'HH:mm"),
          "notifyBefore" -> number,
          "summary" -> text
        )
        ((id, subject, actionDate, notifyBefore, summary) => new Notification(id, subject, actionDate, notifyBefore, summary, new Date, false))
        ((n: Notification) => Some((n.id, n.subject, n.actionDate, n.notifyBefore, n.summary)))
    )(NotificationForm.apply)(NotificationForm.unapply)
  )

  def getNotify = Action.async {
    dao.getNotificationsSent().flatMap(
      notificationsSent => dao.getNotificationsSentNotYet().map(
        notificationsSentNotYet => Ok(views.html.notifications("", notificationsSent, notificationsSentNotYet, notificationForm))
      )
    )
  }

  def postNotify = Action.async { implicit request =>
    notificationForm.bindFromRequest.fold(
      formWithErrors => {
        dao.getNotificationsSent().flatMap(
          notificationsSent => dao.getNotificationsSentNotYet().map(
            notificationsSentNotYet => BadRequest(views.html.notifications("", notificationsSent, notificationsSentNotYet, formWithErrors))
          )
        )
      },
      formValue => {
        dao.create(formValue.notification).map(_ => Redirect("/notify"))
      }
    )
  }

  def getNotification(id: Long) = Action.async {
    dao.byId(id).map(
      option => option match {
        case Some(notification) => Ok(views.html.notification("", notificationForm.fill(NotificationForm(None, notification))))
        case None => Redirect("/notify")
      }
    )
  }

  def postNotification(id: Long) = Action.async { implicit request =>
    notificationForm.bindFromRequest.fold(
      formWithErrors => {
        Future(BadRequest(views.html.notification("ERROR", formWithErrors)))
      },
      formValue => {
        formValue.command match {
          case Some("update") => dao.update(formValue.notification).map(_ => Redirect("/notify"))
          case Some("delete") => dao.delete(formValue.notification.id.getOrElse(0)).map(_ => Redirect("/notify"))
          case _ => Future(Redirect("/notify"))
        }
      }
    )
  }

}