package sbtoneswt

import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream
import java.util.jar.Attributes
import java.util.jar.JarEntry
import java.util.jar.JarFile
import java.util.jar.JarOutputStream
import java.util.jar.Manifest
import java.util.zip.ZipEntry
import sbt.Def
import sbt.Keys._
import sbt._

// 참조: http://stackoverflow.com/questions/2706222/create-cross-platform-java-swt-application

object OneSwtPlugin extends AutoPlugin {
    object autoImport {
        val oneswtAssembly: TaskKey[File] = taskKey[File]("Create a fat jar including swt.")
        val oneswtResolver: TaskKey[Resolver] = taskKey[Resolver]("Where to resolve swt jars")

        val oneswtAssemblyJarName: TaskKey[String] = taskKey[String]("jar name")
        val oneswtAssemblyDefaultJarName: TaskKey[String] = taskKey[String]("jar name")
        val oneswtAssemblyOutputPath: TaskKey[File] = taskKey[File]("output path")

        val oneswtVersion: SettingKey[String] = settingKey[String]("swt version to use")
        val archDependentSwt: SettingKey[ModuleID] = settingKey[ModuleID]("SWT module for current architecture - use it for development")

        // default values for the tasks and settings
        lazy val baseOneSwtSettings: Seq[Def.Setting[_]] = Seq(
            oneswtAssembly := OneSwt.assemblyTask(oneswtAssembly).value,
            oneswtResolver in oneswtAssembly := "Local Maven Repository" at "file:///C:/home/joonsoo/workspace/oneswt/maven",
            oneswtAssemblyJarName in oneswtAssembly := ((oneswtAssemblyJarName in oneswtAssembly) or (oneswtAssemblyDefaultJarName in oneswtAssembly)).value,
            oneswtAssemblyDefaultJarName in oneswtAssembly := { name.value + "-oneswt-assembly-" + version.value + ".jar" },
            oneswtAssemblyOutputPath in oneswtAssembly := { (target in oneswtAssembly).value / (oneswtAssemblyJarName in oneswtAssembly).value },
            target in oneswtAssembly := crossTarget.value,
            oneswtVersion in oneswtAssembly := "4.6.2",
            archDependentSwt := {
                val (name, arch) = (sys.props("os.name"), sys.props("os.arch"))
                println("oneswtAssembly.version=" + (oneswtVersion in oneswtAssembly).value)
                val archName = archs.find(_._1(name, arch)).get._2
                println(s"$name, $arch -> $archName")
                moduleIdOf(archName, (oneswtVersion in oneswtAssembly).value)
            }
        )
    }

    val archs: Seq[((String, String) => Boolean, String)] = Seq(
        ({ (name, arch) => name == "Linux" && arch == "amd64" || arch == "x86_64" }, "gtk-linux-x86_64"),
        ({ (name, arch) => name == "Linux" }, "gtk-linux-x86"),
        ({ (name, arch) => name == "Mac OS X" }, "cocoa-macosx-x86_64"),
        ({ (name, arch) => name.startsWith("Windows") && arch == "amd64" }, "win32-win32-x86_64"),
        ({ (name, arch) => name.startsWith("Windows") }, "win32-win32-x86")
    )
    def moduleIdOf(archName: String, swtVersion: String): ModuleID =
        "org.eclipse.swt" % s"swt-$swtVersion-$archName" % swtVersion

    import autoImport._

    lazy val archDependentSwt: SettingKey[ModuleID] = autoImport.archDependentSwt

    override def requires = sbt.plugins.JvmPlugin

    override def trigger: PluginTrigger = allRequirements

    override lazy val projectSettings: Seq[Def.Setting[_]] = baseOneSwtSettings
}

object OneSwt {
    import OneSwtPlugin.autoImport._

