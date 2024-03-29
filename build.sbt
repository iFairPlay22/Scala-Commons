// Projects
lazy val global = (project in file("."))
  .settings(defaultSettings)
  .settings(
    name := "commons-root",
    scalaVersion := projectLibraryDependencies.scala.scalaVersion,
    libraryDependencies ++= globalLibraryDependencies,
    Test / parallelExecution := false,
    publish / skip := true)
  .aggregate(commons, broker, db, http, scheduler)
  .dependsOn(commons, broker, db, http, scheduler)

lazy val commons = (project in file("commons"))
  .settings(defaultSettings)
  .settings(
    name := "commons-commons-libs",
    scalaVersion := projectLibraryDependencies.scala.scalaVersion,
    libraryDependencies ++= commonsLibraryDependencies)

lazy val broker = (project in file("broker"))
  .settings(defaultSettings)
  .settings(
    name := "commons-broker-libs",
    scalaVersion := projectLibraryDependencies.scala.scalaVersion,
    libraryDependencies ++= brokerLibraryDependencies)
  .dependsOn(commons)

lazy val db = (project in file("db"))
  .settings(defaultSettings)
  .settings(
    name := "commons-db-libs",
    scalaVersion := projectLibraryDependencies.scala.scalaVersion,
    libraryDependencies ++= dbLibraryDependencies)
  .dependsOn(commons)

lazy val http = (project in file("http"))
  .settings(defaultSettings)
  .settings(
    name := "commons-http-libs",
    scalaVersion := projectLibraryDependencies.scala.scalaVersion,
    libraryDependencies ++= httpLibraryDependencies)
  .dependsOn(commons)

lazy val scheduler = (project in file("scheduler"))
  .settings(defaultSettings)
  .settings(
    name := "commons-scheduler-libs",
    scalaVersion := projectLibraryDependencies.scala.scalaVersion,
    libraryDependencies ++= schedulerLibraryDependencies)
  .dependsOn(commons)

// Default settings
lazy val defaultSettings = Seq(
  organization := "ewenbouquet",
  publishTo := {
    val nexus =
      sys.env.getOrElse("NEXUS_BASE_URL", "https://ewenbouquet-nexus-public-url.loca.lt")
    if (isSnapshot.value)
      Some("snapshots" at nexus + "/repository/maven-snapshots/")
    else
      Some("releases" at nexus + "/repository/maven-releases/")
  },
  credentials += Credentials(Path.userHome / ".sbt" / ".ewenbouquet_credentials"))

// Docker plugin settings
lazy val dockerSettings =
  Seq(dockerUsername := Some("ewenbouquet"), dockerBaseImage := "openjdk:11")

// Library dependencies
lazy val projectLibraryDependencies =
  new {
    val scala = new {
      val scalaVersion = "2.13.10"
      val scalaLoggingVersion = "3.9.5"
      val scalaTestVersion = "3.2.15"
      val scalaMockitoTestVersion = "3.2.10.0"

      val scalaLogging = "com.typesafe.scala-logging" %% "scala-logging" % scalaLoggingVersion
      val scalaTest = "org.scalatest" %% "scalatest" % scalaTestVersion
      val scalaMockitoTest = "org.scalatestplus" %% "mockito-3-4" % scalaMockitoTestVersion % Test

      val all = Seq(scalaLogging, scalaTest, scalaMockitoTest)
    }

    val java = new {
      val javaVersion = "11.0.15"
    }

    val logback = new {
      val logbackVersion = "1.4.7"
      val akkaSlfjVersion = "2.8.0"

      val logbackClassic = "ch.qos.logback" % "logback-classic" % logbackVersion
      val akkaSlf4j = "com.typesafe.akka" %% "akka-slf4j" % akkaSlfjVersion

      val all =
        Seq(logbackClassic, akkaSlf4j)
    }

    val circe = new {
      val circeVersion = "0.14.5"

      val circeCore = "io.circe" %% "circe-core" % circeVersion
      val circeGeneric = "io.circe" %% "circe-generic" % circeVersion
      val circeParser = "io.circe" %% "circe-parser" % circeVersion

      val all = Seq(circeCore, circeGeneric, circeParser)
    }

    val akkaHttp = new {

      val akkaHttpVersion = "10.5.0"
      val akkaHttpCirceVersion = "1.39.2"
      val akkaTestKitVersion = "2.8.0"

      val akkaHttp = "com.typesafe.akka" %% "akka-http" % akkaHttpVersion
      val akkaTestKit = "com.typesafe.akka" %% "akka-testkit" % akkaTestKitVersion
      val akkaHttpTestkit = "com.typesafe.akka" %% "akka-http-testkit" % akkaHttpVersion
      val akkaCirceHttp = "de.heikoseeberger" %% "akka-http-circe" % akkaHttpCirceVersion

      val all = Seq(akkaHttp, akkaTestKit, akkaHttpTestkit, akkaCirceHttp)
    }

    val akkaStream = new {

      val akkaStreamVersion = "2.8.0"
      val akkaStreamKafkaVersion = "4.0.0"

      val akkaStream = "com.typesafe.akka" %% "akka-stream" % akkaStreamVersion
      val akkaStreamKafka = "com.typesafe.akka" %% "akka-stream-kafka" % akkaStreamKafkaVersion

      val all = Seq(akkaStream, akkaStreamKafka)
    }

    val akkaDb = new {
      val slickVersion = "3.4.1"
      val akkaStreamAlpakkaSlickVersion = "5.0.0"
      val dbConnectorVersion = "8.0.33"
      val postgresVersion = "42.5.4"

      val slick = "com.typesafe.slick" %% "slick" % slickVersion
      val slickHikaricp = "com.typesafe.slick" %% "slick-hikaricp" % slickVersion
      val akkaStreamAlpakkaDb =
        "com.lightbend.akka" %% "akka-stream-alpakka-slick" % akkaStreamAlpakkaSlickVersion
      val postgres = "org.postgresql" % "postgresql" % postgresVersion

      val all = Seq(slick, slickHikaricp, akkaStreamAlpakkaDb, postgres)
    }

    val kafka = new {
      val kafkaVersion = "3.4.0"

      val kafkaClients = "org.apache.kafka" % "kafka-clients" % kafkaVersion
      val kafkaStreams = "org.apache.kafka" % "kafka-streams" % kafkaVersion
      val kafkaStreamsScala = "org.apache.kafka" %% "kafka-streams-scala" % kafkaVersion

      val all = Seq(kafkaClients, kafkaStreams, kafkaStreamsScala)
    }
  }

lazy val commonsLibraryDependencies =
  projectLibraryDependencies.scala.all ++
    projectLibraryDependencies.logback.all ++
    projectLibraryDependencies.circe.all

lazy val globalLibraryDependencies =
  commonsLibraryDependencies

lazy val brokerLibraryDependencies =
  commonsLibraryDependencies ++
    projectLibraryDependencies.akkaStream.all ++
    projectLibraryDependencies.kafka.all

lazy val dbLibraryDependencies =
  commonsLibraryDependencies ++
    projectLibraryDependencies.akkaStream.all ++
    projectLibraryDependencies.akkaDb.all

lazy val httpLibraryDependencies =
  commonsLibraryDependencies ++
    projectLibraryDependencies.akkaHttp.all ++
    projectLibraryDependencies.akkaStream.all

lazy val schedulerLibraryDependencies =
  commonsLibraryDependencies ++
    projectLibraryDependencies.akkaStream.all
