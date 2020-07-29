package observatory

import scala.collection.concurrent.TrieMap
import observatory.Visualization.predictTemperature

/**
  * 4th milestone: value-added information
  */
object Manipulation extends ManipulationInterface {
  /**
    * @param temperatures Known temperatures
    * @return A function that, given a latitude in [-89, 90] and a longitude in [-180, 179],
    *         returns the predicted temperature at this location
    */
  def makeGrid(temperatures: Iterable[(Location, Temperature)]): GridLocation => Temperature = {
    val cache = new TrieMap[GridLocation, Temperature]

    (x: GridLocation) => {
      cache.getOrElseUpdate(x, predictTemperature(temperatures, Location(x.lat, x.lon)))
      cache(x)
    }
  }

  /**
    * @param temperaturess Sequence of known temperatures over the years (each element of the collection
    *                      is a collection of pairs of location and temperature)
    * @return A function that, given a latitude and a longitude, returns the average temperature at this location
    */
  def average(temperaturess: Iterable[Iterable[(Location, Temperature)]]): GridLocation => Temperature = {
    makeGrid(
      temperaturess.flatten.groupBy(_._1).mapValues(_.map(_._2)).map {
        case (loc, temps) => (loc, temps.sum / temps.size)
      })
  }

  /**
    * @param temperatures Known temperatures
    * @param normals A grid containing the “normal” temperatures
    * @return A grid containing the deviations compared to the normal temperatures
    */
  def deviation(temperatures: Iterable[(Location, Temperature)], normals: GridLocation => Temperature): GridLocation => Temperature = {
    val f = makeGrid(temperatures)
    x: GridLocation => f(x) - normals(x)
  }

}

