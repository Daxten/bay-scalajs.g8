import play.api.ApplicationLoader.Context
import play.api._

/**
  * Application loader that wires up the application dependencies using Macwire
  */
class CustomApplicationLoader extends ApplicationLoader {
  def load(context: Context): Application = new ApplicationComponents(context).application
}
