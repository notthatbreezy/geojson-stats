package com.azavea.geostats

import cats.implicits._
import better.files._
import com.monovore.decline._
import geotrellis.vector.Polygon
import geotrellis.vector.io.json._
import geotrellis.vector.io.json.Implicits._
import cats.data._

sealed trait StatType
case object AreaStat extends StatType
case object LengthStat extends StatType

import scala.io.Source


object Commands {

  private val statOption = Opts.argument[String]("statType").mapValidated { s =>
    s match {
      case "area" => Validated.valid(AreaStat)
      case "length" => Validated.valid(LengthStat)
      case _ => Validated.invalidNel("Stat type must be either 'area' or 'length'")
    }
  }

  private val metricOpt = Opts.flag("metric", help = "Whether to return results in metric units.").orFalse

  private val imperialOpt = Opts.flag("imperial", help = "Whether to return results in imperial units").orFalse

  private val geoJsonOpt: Opts[List[Polygon]] = Opts
    .argument[String]("INPUT_FILE")
    .mapValidated { string =>

      val geojsonString = if (string.startsWith("http")) {
        val html = Source.fromURL(string)
        Some(html.mkString)
      } else {
        val f = string.toFile
        if (f.exists) Some(f.contentAsString) else None
      }
      geojsonString match {
        case Some(s) => {
          val featureCollection = s.parseGeoJson[JsonFeatureCollection]
          val polygons = featureCollection.getAllPolygons().toList
          val multiPolygons = featureCollection.getAllMultiPolygons().toList
          val combined = multiPolygons.flatMap(_.polygons) ++ polygons
            (combined.isEmpty) match {
            case false => Validated.valid(combined)
            case _ => Validated.invalidNel(s"No polygons or multipolygons present in file: $string")
          }
        }
        case _ => Validated.invalidNel(s"No GeoJSON to read at path: $string")
      }
    }

  case class CalculateOptions(
    statOption: StatType,
    input: List[Polygon],
    metric: Boolean,
    imperial: Boolean
  )

  val calculateOpts: Opts[CalculateOptions] =
    (statOption, geoJsonOpt, metricOpt, imperialOpt).mapN(CalculateOptions)

  val geoCommand = Command(
    name = "geojson-stats",
    header = "Calculate statistics on geojson"
  ) {
    calculateOpts
  }
}
