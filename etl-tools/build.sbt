// NOTE sets scalaVersion for all subprojects too
// keeping consistent with play app version for now
val cassandraJavaDriverVersion = "4.8.0"
ThisBuild / scalaVersion := "2.13.1"
name := "intertextuality-graph-etl-tools"
version := "0.1.0"

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
