scalaVersion := "3.2.2"

libraryDependencies ++= Seq(
  "dev.zio" %% "zio" % "2.0.13",
  "dev.zio" %% "zio-json" % "0.5.0",
  //"dev.zio" %% "zio-http" % "0.0.5"
  "dev.zio" %% "zio-http" % "3.0.0-RC2"
)

lazy val exclusionRules = Seq(
            ExclusionRule(organization = "dev.zio", name = "zio-stacktracer_2.13"),
            ExclusionRule(organization = "dev.zio", name = "zio-logging_2.13"),
            ExclusionRule(organization = "dev.zio", name = "zio-streams_2.13"),
            ExclusionRule(organization = "dev.zio", name = "zio_2.13"),
            ExclusionRule(organization = "dev.zio", name = "izumi-reflect_2.13"),
            //ExclusionRule(organization = "org.typelevel", name = "simulacrum-scalafix-annotations_2.13"),
            //ExclusionRule(organization = "org.typelevel", name = "cats-kernel_2.13"),
            //ExclusionRule(organization = "org.typelevel", name = "cats-core_2.13"),
            //ExclusionRule(organization = "", name = ""),
            ExclusionRule(
              organization = "dev.zio",
              name = "izumi-reflect-thirdparty-boopickle-shaded_2.13",
            ),
            ExclusionRule(organization = "com.lihaoyi", name = "fansi_2.13"),
            ExclusionRule(organization = "com.lihaoyi", name = "pprint_2.13"),
            ExclusionRule(organization = "com.lihaoyi", name = "sourcecode_2.13"),
            ExclusionRule(
              organization = "org.scala-lang.modules",
              name = "scala-collection-compat_2.13",
            ),
            ExclusionRule(
              organization = "org.scala-lang.modules",
              name = "scala-java8-compat_2.13",
            )
)

lazy val defaultRunnableApplication = "HelloWorld"

lazy val root = (project in file("."))
  .settings(
    name := "hello",
    // Need this option for zio-http or else runMain in sbt, then ctrl+c will not actually stop the process
    // since it's running in the same JVM as SBT. (Also could be why certain combinations of settings weren't working)
    fork in run := true,
    mainClass in (Compile, packageBin) := Some(defaultRunnableApplication),
    assembly / mainClass := Some(defaultRunnableApplication),
    assembly / assemblyJarName := s"${name.value}-${version.value}.jar",
    resolvers ++= Seq(
      Resolver.mavenLocal,
      "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots",
      "Sonatype OSS Releases" at "https://oss.sonatype.org/content/repositories/releases",
      Resolver.bintrayRepo("mattmoore", "bcrypt-scala")
    ),
    scalacOptions ++= Seq(),
    libraryDependencies := libraryDependencies
        .value
        .map(
          _ excludeAll (exclusionRules:_*)
        ),
    Test / fork := true,
    testFrameworks += new TestFramework("zio.test.sbt.ZTestFramework")
  )



