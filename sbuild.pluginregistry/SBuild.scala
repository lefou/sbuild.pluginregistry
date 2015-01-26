import de.tototec.sbuild._
import de.tototec.sbuild.TargetRefs._
import de.tototec.sbuild.ant._
import de.tototec.sbuild.ant.tasks._

@version("0.5.0")
// @include("../SBuildConfig.scala")
@classpath("mvn:org.apache.ant:ant:1.8.4")
class SBuild(implicit _project: Project) {

  val version = "0.0.1"

  def scalaVersion = "2.11.2"
  def scalaBinVersion = "2.11"
  def scalaLibrary = s"mvn:org.scala-lang:scala-library:${scalaVersion}"
  def scalaCompiler = s"mvn:org.scala-lang:scala-compiler:${scalaVersion}"
  def scalaReflect = s"mvn:org.scala-lang:scala-reflect:${scalaVersion}"
  def scalaXml = s"mvn:org.scala-lang.modules:scala-xml_${scalaBinVersion}:1.0.1"
  val scalaTest = s"mvn:org.scalatest:scalatest_${scalaBinVersion}:2.1.7"

  def compilerPath = scalaLibrary ~ scalaCompiler ~ scalaReflect


  val jar = s"target/org.sbuild.addons-${version}.jar"
  val testJar = s"target/org.sbuild.addons-${version}-tests.jar"

  val compileCp = scalaLibrary

  val testCp = compileCp ~
    scalaXml ~
    scalaTest

  ExportDependencies("eclipse.classpath", testCp)

  Target("phony:all") dependsOn jar

  Target("phony:clean").evictCache exec {
    AntDelete(dir = Path("target"))
  }

  Target("phony:compile").cacheable dependsOn compilerPath ~ compileCp ~ "scan:src/main/scala" exec {
    val input = "src/main/scala"
    val output = "target/classes"

    addons.scala.Scalac(
      deprecation = true, unchecked = true, debugInfo = "vars", target = "jvm-1.6",
      compilerClasspath = compilerPath.files,
      classpath = compileCp.files,
      sources = "scan:src/main/scala".files,
      destDir = Path(output),
      fork = true
    )
  }

  Target(jar) dependsOn ("compile") exec { ctx: TargetContext =>
    AntJar(
      destFile = ctx.targetFile.get,
      baseDir = Path("target/classes"),
      fileSet = AntFileSet(dir = Path("."), includes = "LICENSE.txt")
    )
  }

  Target("phony:testCompile").cacheable dependsOn compilerPath ~ testCp ~ jar ~ "scan:src/test/scala" exec {
    addons.scala.Scalac(
      compilerClasspath = compilerPath.files,
      classpath = testCp.files ++ jar.files,
      sources = "scan:src/test/scala".files,
      destDir = Path("target/test-classes"),
      deprecation = true, unchecked = true, debugInfo = "vars",
      fork = true
    )
  }

  Target(testJar) dependsOn "testCompile" ~ "scan:target/test-classes" exec { ctx: TargetContext =>
    AntJar(destFile = ctx.targetFile.get, baseDir = Path("target/test-classes"))
  }

  Target("phony:test") dependsOn testCp ~ jar ~ "testCompile" exec {
    val res = addons.support.ForkSupport.runJavaAndWait(
      classpath = testCp.files ++ jar.files,
      arguments = Array("org.scalatest.tools.Runner", "-p", Path("target/test-classes").getPath, "-oG", "-u", Path("target/test-output").getPath)
    )
    if (res != 0) throw new RuntimeException("Some tests failed")

  }
}
