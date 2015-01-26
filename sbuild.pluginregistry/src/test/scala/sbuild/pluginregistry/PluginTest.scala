package sbuild.pluginregistry

import org.scalatest.FreeSpec
import sbuild.artifactrepo.Artifact
import sbuild.artifactrepo.Constraint

class PluginTest extends FreeSpec {

  def artifact(name: String, version: String) = Artifact(attributes = Map("Name" -> name, "Version" -> version))

  val a1 = artifact("a", "1")
  val a2 = artifact("a", "2")
  val b1 = artifact("b", "1")
  val b2 = artifact("b", "2")

  val p1 = Artifact(attributes = Map(
    Artifact.Attr.Name -> "org.example.p",
    Artifact.Attr.Version -> "1",
    Plugin.Attr.Name -> "org.example.p.Plugin",
    Plugin.Attr.PluginClass -> "org.example.p.Plugin",
    Plugin.Attr.PluginFactoryClass -> "org.example.p.Plugin",
    Plugin.Attr.Version -> "1"
  ))

  "Empty repo" - {
    val artifacts = Seq()

    "Find org.example.P1" in {
      assert(PluginRegistry.findPlugins(artifacts, Set(Constraint.AttributeEquals(Plugin.Attr.Name, "org.example.P1"))) === Seq())
    }

  }

  "Test repo" - {
    val artifacts = Seq(a1, a2, b1, b2, p1)

    "Find all" in {
      val res = PluginRegistry.findPlugins(artifacts, Set())
      assert(res.size === 1)
    }

    "Find p1" in {
      val res = PluginRegistry.findPlugins(artifacts, Set(Constraint.AttributeEquals(Plugin.Attr.Name, "org.example.p.Plugin")))
      assert(res.size === 1)
      assert(res.head.artifact === p1)
    }

  }

}