package bay.driver

import com.github.tminglei.slickpg._

object CustomizedPgDriver extends CustomizedPgDriver

trait CustomizedPgDriver
    extends ExPostgresDriver
    with PgArraySupport
    with PgDateSupport2bp
    with PgEnumSupport
    with PgRangeSupport
    with PgHStoreSupport
    with PgSearchSupport
    with NewPgCirceJsonSupport {

  override val api = new MyAPI {}

  trait MyAPI
      extends API
      with ArrayImplicits
      with RangeImplicits
      with HStoreImplicits
      with SearchImplicits
      with SearchAssistants
      with DateTimeImplicits
      with JsonImplicits

  override def pgjson: String = "jsonb"
}
