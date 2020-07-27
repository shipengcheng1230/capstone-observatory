package observatory

import com.sksamuel.scrimage.{Image, Pixel}
import scala.math._
import observatory.Visualization.{interpolateColor, predictTemperature}

/**
  * 3rd milestone: interactive visualization
  */
object Interaction extends InteractionInterface {

  /**
    * @param tile Tile coordinates
    * @return The latitude and longitude of the top-left corner of the tile, as per http://wiki.openstreetmap.org/wiki/Slippy_map_tilenames
    */
  def tileLocation(tile: Tile): Location = {
    val n = pow(2, tile.zoom)
    val lon = tile.x / n * 360.0 - 180.0
    val lat = toDegrees(atan(sinh(Pi * (1.0 - 2.0 * tile.y / n))))
    Location(lat, lon)
  }

  /**
    * @param temperatures Known temperatures
    * @param colors Color scale
    * @param tile Tile coordinates
    * @return A 256×256 image showing the contents of the given tile
    */
  def tile(temperatures: Iterable[(Location, Temperature)], colors: Iterable[(Temperature, Color)], tile: Tile): Image = {
    val px = for {
      y <- 0 until tileY
      x <- 0 until tileX
    } yield (x, y)

    val im = px.par.map
      { case (x, y) => Tile(256 * tile.x + x, 256 * tile.y + y, tile.zoom + 8) }
        .map(tileLocation)
        .map(predictTemperature(temperatures, _))
        .map(interpolateColor(colors, _))
        .map(c => Pixel(c.red, c.green, c.blue, 127))
        .toArray

    Image(tileX, tileY, im)
  }

  private val tileX = 256
  private val tileY = 256

  /**
    * Generates all the tiles for zoom levels 0 to 3 (included), for all the given years.
    * @param yearlyData Sequence of (year, data), where `data` is some data associated with
    *                   `year`. The type of `data` can be anything.
    * @param generateImage Function that generates an image given a year, a zoom level, the x and
    *                      y coordinates of the tile and the data to build the image from
    */
  def generateTiles[Data](
    yearlyData: Iterable[(Year, Data)],
    generateImage: (Year, Tile, Data) => Unit
  ): Unit = {
    for {
      (year, data) <- yearlyData
      zoom <- 0 to 3
      x <- 0 until 1 << zoom
      y <- 0 until 1 << zoom
    } generateImage(year, Tile(x, y, zoom), data)
  }

}
