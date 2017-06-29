// scalafmt: { maxColumn = 160, align.tokens = ["="] }

import akka.actor.ActorSystem
import bay.driver.CustomizedPgDriver
import com.softwaremill.macwire._
import controllers.Application
import controllers.AssetsComponents
import controllers.Security
import play.api.ApplicationLoader.Context
import play.api.BuiltInComponentsFromContext
import play.api.LoggerConfigurator
import play.api.db.DBComponents
import play.api.db.HikariCPComponents
import play.api.db.slick.DbName
import play.api.db.slick.SlickComponents
import play.api.i18n.I18nComponents
import play.api.mvc.BodyParsers
import play.api.mvc.EssentialFilter
import play.api.mvc.PlayBodyParsers
import play.api.routing.Router
import play.filters.cors.CORSConfig
import play.filters.cors.CORSFilter
import play.filters.gzip.GzipFilter
import play.filters.gzip.GzipFilterConfig
import router.Routes
import services.Services
import services.dao.UserDao
import slick.basic.DatabaseConfig

import scala.concurrent.ExecutionContext

class ApplicationComponents(context: Context)
    extends BuiltInComponentsFromContext(context)
    with I18nComponents
    with DBComponents
    with HikariCPComponents
    with SlickComponents
    with AssetsComponents {

  /*
   * Engine
   */
  implicit val ec: ExecutionContext = scala.concurrent.ExecutionContext.Implicits.global
  LoggerConfigurator(context.environment.classLoader).foreach {
    _.configure(context.environment)
  }
  val bodyParsers: BodyParsers.Default = wire[BodyParsers.Default]

  /*
   * Filter
   */
  lazy val gzipFilterConfig: GzipFilterConfig         = GzipFilterConfig.fromConfiguration(configuration)
  lazy val gzipFilter: GzipFilter                     = wire[GzipFilter]
  lazy val corsConfig: CORSConfig                     = CORSConfig.fromConfiguration(configuration)
  lazy val corsFilter: CORSFilter                     = new CORSFilter(corsConfig)
  override lazy val httpFilters: Seq[EssentialFilter] = wireSet[EssentialFilter].toSeq

  /*
   * SERVICES
   */
  lazy val dbConfig: DatabaseConfig[CustomizedPgDriver] = slickApi.dbConfig[CustomizedPgDriver](DbName("default"))
  lazy val userDao: UserDao                             = wire[UserDao]
  lazy val services: Services                           = wire[Services]

  /*
   * CONTROLLER
   */
  lazy val security: Security         = wire[Security]
  lazy val appController: Application = wire[Application]

  /*
   * ROUTES
   */
  lazy val router: Router = {
    // add the prefix string in local scope for the Routes constructor
    val prefix: String = "/"
    wire[Routes]
  }
}
