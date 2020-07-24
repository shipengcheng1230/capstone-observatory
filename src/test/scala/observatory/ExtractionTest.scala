package observatory

import java.time.LocalDate

import org.junit.Assert._
import org.junit.Test
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest._

trait ExtractionTest extends AnyFlatSpec with MilestoneSuite {
  private val milestoneTest = namedMilestoneTest("data extraction", 1) _

  // Implement tests for the methods of the `Extraction` object
  ignore should "work locally" in {
    val result = Extraction.locateTemperatures(1975, "/stations.csv", "/1975.csv")
    assert(result.nonEmpty)
  }

  "`locationYearlyAverageRecords`" should "work!" in {
    val loc1: Location = Location(1.0, 1.0)
    val loc2: Location = Location(2.0, 2.0)
    // dummy data
    val records: scala.Iterable[(LocalDate, Location, Double)] = Seq(
      (LocalDate.of(2000, 1, 1), loc1, 1.0),
      (LocalDate.of(2000, 1, 2), loc1, 2.0),
      (LocalDate.of(2000, 1, 3), loc1, 3.0),
      (LocalDate.of(2001, 1, 1), loc2, 4.0),
      (LocalDate.of(2001, 1, 2), loc2, 5.0),
      (LocalDate.of(2001, 1, 3), loc2, 6.0),
    )
    val result = Extraction.locationYearlyAverageRecords(records)
    assert(result.nonEmpty)
    assertResult(2)(result.size)
    assertResult(2.0)(result.toSeq.filter(_._1 == loc1).head._2)
    assertResult(5.0)(result.toSeq.filter(_._1 == loc2).head._2)
  }
}

