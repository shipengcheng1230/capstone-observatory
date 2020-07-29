package observatory

import com.sksamuel.scrimage.{Image, Pixel}
import observatory.Interaction.tileLocation
import observatory.Visualization.interpolateColor
import scala.math._

/**
  * 5th milestone: value-added information visualization
  */
object Visualization2 extends Visualization2Interface {

  /**
    * @param point (x, y) coordinates of a point in the grid cell
    * @param d00 Top-left value
    * @param d01 Bottom-left value
    * @param d10 Top-right value
    * @param d11 Bottom-right value
    * @return A guess of the value at (x, y) based on the four known values, using bilinear interpolation
    *         See https://en.wikipedia.org/wiki/Bilinear_interpolation#Unit_Square
    */
  def bilinearInterpolation(
    point: CellPoint,
    d00: Temperature,
    d01: Temperature,
    d10: Temperature,
    d11: Temperature
  ): Temperature = {
    d00 * (1 - point.x) * (1 - point.y) +
      d10 * point.x * (1 - point.y) + d01 * (1 - point.x) * point.y +
      d11 * point.x * point.y
  }

  /**
    * @param grid Grid to visualize
    * @param colors Color scale to use
    * @param tile Tile coordinates to visualize
    * @return The image of the tile at (x, y, zoom) showing the grid using the given color scale
    */
  def visualizeGrid(
    grid: GridLocation => Temperature,
    colors: Iterable[(Temperature, Color)],
    tile: Tile
  ): Image = {

    val gs = for {
      y <- 0 until imageHeight
      x <- 0 until imageWidth
    } yield (x, y)

    val px = gs.par.map(
      g => Tile(imageWidth * tile.x + g._1, imageHeight * tile.y + g._2, tile.zoom + 8)
    )
      .map(tileLocation)
      .map(interpolateTemperature(grid, _))
      .map(interpolateColor(colors, _))
      .map(c => Pixel(c.red, c.green, c.blue, 127))
      .toArray
    Image(imageWidth, imageHeight, px)
  }

  private def interpolateTemperature(grid: GridLocation => Temperature, location: Location): Temperature = {
    val p0x = floor(location.lon).toInt
    val p0y = floor(location.lat).toInt
    val p1x = ceil(location.lon).toInt
    val p1y = ceil(location.lat).toInt
    val px = location.lon - p0x
    val py = location.lat - p0y
    val List(d00, d01, d10, d11) = List(
      GridLocation(p0y, p0x), GridLocation(p0y, p1x),
      GridLocation(p1y, p0x), GridLocation(p1y, p1x),
    ).map(grid)
    bilinearInterpolation(CellPoint(py, px), d00, d01, d10, d11)
  }

  private val imageWidth = 256
  private val imageHeight = 256

}
