package controllers

import javax.inject.{Inject, Singleton}

import dao.NotificationDAO
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, Controller}
import play.api.libs.concurrent.Execution.Implicits.defaultContext

@Singleton
class NotifyC @Inject() (val messagesApi: MessagesApi, dao: NotificationDAO) extends Controller with I18nSupport {

  def getNotify = Action.async {
    dao.getNotificationsSent().flatMap(
      notificationsSent => dao.getNotificationsSentNotYet().map(
        notificationsSentNotYet => Ok(views.html.notifications("", notificationsSent, notificationsSentNotYet, Forms.notificationForm))
      )
    )
  }

  def postNotify = Action.async { implicit request =>
    Forms.notificationForm.bindFromRequest.fold(
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

}
