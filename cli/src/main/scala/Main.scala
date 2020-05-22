package com.azavea.geostats

import geotrellis.vector._
import com.monovore.decline._
import geotrellis.proj4.{LatLng, WebMercator}

import squants.space.{Meters, SquareMeters, SquareFeet, Feet}
import squants.space.SquareKilometers
import squants.space.SquareUsMiles
import squants.space.Kilometers
import squants.space.UsMiles
import scala.math.BigDecimal

object HelloWorld
    extends CommandApp(
  name = "geojson-stats",
      header = "Calculate statistics on geojson",
      main = (Commands.calculateOpts).map({
        case Commands.CalculateOptions(statOption, polygons, metric, imperial) => {
          val webMercatorPolygons = polygons.map { p =>
            Projected(p, 4326).reproject(LatLng, WebMercator)(3857)
          }

          statOption match {
            case AreaStat => {
              val combinedArea = SquareMeters(
                BigDecimal(webMercatorPolygons.map(_.geom.area).reduceLeft(_ + _)).setScale(2, BigDecimal.RoundingMode.HALF_UP).toDouble)
              if (metric) {
                val humanSize = if (combinedArea.value > 900) combinedArea in SquareKilometers else combinedArea
                println(s"Combined area is $humanSize in metric units")
              }
              if (imperial) {
                val imperialArea = combinedArea in SquareFeet
                val humanSize = if (imperialArea.value > 4000) imperialArea in SquareUsMiles else combinedArea
                println(s"Combined area is $humanSize in imperial units")
              }
            }

            case LengthStat => {
              val combinedLength = Meters(
                                BigDecimal(webMercatorPolygons.map(_.geom.perimeter).reduceLeft(_ + _)).setScale(2, BigDecimal.RoundingMode.HALF_UP).toDouble)
              if (metric) {
                val humanSize = if (combinedLength.value > 900) combinedLength in Kilometers else combinedLength
                println(s"Combined perimeter is $humanSize in metric units")
              }
              if (imperial) {
                val imperialLength = combinedLength in Feet
                val humanSize = if (imperialLength.value > 4000) imperialLength in UsMiles else imperialLength
                println(s"Combined perimeter is $humanSize in imperial units")
              }
            }
          }
        }})
)
