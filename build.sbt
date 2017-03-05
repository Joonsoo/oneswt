
lazy val root = (project in file(".")).
    settings(
        sbtPlugin := true,
        organization := "com.giyeok.oneswt",
        name := "sbt-oneswt",
        version := "0.0.1",
        description := "create fat jar with cross-platform swt jars"
    )
    .settings(addSbtPlugin("com.eed3si9n" % "sbt-assembly" % "0.14.4"))
