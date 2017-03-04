
lazy val root = (project in file(".")).
    settings(
        sbtPlugin := true,
        organization := "com.giyeok",
        name := "sbt-oneswt",
        version := "0.0.1",
        description := "create fat jar with cross-platform swt jars"
    )
