lazy val root = (project in file(".")).
    settings(BintrayPlugin.bintraySettings: _*).
    settings(
        sbtPlugin := true,
        organization := "com.giyeok.oneswt",
        name := "sbt-oneswt",
        scalaVersion := "2.12.5",
        version := "0.0.4",
        description := "create fat jar with cross-platform swt jars",
        licenses += ("MIT", url("http://opensource.org/licenses/MIT")),
        publishMavenStyle := false,
        bintrayRepository := "sbt-plugins",
        bintrayOrganization in bintray := None
    )
    .settings(addSbtPlugin("com.eed3si9n" % "sbt-assembly" % "0.14.6"))
