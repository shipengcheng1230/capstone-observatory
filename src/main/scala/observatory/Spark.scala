package observatory

import org.apache.spark.sql.SparkSession

object Spark {

  private[observatory] lazy val spark: SparkSession = SparkSession
    .builder()
    .appName("observatory")
    .master("local[*]")
    .getOrCreate()
}
