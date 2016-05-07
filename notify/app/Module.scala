import com.google.inject.AbstractModule

import services.NotifyTimer

class Module extends AbstractModule {

  override def configure() = {
    bind(classOf[NotifyTimer]).asEagerSingleton()
  }

}
