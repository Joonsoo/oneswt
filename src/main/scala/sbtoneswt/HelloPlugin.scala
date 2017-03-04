package sbtoneswt

import sbt._
import Keys._

object HelloPlugin extends AutoPlugin {
    object autoImport {
        val obfuscate = taskKey[Seq[File]]("Obfuscates files")
        val obfuscateLiterals = settingKey[Boolean]("Obfuscate literals")

        lazy val baseObfuscateSettings: Seq[Def.Setting[_]] = Seq(
            obfuscate := Obfuscate(sources.value, (obfuscateLiterals in obfuscate).value)
        )
    }

    import autoImport._

    override def requires: Plugins = sbt.plugins.JvmPlugin

    override def trigger: PluginTrigger = allRequirements

    override val projectSettings: Seq[Def.Setting[_]] =
        inConfig(Compile)(baseObfuscateSettings) ++
            inConfig(Test)(baseObfuscateSettings)

    lazy val oneswtAssemblyCommand: Command =
        Command.command("oneswtAssembly") { (state: State) =>
            println("oneswtAssembly")
            state
        }
}

object Obfuscate {
    def apply(sources: Seq[File], obfuscateLiterals: Boolean): Seq[File] = {
        sources
    }
}
