package observatory

import java.nio.file.Paths
import java.time.LocalDate

import org.apache.spark.sql.{Encoder, Encoders, SparkSession}
import org.apache.spark.sql.functions._

import scala.io.Source

/**
  * 1st milestone: data extraction
  */
object Extraction extends ExtractionInterface {
  import observatory.Spark._
  import spark.implicits._
  import observatory.implicits._

  private val stationSchema = Encoders.product[Station].schema
  private val temperatureRecordSchema = Encoders.product[TemperatureRecord].schema

  /**
    * @param year             Year number
    * @param stationsFile     Path of the stations resource file to use (e.g. "/stations.csv")
    * @param temperaturesFile Path of the temperatures resource file to use (e.g. "/1975.csv")
    * @return A sequence containing triplets (date, location, temperature)
    */
  def locateTemperatures(year: Year, stationsFile: String, temperaturesFile: String): Iterable[(LocalDate, Location, Temperature)] = {
    // use DataSet

    val dfStation = spark.read
      .option("header", value = false)
      .schema(stationSchema)
      // cannot read abspath from jar
      .csv(fileToDS(stationsFile))
      .as[Station]
      .filter(s => s.lat.isDefined && s.lon.isDefined)

    val dfTemp = spark.read
      .option("header", value = false)
      .schema(temperatureRecordSchema)
      .csv(fileToDS(temperaturesFile))
      .as[TemperatureRecord]
      .filter(r => r.temp != 9999.9)

    dfStation.joinWith(dfTemp,
      dfStation("stn").eqNullSafe(dfTemp("stn")) && dfStation("wban").eqNullSafe(dfTemp("wban"))
    ).map(r => (
      LocalDate.of(year, r._2.month, r._2.day),
      Location(r._1.lat.get, r._1.lon.get),
      r._2.temp.toCelsius
    )).collect()
  }

  private def fileToDS(file: String) =
    spark.sparkContext.parallelize(
      Source.fromInputStream(getClass.getResourceAsStream(file)).getLines.toSeq
    ).toDS

  /**
    * @param records A sequence containing triplets (date, location, temperature)
    * @return A sequence containing, for each location, the average temperature over the year.
    */
  def locationYearlyAverageRecords(records: Iterable[(LocalDate, Location, Temperature)]): Iterable[(Location, Temperature)] = {
    spark.sparkContext.parallelize(records.toSeq)
      .map(r => (r._1.getYear, r._2, r._3))
      .toDF("year", "loc", "temp")
      .groupBy($"year", $"loc")
      .agg($"year", $"loc", avg($"temp").as("temp"))
      .select($"loc".as[Location], $"temp".as[Temperature])
      .collect()
  }

}
