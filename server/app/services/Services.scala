package services

import play.api.Configuration
import play.api.i18n.MessagesApi
import services.dao.UserDao

class Services(
    val userDao: UserDao,
    val configuration: Configuration,
    val messagesApi: MessagesApi
)
