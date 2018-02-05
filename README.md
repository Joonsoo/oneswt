# oneswt

SBT plugin to enable architecture dependent SWT dependency(Windows, gtk, Mac OS).

Plugin deployed in bintray: https://bintray.com/joonsoo/sbt-plugins/sbt-oneswt

SWT jars deployed in bintray: https://bintray.com/joonsoo/oneswt


HOW TO USE
----------

Add to `project/plugins.sbt`:

	resolvers += Resolver.bintrayIvyRepo("joonsoo", "sbt-plugins")
	addSbtPlugin("com.giyeok.oneswt" % "sbt-oneswt" % "0.0.3")


To use architecture-dependent SWT dependency, add to `build.sbt`:

	libraryDependencies += sbtoneswt.OneSwtPlugin.archDependentSwt.value


TBA: To create fat jar with architecture branching

	sbt oneswtAssembly

