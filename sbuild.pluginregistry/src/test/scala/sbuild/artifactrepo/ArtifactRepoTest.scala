package sbuild.artifactrepo

import org.scalatest.FreeSpec

import scala.util.Success

class ArtifactRepoFunctionsTest extends FreeSpec {

  val a1 = Artifact("a", "1", location = "???")
  val a2 = Artifact("a", "2", location = "???")
  val a3 = Artifact("a", "3", location = "???", dependencies = Map("compile" -> Seq(Dependency("b", Constraint.Version("1")))))
  val b1 = Artifact("b", "1", location = "???")
  val b2 = Artifact("b", "2", location = "???")

  "Repo with a:1" - {
    val artifacts = Seq(a1)

    "Find a" in {
      val res = ArtifactRepoFunctions.resolve(ResolveState("compile", artifacts, unresolvedDependencies = Seq(Dependency("a"))))
      assert(res === Success(Seq(a1)))
    }
    "Find a:1" in {
      val res = ArtifactRepoFunctions.resolve(ResolveState("compile", artifacts, unresolvedDependencies = Seq(Dependency("a", Constraint.Version("1")))))
      assert(res === Success(Seq(a1)))
    }
    "Fail a:2" in {
      val res = ArtifactRepoFunctions.resolve(ResolveState("compile", artifacts, unresolvedDependencies = Seq(Dependency("a", Constraint.Version("2")))))
      val ex = intercept[ArtifactNotFoundException](res.get)
      assert(ex.getMessage startsWith "Cannot find artifact that resolves dependency: ")
      assert(ex.dependency == Dependency("a", Constraint.Version("2")))
    }
  }

  "Repo with a:1 and a:2" - {
    val artifacts = Seq(a1, a2)

    "Find a:2" in {
      val res = ArtifactRepoFunctions.resolve(ResolveState("compile", artifacts, unresolvedDependencies = Seq(Dependency("a", Constraint.Version("2")))))
      assert(res === Success(Seq(a2)))
    }
  }

  "Repo with missing transitive dependencies" - {
    val artifacts = Seq(a3)

    "Fail a:3 because of missing b:1" in {
      val res = ArtifactRepoFunctions.resolve(ResolveState("compile", artifacts, unresolvedDependencies = Seq(Dependency("a", Constraint.Version("3")))))
      val ex = intercept[OverconstrainedDependenciesException](res.get)
      // assert(ex.getMessage === "Cannot find artifact that resolves dependency: Dependency(a,List(Version(2)))")
    }
  }

  "Repo with a:1, a:2, a:3, b1, b2" - {
    val artifacts = Seq(a1, a2, a3, b1, b2)

    "Find b:1" in {
      val res = ArtifactRepoFunctions.resolve(ResolveState("compile", artifacts, unresolvedDependencies = Seq(Dependency("b", Constraint.Version("1")))))
      assert(res === Success(Seq(b1)))
    }

    "Find a:3" in {
      val res = ArtifactRepoFunctions.resolve(ResolveState("compile", artifacts, unresolvedDependencies = Seq(Dependency("a", Constraint.Version("3")))))
      assert(res === Success(Seq(a3, b1)))
    }

    "Find a:3 by version range" in {
      val res = ArtifactRepoFunctions.resolve(ResolveState("compile", artifacts, unresolvedDependencies = Seq(Dependency("a", Constraint.VersionRange("[3, 3]")))))
      assert(res === Success(Seq(a3, b1)))
    }
  }

  println("FAIL " + ArtifactRepoFunctions.resolve(ResolveState(
    dependencyScope = "compile",
    availableArtifacts = Seq(
      Artifact("a", "1", location = "???", dependencies = Map("compile" -> Seq(Dependency("b", Constraint.VersionRange("1"))))),
      Artifact("b", "1", location = "???"),
      Artifact("b", "2", location = "???")
    ),
    unresolvedDependencies = Seq(Dependency(name = "a", Constraint.VersionRange("1"))))))

  println("FAIL " + ArtifactRepoFunctions.resolve(ResolveState(
    dependencyScope = "compile",
    availableArtifacts = Seq(
      Artifact("a", "1", location = "???", dependencies = Map("compile" -> Seq(
        Dependency("b", Constraint.Version("1")),
        Dependency("b", Constraint.Version("2")))
      )),
      Artifact("b", "1", location = "???")
    ),
    unresolvedDependencies = Seq(Dependency(name = "a", Constraint.Version("1"))))))

}