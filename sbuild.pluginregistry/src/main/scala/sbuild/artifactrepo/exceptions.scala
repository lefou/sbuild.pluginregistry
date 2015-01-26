package sbuild.artifactrepo

class ResolveException(msg: String) extends RuntimeException(msg)

class OverconstrainedDependenciesException(msg: String, val resolveState: ResolveState) extends ResolveException(msg) {
  def this(resolveState: ResolveState) = this("Cannot resolve overconstrained dependencies: " + resolveState, resolveState)
}
class UnderconstrainedDependenciesException(msg: String, val resolveState: ResolveState) extends ResolveException(msg) {
  def this(resolveState: ResolveState) = this("Cannot resolve underconstrained dependencies: " + resolveState, resolveState)
}

class ArtifactNotFoundException(msg: String, val dependency: Dependency) extends ResolveException(msg) {
  def this(dependency: Dependency) = this("Cannot find artifact that resolves dependency: " + dependency, dependency)
}

class ArtifactCollisionException(msg: String, artifact1: Artifact, artifact2: Artifact) extends ResolveException(msg)

