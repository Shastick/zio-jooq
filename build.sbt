// *****************************************************************************
// Projects
// *****************************************************************************

lazy val root =
  project
    .in(file("."))
    .settings(settings)
    .settings(
      libraryDependencies ++= Seq(
        library.jooq        % Provided,
        library.zio         % Provided,
        library.zioTest     % Test,
        library.zioTestSbt  % Test,
        library.h2          % Test,
      ),
      publishArtifact := true,
      testFrameworks += new TestFramework("zio.test.sbt.ZTestFramework"),
      scalaVersion := "2.13.3",
      crossScalaVersions := Seq("2.12.12", "2.13.3")
    )

// *****************************************************************************
// Library dependencies
// *****************************************************************************

lazy val library =
  new {

    object Version {
      val zio = "1.0.1"
    }

    val jooq        = "org.jooq"       % "jooq"         % "3.13.4"
    val zio         = "dev.zio"       %% "zio"          % Version.zio
    val zioTest     = "dev.zio"       %% "zio-test"     % Version.zio
    val zioTestSbt  = "dev.zio"       %% "zio-test-sbt" % Version.zio
    val h2          = "com.h2database" % "h2"           % "1.4.200"
  }

// *****************************************************************************
// Settings
// *****************************************************************************

lazy val settings =
  commonSettings ++
    scalafmtSettings ++
    commandAliases

lazy val commonSettings =
  Seq(
    name := "zio-jooq",
    organization := "ch.j3t",
    organizationName := "j3t",
    homepage := Some(url("https://github.com/Shastick/zio-jooq/")),
    licenses := Seq("Apache 2.0" -> url("http://www.apache.org/licenses/LICENSE-2.0")),
    publishMavenStyle := true,
    publishArtifact in Test := false,
    pomIncludeRepository := { _ => false },
    publishTo := {
      val nexus = "https://oss.sonatype.org/"
      if (isSnapshot.value)
        Some("snapshots" at nexus + "content/repositories/snapshots")
      else
        Some("releases" at nexus + "service/local/staging/deploy/maven2")
    },
    scmInfo := Some(
      ScmInfo(
        url("https://github.com/Shastick/zio-jooq.git"),
        "scm:git:git@github.com:Shastick/zio-jooq.git"
      )
    ),
    developers := List(
      Developer("shastick", "Shastick", "", url("https://github.com/Shastick"))
    ),
    scalacOptions --= Seq(
      "-Xlint:nullary-override"
    )
  )

lazy val scalafmtSettings =
  Seq(
    scalafmtOnCompile := true
  )

lazy val commandAliases =
  addCommandAlias("fmt", "all scalafmtSbt scalafmt test:scalafmt") ++
    addCommandAlias("check", "all scalafmtSbtCheck scalafmtCheck test:scalafmtCheck")
