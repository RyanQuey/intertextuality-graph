val cassandraJavaDriverVersion = "4.8.0"
// TODO base name and version on env vars, which we pass in on build (see start/build scripts)

lazy val intertextualityPlayApi = (project in file("."))
  .enablePlugins(PlayScala)
  .settings(
    name := """intertextuality-graph-play-api""",
    maintainer := "quey.ryan@gmail.org",
    organization := "com.ryanquey",
    version := "1.0-SNAPSHOT",
    scalaVersion := "2.13.1",
    libraryDependencies ++= Seq(
      guice,
      "org.scalatestplus.play" %% "scalatestplus-play" % "5.0.0" % Test,
      // https://mvnrepository.com/artifact/com.fasterxml.jackson.module/jackson-module-scala
      "com.fasterxml.jackson.module" %% "jackson-module-scala" % "2.11.0",
      "com.datastax.oss" % "java-driver-core" % cassandraJavaDriverVersion,
      "com.datastax.oss" % "java-driver-query-builder" % cassandraJavaDriverVersion,
      "com.datastax.oss" % "java-driver-mapper-runtime" % cassandraJavaDriverVersion,
    ),
    scalacOptions ++= Seq(
      "-feature",
      "-deprecation",
      "-Xfatal-warnings"
    )
  )


// https://www.playframework.com/documentation/2.8.x/sbtSubProjects
// lazy val dataUtils = (project in file("lib/data-utils-for-java"))
