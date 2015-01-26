package sbuild.artifactrepo

object Artifact {

  object Attr {
    val Name = "Name"
    val Version = "Version"
    val Location = "Location"
  }

  def apply(name: String,
    version: String,
    location: String,
    dependencies: Map[String, Seq[Dependency]],
    attributes: Map[String, String]): Artifact =
    Artifact(
      attributes = attributes ++ Map(Attr.Name -> name, Attr.Version -> version, Attr.Location -> location),
      dependencies = dependencies
    )

  def apply(name: String,
    version: String,
    location: String,
    dependencies: Map[String, Seq[Dependency]]): Artifact =
    Artifact(
      attributes = Map(Attr.Name -> name, Attr.Version -> version, Attr.Location -> location),
      dependencies = dependencies
    )

  def apply(name: String,
    version: String,
    location: String): Artifact =
    Artifact(attributes = Map(Attr.Name -> name, Attr.Version -> version, Attr.Location -> location))
}

// TODO: rework dependencies (to support intent), use a Map
case class Artifact(
  attributes: Map[String, String],
  dependencies: Map[String, Seq[Dependency]] = Map()) {

  def name: String = attributes(Artifact.Attr.Name)
  def version: String = attributes(Artifact.Attr.Version)
  def location: String = attributes(Artifact.Attr.Location)
  def scopedDependencies(scope: String): Seq[Dependency] = dependencies.getOrElse(scope, Seq())

}

case object Dependency {
  def apply(name: String, constraints: Set[Constraint]): Dependency = Dependency(constraints + Constraint.Name(name))
  def apply(name: String, constraints: Constraint*): Dependency = Dependency(constraints.toSet[Constraint] + Constraint.Name(name))
  def apply(name: String): Dependency = Dependency(Set[Constraint](Constraint.Name(name)))
  def apply(constraints: Constraint*): Dependency = Dependency(constraints.toSet[Constraint])
}

case class Dependency (constraints: Set[Constraint])

sealed trait Constraint

object Constraint {
  case class AttributeEquals(name: String, value: String) extends Constraint
  case class VersionRange(versionRange: String) extends Constraint

  def Name(name: String) = AttributeEquals(Artifact.Attr.Name, name)
  def Version(version: String) = AttributeEquals(Artifact.Attr.Version, version)
}
