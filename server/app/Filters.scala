import play.api.http.DefaultHttpFilters
import play.filters.cors.CORSFilter
import play.filters.gzip.GzipFilter

class Filters(corsFilter: CORSFilter, gzipFilter: GzipFilter)
    extends DefaultHttpFilters(corsFilter, gzipFilter)
