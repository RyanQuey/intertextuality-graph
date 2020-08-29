// NOTE sets scalaVersion for all subprojects too
// keeping consistent with play app version for now
ThisBuild / scalaVersion := "2.13.1"
name := "intertextuality-graph-etl-tools"
version := "0.1.0"

// https://github.com/scalaj/scalaj-http
libraryDependencies +=  "org.scalaj" %% "scalaj-http" % "2.4.2"

