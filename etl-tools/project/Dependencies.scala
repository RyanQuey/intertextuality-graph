import sbt._

object Dependencies {
  val cassandraJavaDriverVersion = "4.9.0"
  val scalaVersion = "2.13.1"

  lazy val javaDriverMapperRuntime = "com.datastax.oss" % "java-driver-mapper-runtime" % cassandraJavaDriverVersion
  lazy val javaDriverMapperProcessor = "com.datastax.oss" % "java-driver-mapper-processor" % cassandraJavaDriverVersion
  lazy val javaDriverCore = "com.datastax.oss" % "java-driver-core" % cassandraJavaDriverVersion
  lazy val javaDriverQueryBuilder = "com.datastax.oss" % "java-driver-query-builder" % cassandraJavaDriverVersion
  // https://github.com/scalaj/scalaj-http
  lazy val http = "org.scalaj" %% "scalaj-http" % "2.4.2"

  // https://mvnrepository.com/artifact/com.google.inject/guice
  lazy val guice = "com.google.inject" % "guice" % "4.2.3"

  // https://mvnrepository.com/artifact/org.apache.commons/commons-csv
  lazy val apacheCommons = "org.apache.commons" % "commons-csv" % "1.8"

  // required for datastax java driver to work, due to compatibility issues
  // https://mvnrepository.com/artifact/com.fasterxml.jackson.module/jackson-module-scala
  lazy val jackson = "com.fasterxml.jackson.module" %% "jackson-module-scala" % "2.11.0"
	lazy val scalaReflect = "org.scala-lang" % "scala-reflect" % scalaVersion
}
