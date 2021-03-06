// NOTE sets scalaVersion for all subprojects too
// keeping consistent with play app version for now
val cassandraJavaDriverVersion = "4.9.0"
val dataUtilsVersion = sys.env.get("DATA_UTILS_VERSION")
val modelsVersion = sys.env.get("INTERTEXTUALITY_GRAPH_MODELS_VERSION")

ThisBuild / scalaVersion := "2.13.1"
ThisBuild / organization := "com.ryanquey"
ThisBuild / organizationName := "ryanquey"
name := "intertextuality-graph-etl-tools"
version := "0.1.0-SNAPSHOT"


// allow my other projects which are installed to local mvn work here. Should work after running packaging scripts in root of this git repo
// https://stackoverflow.com/a/21628869/6952495
resolvers += Resolver.mavenLocal

// https://github.com/milessabin/shapeless
resolvers ++= Seq(
  Resolver.sonatypeRepo("releases"),
  Resolver.sonatypeRepo("snapshots")

)

// I want to parse osis refs. 
// This library helps me do it: https://github.com/crosswire/jsword/blob/master/src/main/java/org/crosswire/jsword/passage/OsisParser.java#L51
// why am not just using their mvn repo: https://github.com/crosswire/jsword/issues/113

// option#1 (haven't tried yet): https://github.com/stupenrose/jsword-mvn
// Approach using sbt instead of mvn: https://www.scala-sbt.org/1.x/docs/Resolvers.html#URL

// Option#2: https://github.com/crosswire/jsword/issues/113#issuecomment-705641632
// https://jitpack.io/#AndBible/jsword/2.3.10

resolvers += "jitpack" at "https://jitpack.io"
libraryDependencies += "com.github.AndBible" % "jsword" % "2.3.10" exclude("de.psdev.slf4j-android-logger", "slf4j-android-logger") 


libraryDependencies +=  "org.scalaj" %% "scalaj-http" % "2.4.2"

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
  "org.scala-lang" % "scala-reflect" % scalaVersion.value,
  "com.michaelpollmeier" %% "gremlin-scala" % "3.4.7.2",
  "com.chuusai" %% "shapeless" % "2.3.3",

  )

// local projects
// requires .get to force it to not be an option, but an actual String
libraryDependencies +=  "com.ryanquey.intertextuality-graph" % "models" % modelsVersion.get

// our _build-data-utils-jar.sh should install to maven as well as put a jar in hte play app's lib dir
libraryDependencies +=  "com.ryanquey" % "data-utils" % dataUtilsVersion.get

