lazy val root = (project in file("."))
  .enablePlugins(PlayScala)
  .settings(
    name := """cambodia-in-charts""",
    maintainer := "quey.ryan@gmail.org",
    organization := "com.ryanquey",
    version := "1.0-SNAPSHOT",
    scalaVersion := "2.13.1",
    libraryDependencies ++= Seq(
      guice,
      "org.scalatestplus.play" %% "scalatestplus-play" % "5.0.0" % Test
    ),
    scalacOptions ++= Seq(
      "-feature",
      "-deprecation",
      "-Xfatal-warnings"
    )
  )
