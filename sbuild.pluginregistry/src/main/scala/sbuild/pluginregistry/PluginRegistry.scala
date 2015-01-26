package sbuild.pluginregistry

import scala.util.Try
import sbuild.artifactrepo.Artifact
import sbuild.artifactrepo.ArtifactRepoFunctions
import sbuild.artifactrepo.Constraint
import sbuild.artifactrepo.Dependency
import scala.util.Success

object Plugin {

  object Attr {
    val Name = "SBuildPlugin-Name"
    val Version = "SBuildPlugin-Version"
    val PluginClass = "SBuildPlugin-PluginClass"
    val PluginFactoryClass = "SBuildPlugin-PluginFactoryClass"
    val ExportedPackages = "SBuildPlugin-ExportedPackages"
    val SBuildVersion = "SBuildPlugin-SBuildVersion"
  }

  def fromArtifact(artifact: Artifact): Try[Plugin] = Try {
    val attr = artifact.attributes
    Plugin(
      artifact = artifact,
      name = attr(Attr.Name),
      version = attr.getOrElse(Attr.Version, artifact.version),
      pluginClass = attr(Attr.PluginClass),
      pluginFactoryClass = attr(Attr.PluginFactoryClass),
      exportedPackages = attr.get(Attr.ExportedPackages).map(_.split(",").toSeq),
      sbuildVersion = attr.getOrElse(Attr.SBuildVersion, "0.7.1")
    )
  }

}

case class Plugin(
  name: String,
  version: String,
  exportedPackages: Option[Seq[String]],
  pluginClass: String,
  pluginFactoryClass: String,
  artifact: Artifact,
  sbuildVersion: String)

object PluginRegistry {

  def findPlugins(artifacts: Seq[Artifact], constraints: Set[Constraint]): Seq[Plugin] = {
    val pluginArtifacts = ArtifactRepoFunctions.filterArtifacts(artifacts, Dependency(constraints))
    pluginArtifacts.map(Plugin.fromArtifact).collect { case Success(p) => p }
  }
  
}

