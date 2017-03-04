package sbtoneswt

import java.io.File
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

        val oneswtAssemblyJarName: TaskKey[String] = taskKey[String]("jar name")
        val oneswtAssemblyDefaultJarName: TaskKey[String] = taskKey[String]("jar name")
        val oneswtAssemblyOutputPath: TaskKey[File] = taskKey[File]("output path")

        // default values for the tasks and settings
        lazy val baseOneSwtSettings: Seq[Def.Setting[_]] = Seq(
            oneswtAssembly := OneSwt.assemblyTask(oneswtAssembly).value,
            oneswtAssemblyJarName in oneswtAssembly := ((oneswtAssemblyJarName in oneswtAssembly) or (oneswtAssemblyDefaultJarName in oneswtAssembly)).value,
            oneswtAssemblyDefaultJarName in oneswtAssembly := { name.value + "-oneswt-assembly-" + version.value + ".jar" },
            oneswtAssemblyOutputPath in oneswtAssembly := { (target in oneswtAssembly).value / (oneswtAssemblyJarName in oneswtAssembly).value }
        )
    }
    override def requires = sbt.plugins.JvmPlugin

    override def trigger: PluginTrigger = allRequirements
}

object OneSwt {
    import OneSwtPlugin.autoImport._

    def assemblyTask(key: TaskKey[File]): Def.Initialize[Task[File]] = Def.task {
        val assemblyJar: File = ??? // sbtassembly
        val swtVersion = (version in key).value
        OneSwt(
            out = (oneswtAssemblyOutputPath in key).value,
            assemblyFile = assemblyJar,
            swtVersion = swtVersion,
            log = (streams in key).value.log
        )
    }

    def transfer(in: InputStream, out: OutputStream): Unit = {
        val length = 1024
        val bytes = new Array[Byte](length)
        var bytesRead = in.read(bytes)

        while (bytesRead >= 0) {
            out.write(bytes)
            bytesRead = in.read(bytes)
        }
    }

    def apply(out: File, assemblyFile: File, swtVersion: String, log: Logger): File = {
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
            outManifest.getMainAttributes.put(Attributes.Name.MAIN_CLASS, "com.giyeok.oneswt.oneswt.SwtLoader")
            outManifest.getMainAttributes.put("OneSwt-Original-Main-Class", originalMainClassName)
        }
        outManifest.getMainAttributes.put("OneSwt-Swt-Version: ", swtVersion)

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
            if (!(entry.getName startsWith "com/eclipse/swt/")) {
                copyFile(entry, assemblyJar.getInputStream(entry))
            }
        }

        // swt-**.jar 파일들 추가
        def addFile(name: String, entryContent: InputStream): Unit = {
            outStream.putNextEntry(new ZipEntry(name))
            transfer(entryContent, outStream)
            outStream.closeEntry()
        }
        // TODO - dependency manager에서 "com.giyeok.oneswt" % "swt-**" % swtVersion 에 해당하는 jar얻어오기

        // TODO - SwtLoader 클래스 넣기
        addFile("/com/giyeok/oneswt/SwtLoader.class", ???)

        outStream.close()

        out
    }
}