    def assemblyTask(key: TaskKey[File]): Def.Initialize[Task[File]] = Def.task {
        // TODO 가능하면 assembly할 때 아예 swt 관련 jar는 뺄 수 없을까
        val assemblyJar = (sbtassembly.AssemblyKeys.assembly in oneswtAssembly).value
        // println(assemblyJar.getCanonicalPath)
        // println(s"out jar to: ${(oneswtAssemblyOutputPath in key).value}")
        val swtVersion = (oneswtVersion in key).value
        // val swtResolver = (oneswtResolver in key).value

        val swtModules: Seq[(String, File)] =
            OneSwtPlugin.archs map { _._2 } map { archName =>
                val moduleId = OneSwtPlugin.moduleIdOf(archName, swtVersion)
                val fileName = moduleId.name + "-" + moduleId.revision + ".jar"
                (fileName, ???)
            }

        OneSwt(
            out = (oneswtAssemblyOutputPath in key).value,
            assemblyFile = assemblyJar,
            swtVersion = swtVersion,
            swtModules = swtModules,
            log = (streams in key).value.log
        )
    }

    def transfer(in: InputStream, out: OutputStream): Unit = {
        val length = 1024
        val bytes = new Array[Byte](length)
        var bytesRead = in.read(bytes)

        while (bytesRead >= 0) {
            out.write(bytes, 0, bytesRead)
            bytesRead = in.read(bytes)
        }
    }

    def apply(out: File, assemblyFile: File, swtVersion: String, swtModules: Seq[(String, File)], log: Logger): File = {
        // 1. assemblyJar에서 org/eclipse/swt 폴더 삭제
        // 2. assemblyJar에 swt jar들 넣기
        // 3. SwtLoader에서 assemblyJar의 원래 메인 클래스 실행하도록 수정하기(manifest 이용)
        // 4. assemblyJar의 manifest에서 MainClass를 SwtLoader로 바꾸기

        val assemblyJar = new JarFile(assemblyFile)

        val assemblyManifest = assemblyJar.getManifest
        val outManifest = new Manifest(assemblyManifest)

        // 기존 manifest에서 MAIN_CLASS만 변경
        if (assemblyManifest.getMainAttributes.containsKey(Attributes.Name.MAIN_CLASS)) {
            val originalMainClassName = assemblyManifest.getMainAttributes.get(Attributes.Name.MAIN_CLASS)
            outManifest.getMainAttributes.put(Attributes.Name.MAIN_CLASS, "com.giyeok.oneswt.SwtLoader")
            outManifest.getMainAttributes.put(new Attributes.Name("OneSwt-Original-Main-Class"), originalMainClassName)
        }
        outManifest.getMainAttributes.put(new Attributes.Name("OneSwt-SWT-Version"), swtVersion)

        // 새 jar 생성
        val outStream = new JarOutputStream(new FileOutputStream(out), outManifest)

        // assemblyJar -> newJar로 org/eclipse/swt 제외한 클래스 파일들 옮기기
        def copyFile(assemblyJarEntry: JarEntry, entryContent: InputStream): Unit = {
            outStream.putNextEntry(new ZipEntry(assemblyJarEntry))
            transfer(entryContent, outStream)
            outStream.closeEntry()
        }

        val assemblyJarEntryIterator = assemblyJar.entries()
        while (assemblyJarEntryIterator.hasMoreElements) {
            val entry = assemblyJarEntryIterator.nextElement()
            val entryName = entry.getName
            // println(entryName)
            if (!(entryName startsWith "com/eclipse/swt/") && !(entryName startsWith "swt") && (entryName != "META-INF/MANIFEST.MF")) {
                copyFile(entry, assemblyJar.getInputStream(entry))
            }
        }

        // swt-**.jar 파일들 추가
        def addFile(name: String, entryContent: InputStream): Unit = {
            outStream.putNextEntry(new ZipEntry(name))
            transfer(entryContent, outStream)
            outStream.closeEntry()
        }
        swtModules foreach { swtModule =>
            addFile(swtModule._1, new FileInputStream(swtModule._2))
        }

        // SwtLoader 클래스 추가
        // addFile("/com/giyeok/oneswt/SwtLoader.class", ???)

        outStream.close()

        out
    }
}
