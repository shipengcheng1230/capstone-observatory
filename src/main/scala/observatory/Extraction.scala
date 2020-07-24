package observatory

import java.time.LocalDate

import scala.io.Source
import org.apache.spark._
import org.apache.spark.sql.SparkSession

/**
  * 1st milestone: data extraction
  */
object Extraction extends ExtractionInterface {

  val spark = SparkSession
    .builder()
    .appName("observatory")
    .master("local[4]")
    .getOrCreate()

  import spark.implicits._

  /**
    * @param year             Year number
    * @param stationsFile     Path of the stations resource file to use (e.g. "/stations.csv")
    * @param temperaturesFile Path of the temperatures resource file to use (e.g. "/1975.csv")
    * @return A sequence containing triplets (date, location, temperature)
    */
  def locateTemperatures(year: Year, stationsFile: String, temperaturesFile: String): Iterable[(LocalDate, Location, Temperature)] = {
    val fsStation = Source.fromInputStream(getClass.getResourceAsStream(stationsFile), "utf-8")
    val fsTemp = Source.fromInputStream(getClass.getResourceAsStream(temperaturesFile), "utf-8")
    ???
  }

  /**
    * @param records A sequence containing triplets (date, location, temperature)
    * @return A sequence containing, for each location, the average temperature over the year.
    */
  def locationYearlyAverageRecords(records: Iterable[(LocalDate, Location, Temperature)]): Iterable[(Location, Temperature)] = {
    ???
  }

  def fahrenheit2celsius(x: Double): Temperature = (x - 32) * 5 / 9

}
