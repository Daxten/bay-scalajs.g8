package services

import com.google.inject.Inject
import com.google.inject.Singleton
import play.api.Configuration
import play.api.i18n.MessagesApi
import services.dao.UserDao

@Singleton
class Services @Inject()(
    val userDao: UserDao,
    val configuration: Configuration,
    val messagesApi: MessagesApi
) {}
