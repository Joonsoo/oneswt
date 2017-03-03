lazy val root = (project in file("."))
    .settings(
        organization := "com.giyeok",
        name := "oneswt",
        version := "0.0.1"
    )

javacOptions in compile ++= Seq("-encoding", "UTF-8")

javaOptions in run := {
    println(sys.props("os.name"))
    if (sys.props("os.name") == "Mac OS X") Seq("-XstartOnFirstThread", "-d64") else Seq()
}
