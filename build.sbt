import sbt.Project.projectToRef
import play.PlayImport.PlayKeys._

lazy val clients = Seq(exampleClient)
lazy val scalaV = "2.12.4"

//resolvers += "bintray/non" at "http://dl.bintray.com/non/maven"

lazy val exampleServer = (project in file("example-server")).settings(
  scalaVersion := scalaV,
  routesImport += "config.Routes._",
  scalaJSProjects := clients,
  pipelineStages := Seq(scalaJSProd, gzip),
  resolvers += "scalaz-bintray" at "https://dl.bintray.com/scalaz/releases",
  libraryDependencies ++= Seq(
    filters,
    jdbc,
    evolutions,
    "com.michaelpollmeier" %% "gremlin-scala" % "3.3.1.1",
    "org.neo4j" % "neo4j-tinkerpop-api-impl" % "0.7-3.2.3",
    "com.typesafe.play" %% "anorm" % "2.5.3",
    "com.vmunier" %% "play-scalajs-scripts" % "1.1.1",
    "com.typesafe.slick" %% "slick" % "3.2.1",
    "com.typesafe.play" %% "play-slick" % "3.0.1",
    "com.lihaoyi" %% "upickle" % "0.5.1",
    "org.webjars" %% "webjars-play" % "2.6.3",
    "org.webjars" % "bootstrap" % "4.0.0",
    "org.webjars" % "jquery" % "3.3.1",
    "org.webjars" % "font-awesome" % "5.0.6",
    "com.lihaoyi" %% "utest" % "0.6.3" % "test"
  )
 ).enablePlugins(PlayScala).
  aggregate(clients.map(projectToRef): _*).
  dependsOn(exampleSharedJvm)

lazy val exampleClient = (project in file("example-client")).settings(
  scalaVersion := scalaV,
  persistLauncher := true,
  persistLauncher in Test := false,
  libraryDependencies ++= Seq(
    "org.scala-js" %%% "scalajs-dom" % "0.9.4",
    "com.lihaoyi" %%% "scalatags" % "0.6.7",
    "com.lihaoyi" %%% "scalarx" % "0.3.2",
    "be.doeraene" %%% "scalajs-jquery" % "0.9.2",
    "com.lihaoyi" %%% "upickle" % "0.5.1",
    "com.lihaoyi" %%% "utest" % "0.6.3" % Test
  )
).enablePlugins(ScalaJSPlugin, ScalaJSPlay).
  dependsOn(exampleSharedJs)

val exampleSharedJvmSettings = List(
  libraryDependencies ++= Seq(
    "com.lihaoyi" %% "upickle" % "0.5.1",
    "com.lihaoyi" %% "utest" % "0.6.3" % "test"
  )
)

val exampleSharedForIDE = (project in file("example-shared")).settings(
  (scalaVersion := scalaV) +:
  (testFrameworks += new TestFramework("utest.runner.Framework")) +:
  exampleSharedJvmSettings :_*)

val exampleShared = (crossProject.crossType(CrossType.Pure) in file("example-shared")).
  settings(
    scalaVersion := scalaV,
    testFrameworks += new TestFramework("utest.runner.Framework")
  ).
  jvmSettings(exampleSharedJvmSettings: _*).
  jsSettings(
    libraryDependencies ++= Seq(
      "com.lihaoyi" %%% "upickle" % "0.5.1",
      "com.lihaoyi" %%% "utest" % "0.6.3" % "test"
    )
  )

lazy val exampleSharedJvm = exampleShared.jvm
lazy val exampleSharedJs = exampleShared.js

// loads the jvm project at sbt startup
onLoad in Global := (Command.process("project exampleServer", _: State)) compose (onLoad in Global).value

// for Eclipse users
EclipseKeys.skipParents in ThisBuild := false
// Compile the project before generating Eclipse files, so that generated .scala or .class files for views and routes are present
EclipseKeys.preTasks := Seq(compile in (exampleServer, Compile))
