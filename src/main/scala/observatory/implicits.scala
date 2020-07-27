package observatory

import org.apache.spark.sql.{Encoder, Encoders}

import scala.reflect.ClassTag
import scala.math.{min, max, round}

object implicits {
  // https://stackoverflow.com/a/39442829/8697614
  implicit def kryoEncoder[A](implicit ct: ClassTag[A]): Encoder[A] =
    org.apache.spark.sql.Encoders.kryo[A](ct)

  implicit def tuple3[A1, A2, A3](implicit e1: Encoder[A1],
                                  e2: Encoder[A2],
                                  e3: Encoder[A3]
                                 ): Encoder[(A1, A2, A3)] =
    Encoders.tuple[A1, A2, A3](e1, e2, e3)

  implicit class Fahrenheit2Celsius(x: Temperature) {
    def toCelsius: Temperature = (x - 32) * 5 / 9
  }

  implicit class Clamp256(x: Double) {
    def clamp256: Int = max(0, min(255, round(x).toInt))
  }
}
