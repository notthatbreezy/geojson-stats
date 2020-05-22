// give the user a nice default project!
ThisBuild / organization := "com.azavea"
ThisBuild / scalaVersion := "2.12.10"

val BetterFilesVersion   = "3.8.0"
val DeclineVersion       = "1.0.0"
val RasterFoundryVersion = "1.40.3"
val SttpVersion          = "2.0.7"

val cliDependencies = List(
  "com.github.pathikrit"  %% "better-files"                   % BetterFilesVersion,
  "com.monovore"                 %% "decline"                        % DeclineVersion,
  "com.monovore"                 %% "decline-effect"                 % DeclineVersion,
  "com.rasterfoundry"            %% "datamodel"                      % RasterFoundryVersion,
  "com.softwaremill.sttp.client" %% "async-http-client-backend-cats" % SttpVersion,
  "com.softwaremill.sttp.client" %% "circe"                          % SttpVersion,
  "com.softwaremill.sttp.client" %% "core"                           % SttpVersion,
  "org.typelevel"  %% "squants"  % "1.3.0"
)

lazy val cli = (project in file("./cli"))
  .settings(
    libraryDependencies ++= cliDependencies,
    externalResolvers ++= Seq(
      DefaultMavenRepository,
      Resolver.sonatypeRepo("snapshots"),
      Resolver.bintrayRepo("azavea", "maven"),
      Resolver.bintrayRepo("azavea", "geotrellis"),
      "locationtech-releases" at "https://repo.locationtech.org/content/groups/releases",
      "locationtech-snapshots" at "https://repo.locationtech.org/content/groups/snapshots",
      Resolver.file("local", file(Path.userHome.absolutePath + "/.ivy2/local"))(
        Resolver.ivyStylePatterns
      )
    ),
    outputStrategy := Some(StdoutOutput),
    assemblyMergeStrategy in assembly := {
      case "reference.conf"                       => MergeStrategy.concat
      case "application.conf"                     => MergeStrategy.concat
      case n if n.startsWith("META-INF/services") => MergeStrategy.concat
      case n if n.endsWith(".SF") || n.endsWith(".RSA") || n.endsWith(".DSA") =>
        MergeStrategy.discard
      case "META-INF/MANIFEST.MF" => MergeStrategy.discard
      case _                      => MergeStrategy.first
    }
  )
