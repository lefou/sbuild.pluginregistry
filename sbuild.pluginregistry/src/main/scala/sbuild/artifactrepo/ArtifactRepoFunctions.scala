package sbuild.artifactrepo

import scala.util.Failure
import scala.util.Success
import scala.util.Try

import sbuild.pluginregistry.internal.OSGiVersion
import sbuild.pluginregistry.internal.OSGiVersionRange

case class ResolveRequest(dependencies: Seq[Dependency])

// case class ResolveResult(artifacts: Try[Seq[Artifact]])

case class ResolveState(
  dependencyScope: String,
  availableArtifacts: Seq[Artifact] = Seq(),
  appliedDependencies: Seq[Dependency] = Seq(),
  resolvedArtifacts: Seq[Artifact] = Seq(),
  unresolvedDependencies: Seq[Dependency] = Seq())

object ArtifactRepoFunctions {

  def filterArtifacts(artifacts: Seq[Artifact], dep: Dependency): Seq[Artifact] = {
    def matchConstraints(artifact: Artifact, constraints: Set[Constraint]): Boolean = {
      constraints forall {
        case Constraint.AttributeEquals(key, value) =>
          artifact.attributes.get(key).map(_ == value).getOrElse(false)
        case Constraint.VersionRange(v) =>
          val range = OSGiVersionRange.parseVersionOrRange(v)
          val aVersion = OSGiVersion.parseVersion(artifact.version)
          range.includes(aVersion)
      }
    }

    artifacts.filter { a => matchConstraints(a, dep.constraints) }
  }

  def addCollisionFreeArtifact(artifacts: Seq[Artifact], newArtifact: Artifact): Try[Seq[Artifact]] = {
    // TODO: add more criteria, e.g. exported packages or services
    val coll = artifacts.find(artifact => artifact.name == newArtifact.name)
    coll match {
      case Some(coll) => Failure(new ArtifactCollisionException("Artifact collision", coll, newArtifact))
      case None => Success(artifacts ++ Seq(newArtifact))
    }
  }

  def resolve(resolveState: ResolveState): Try[Seq[Artifact]] = Try {
    if (resolveState.unresolvedDependencies.isEmpty) {
      // no further dependencies
      resolveState.resolvedArtifacts
    } else {
      val dependencyScope = resolveState.dependencyScope
      val dep = resolveState.unresolvedDependencies.head
      val otherDeps = resolveState.unresolvedDependencies.tail
      val foundArtifacts = filterArtifacts(resolveState.availableArtifacts, dep)
      val appliedDependencies = resolveState.appliedDependencies ++ Seq(dep)

      if (foundArtifacts.isEmpty) {
        throw new ArtifactNotFoundException(dep)
      }

      val resolved = foundArtifacts.map { artifact =>
        resolve(ResolveState(
          dependencyScope = dependencyScope,
          appliedDependencies = appliedDependencies,
          unresolvedDependencies = otherDeps ++ artifact.scopedDependencies(dependencyScope),
          availableArtifacts = resolveState.availableArtifacts,
          resolvedArtifacts = addCollisionFreeArtifact(resolveState.resolvedArtifacts, artifact).get
        ))
      }
      val successful = resolved.filter(_.isSuccess)

      if (successful.isEmpty) {
        throw new OverconstrainedDependenciesException(resolveState)
      } else if (successful.size > 1) {
        throw new UnderconstrainedDependenciesException(resolveState)
      } else {
        successful.head.get
      }
    }
  }

}

trait Resolver {
  type ResolveResult = Try[Seq[Artifact]]

  def resolve(resolveState: ResolveState): ResolveResult
}

