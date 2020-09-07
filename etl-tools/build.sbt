// NOTE sets scalaVersion for all subprojects too
// keeping consistent with play app version for now
val cassandraJavaDriverVersion = "4.9.0"
val dataUtilsVersion = sys.env.get("DATA_UTILS_VERSION")

ThisBuild / scalaVersion := "2.13.1"
ThisBuild / organization := "com.ryanquey"
ThisBuild / organizationName := "ryanquey"
name := "intertextuality-graph-etl-tools"
version := "0.1.0"


// allow my projects which are installed to local mvn work here. Should work after running packaging scripts in root of this git repo
// https://stackoverflow.com/a/21628869/6952495
resolvers += Resolver.mavenLocal

// https://github.com/scalaj/scalaj-http
libraryDependencies +=  "org.scalaj" %% "scalaj-http" % "2.4.2"

// https://mvnrepository.com/artifact/com.google.inject/guice
libraryDependencies += "com.google.inject" % "guice" % "4.2.3"

// https://mvnrepository.com/artifact/org.apache.commons/commons-csv
libraryDependencies += "org.apache.commons" % "commons-csv" % "1.8"




libraryDependencies ++= Seq( 
  // required for datastax java driver to work, due to compatibility issues
  // https://mvnrepository.com/artifact/com.fasterxml.jackson.module/jackson-module-scala
  "com.fasterxml.jackson.module" %% "jackson-module-scala" % "2.11.0",
  "com.datastax.oss" % "java-driver-core" % cassandraJavaDriverVersion,
  "com.datastax.oss" % "java-driver-query-builder" % cassandraJavaDriverVersion,
  "com.datastax.oss" % "java-driver-mapper-runtime" % cassandraJavaDriverVersion,
  "org.scala-lang" % "scala-reflect" % scalaVersion.value
  )

// local projects
libraryDependencies +=  "com.ryanquey.intertextuality-graph" % "models" % "0.1.0"

libraryDependencies +=  "com.ryanquey" % "data-utils" % dataUtilsVersion.get

