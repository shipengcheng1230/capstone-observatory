package observatory

import com.sksamuel.scrimage.{Image, Pixel}
import scala.math.pow

/**
  * 2nd milestone: basic visualization
  */
object Visualization extends VisualizationInterface {

  import observatory.implicits._

  /**
    * @param temperatures Known temperatures: pairs containing a location and the temperature at this location
    * @param location Location where to predict the temperature
    * @return The predicted temperature at `location`
    */
  def predictTemperature(temperatures: Iterable[(Location, Temperature)], location: Location): Temperature = {
    temperatures.find(x => x._1 ~= location).map(_._2)
      .getOrElse(inverseDistanceWeighting(2.0)(temperatures, location))
  }

  private def inverseDistanceWeighting(p: Double)
                              (temperatures: Iterable[(Location, Temperature)], location: Location): Temperature = {
    val result = temperatures.map(
      x => {
        val weight = 1.0 / pow(x._1.distance(location), p)
        (weight * x._2, weight)
      }
    ).reduce((a, b) => (a._1 + b._1, a._2 + b._2))
    result._1 / result._2
  }

  /**
    * @param points Pairs containing a value and its associated color
    * @param value The value to interpolate
    * @return The color that corresponds to `value`, according to the color scale defined by `points`
    */
  def interpolateColor(points: Iterable[(Temperature, Color)], value: Temperature): Color = {
    val pts = points.toSeq.sortBy(_._1)
    pts.indexWhere(_._1 >= value) match {
      case -1 => interpolateRGB(pts.init.last, pts.last, value)
      case 0  => interpolateRGB(pts.head, pts.tail.head, value)
      case x  => interpolateRGB(pts(x-1), pts(x), value)
    }
  }

  private def interpolateRGB(p1: (Temperature, Color), p2: (Temperature, Color), value: Temperature): Color = {
    Color(
      linearInterpolate(p1._1, p1._2.red, p2._1, p2._2.red, value).clamp256,
      linearInterpolate(p1._1, p1._2.green, p2._1, p2._2.green, value).clamp256,
      linearInterpolate(p1._1, p1._2.blue, p2._1, p2._2.blue, value).clamp256,
    )
  }

  private def linearInterpolate(x1: Double, y1: Int, x2: Double, y2: Int, x: Temperature): Double =
    y1 + (x - x1) * (y2 - y1) / (x2 - x1)

  /**
    * @param temperatures Known temperatures
    * @param colors Color scale
    * @return A 360×180 image where each pixel shows the predicted temperature at its location
    */
  def visualize(temperatures: Iterable[(Location, Temperature)], colors: Iterable[(Temperature, Color)]): Image = {
    Image(mapRow, mapCol,
      (0 until mapRow * mapCol).par
        .map(index2location)
        .map(loc => predictTemperature(temperatures, loc))
        .map(temp => interpolateColor(colors, temp))
        .map(c => Pixel(c.red, c.green, c.blue, 255))
        .toArray
    )
  }

  private val mapRow = 360
  private val mapCol = 180

  private def index2location(i: Int): Location = Location(90 - i / mapRow, i % mapRow - 180)

}