ThisBuild / assemblyMergeStrategy := {
  case PathList("io", "netty", xs @ _*)         => MergeStrategy.first
  case PathList("com", "fasterxml", xs @ _*)         => MergeStrategy.first
  case PathList("org", "reactivestreams", xs @ _*)         => MergeStrategy.first
  case PathList("ch", "qos", xs @ _*)         => MergeStrategy.first
  case PathList("com", "outr", xs @ _*)         => MergeStrategy.first
  case PathList("javax", "annotation", xs @ _*)         => MergeStrategy.first
  //case PathList("org", "apache", "tomcat-annotations-api", xs @ _*)         => MergeStrategy.first
  //case PathList("javax", "activation", "activation", xs @ _*)         => MergeStrategy.discard
  case PathList("com", "sun", "activation", xs @ _*)         => MergeStrategy.last
  case PathList(ps @ _*) if ps.last endsWith "javamail.providers" => MergeStrategy.last
  case PathList(ps @ _*) if ps.last endsWith "io.netty.versions.properties" => MergeStrategy.last
  case PathList(ps @ _*) if ps.last endsWith "native-image.properties" => MergeStrategy.last
  case PathList(ps @ _*) if ps.last endsWith "reflection-config.json" => MergeStrategy.last
  case PathList(ps @ _*) if ps.last endsWith "module-info.class" => MergeStrategy.last
  case PathList(ps @ _*) if ps.last endsWith "StaticLoggerBinder.class" => MergeStrategy.last
  case PathList(ps @ _*) if ps.last endsWith "StaticMDCBinder.class" => MergeStrategy.last
  case PathList(ps @ _*) if ps.last endsWith "ActivationDataFlavor.class" => MergeStrategy.last
  case PathList(ps @ _*) if ps.last endsWith "CommandInfo.class" => MergeStrategy.last
  case PathList(ps @ _*) if ps.last endsWith "CommandMap.class" => MergeStrategy.last
  case PathList(ps @ _*) if ps.last endsWith "CommandObject.class" => MergeStrategy.last
  case PathList(ps @ _*) if ps.last endsWith "DataContentHandler.class" => MergeStrategy.last
  case PathList(ps @ _*) if ps.last endsWith "DataHandlerDataSource.class" => MergeStrategy.last
  case PathList(ps @ _*) if ps.last endsWith "DataSource.class" => MergeStrategy.last
  case PathList(ps @ _*) if ps.last endsWith "DataSourceDataContentHandler.class" => MergeStrategy.last
  case PathList(ps @ _*) if ps.last endsWith "FileDataSource.class" => MergeStrategy.last
  case PathList(ps @ _*) if ps.last endsWith "FileTypeMap.class" => MergeStrategy.last
  case PathList(ps @ _*) if ps.last endsWith "MailcapCommandMap.class" => MergeStrategy.last
  case PathList(ps @ _*) if ps.last endsWith "MimeTypeParameterList.class" => MergeStrategy.last
  case PathList(ps @ _*) if ps.last endsWith "MimeTypeParseException.class" => MergeStrategy.last
  case PathList(ps @ _*) if ps.last endsWith "MimetypesFileTypeMap.class" => MergeStrategy.last
  case PathList(ps @ _*) if ps.last endsWith "SecuritySupport$3.class" => MergeStrategy.last
  case PathList(ps @ _*) if ps.last endsWith "SecuritySupport$4.class" => MergeStrategy.last
  case PathList(ps @ _*) if ps.last endsWith "SecuritySupport$5.class" => MergeStrategy.last
  case PathList(ps @ _*) if ps.last endsWith "SecuritySupport.class" => MergeStrategy.last
  case PathList(ps @ _*) if ps.last endsWith "URLDataSource.class" => MergeStrategy.last
  case PathList(ps @ _*) if ps.last endsWith "UnsupportedDataTypeException.class" => MergeStrategy.last
  case PathList(ps @ _*) if ps.last endsWith "ObjectDataContentHandler.class" => MergeStrategy.last
  case PathList(ps @ _*) if ps.last endsWith "UnsupportedDataTypeException.class" => MergeStrategy.last
  case PathList(ps @ _*) if ps.last endsWith "LineTokenizer.class" => MergeStrategy.last
  case PathList(ps @ _*) if ps.last endsWith "DataHandlerDataSource.class" => MergeStrategy.last
  case PathList(ps @ _*) if ps.last endsWith "FileDataSource.class" => MergeStrategy.last
  case PathList(ps @ _*) if ps.last endsWith "FileTypeMap.class" => MergeStrategy.last
  case PathList(ps @ _*) if ps.last endsWith "MailcapCommandMap.class" => MergeStrategy.last
  case PathList(ps @ _*) if ps.last endsWith "MimeTypeParseException.class" => MergeStrategy.last
  case PathList(ps @ _*) if ps.last endsWith "SecuritySupport$1.class" => MergeStrategy.last
  case PathList(ps @ _*) if ps.last endsWith "SecuritySupport$2.class" => MergeStrategy.last
  case PathList(ps @ _*) if ps.last endsWith "DataContentHandlerFactory.class" => MergeStrategy.last
  case PathList(ps @ _*) if ps.last endsWith "DataHandler$1.class" => MergeStrategy.last
  case PathList(ps @ _*) if ps.last endsWith "DataHandler.class" => MergeStrategy.last
  case PathList(ps @ _*) if ps.last endsWith "MimeType.class" => MergeStrategy.last
  case PathList(ps @ _*) if ps.last endsWith ".html" => MergeStrategy.first
  case "application.conf"                            => MergeStrategy.concat
  case "unwanted.txt"                                => MergeStrategy.discard
  case x =>
    val oldStrategy = (ThisBuild / assemblyMergeStrategy).value
    oldStrategy(x)
}
