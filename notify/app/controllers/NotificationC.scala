package controllers

import javax.inject.{Inject, Singleton}

import dao.NotificationDAO
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, Controller}
import play.api.libs.concurrent.Execution.Implicits.defaultContext

import scala.concurrent.Future

@Singleton
class NotificationC @Inject() (val messagesApi: MessagesApi, dao: NotificationDAO) extends Controller with I18nSupport {

  def getNotification(id: Long) = Action.async {
    dao.byId(id).map(
      option => option match {
        case Some(notification) => Ok(views.html.notification("", Forms.notificationForm.fill(NotificationForm(None, notification))))
        case None => Redirect("/notify")
      }
    )
  }

  def postNotification(id: Long) = Action.async { implicit request =>
    Forms.notificationForm.bindFromRequest.fold(
      formWithErrors => {
        Future(BadRequest(views.html.notification("ERROR", formWithErrors)))
      },
      formValue => {
        formValue.command match {
          case Some("update") => dao.update(formValue.notification).map(_ =>
            Redirect("/notify").flashing("message" -> ("[" + formValue.notification.subject + "]" + "を更新しました。"))
          )
          case Some("delete") => dao.delete(formValue.notification.id.getOrElse(0)).map(_ =>
            Redirect("/notify").flashing("message" -> ("[" + formValue.notification.subject + "]" + "を削除しました。"))
          )
          case _ => Future(Redirect("/notify"))
        }
      }
    )
  }

}
