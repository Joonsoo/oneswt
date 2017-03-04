
lazy val root = (project in file(".")).
    settings(
        sbtPlugin := true,
        organization := "com.giyeok",
        name := "sbt-oneswt",
        version := "4.6.2",
        description := "create fat jar with cross-platform swt jars"
    )
    .settings(addSbtPlugin("com.eed3si9n" % "sbt-assembly" % "0.14.4"))
