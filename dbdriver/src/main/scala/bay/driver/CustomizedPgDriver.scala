package bay.driver

import com.github.tminglei.slickpg._
import com.github.tminglei.slickpg.str.PgStringExtensions
import com.github.tminglei.slickpg.str.PgStringSupport

object CustomizedPgDriver extends CustomizedPgDriver

trait CustomizedPgDriver
    extends ExPostgresProfile
    with PgArraySupport
    with PgDate2Support
    with PgEnumSupport
    with PgRangeSupport
    with PgHStoreSupport
    with PgSearchSupport
    with PgCirceJsonSupport
    with PgStringSupport {

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
      with PgStringImplicits

  override def pgjson: String = "jsonb"
}
