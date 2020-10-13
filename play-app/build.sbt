val cassandraJavaDriverVersion = "4.9.0"
val dataUtilsVersion = sys.env.get("DATA_UTILS_VERSION")
val modelsVersion = sys.env.get("INTERTEXTUALITY_GRAPH_MODELS_VERSION")

// TODO base name and version on env vars, which we pass in on build (see start/build scripts)

// TODO try this one for autoreload
// Global / onChangedBuildSource := ReloadOnSourceChanges

lazy val sampleTask = taskKey[Unit]("Prints 'Hello World'")

lazy val intertextualityPlayApi = (project in file("."))
  .enablePlugins(PlayScala)
  .settings(
    name := """intertextuality-graph-play-api""",
    maintainer := "quey.ryan@gmail.org",
    organization := "com.ryanquey",
    version := "1.0-SNAPSHOT",
    scalaVersion := "2.13.1",

    // https://www.scala-sbt.org/1.x/docs/Tasks.html
    sampleTask := {
        val sum = 1 + 2
        println("got env var: "+ dataUtilsVersion)
    },

    // allow my projects which are installed to local mvn work here. Should work after running packaging scripts in root of this git repo
    // https://stackoverflow.com/a/21628869/6952495
    resolvers += Resolver.mavenLocal,
    libraryDependencies ++= Seq(
      guice,
      
      "org.scalatestplus.play" %% "scalatestplus-play" % "5.0.0" % Test,
      // required for datastax java driver to work, due to compatibility issues
      // https://mvnrepository.com/artifact/com.fasterxml.jackson.module/jackson-module-scala
      
     // Avoid this error that comes up when adding the cassandra driver by adding a specific Jackson version: com.fasterxml.jackson.databind.JsonMappingException: Scala module 2.10.3 requires Jackson Databind version >= 2.10.0 and < 2.11.0
      "com.fasterxml.jackson.module" %% "jackson-module-scala" % "2.11.0",
      "com.datastax.oss" % "java-driver-core" % cassandraJavaDriverVersion,
      "com.datastax.oss" % "java-driver-query-builder" % cassandraJavaDriverVersion,
      "com.datastax.oss" % "java-driver-mapper-runtime" % cassandraJavaDriverVersion,

      "com.michaelpollmeier" %% "gremlin-scala" % "3.4.7.2",

      // "com.ryanquey.intertextuality-graph" % "models" % modelsVersion.get,
      // for now just putting the jar in the libs dir, rather than installing from local mvn repo
      // but maybe jar is better
      // "com.ryanquey" % "data-utils" % dataUtilsVersion.get,

      // if this doesn't work, make sure to run ./scripts/sbt/sbt.sh publishLocal from the etl-tools dir
      // play app will only see changes made that get published using publishLocal
      // NOTE now trying to put jar in source control, for easy deploys to Heroku
      // "com.ryanquey" %% "intertextuality-graph-etl-tools" % "0.1.0-SNAPSHOT"
    ),
    scalacOptions ++= Seq(
      "-feature",
      "-deprecation",
      "-Xfatal-warnings"
    )
  )


// https://www.playframework.com/documentation/2.8.x/sbtSubProjects
// lazy val dataUtils = (project in file("lib/data-utils-for-java"))
