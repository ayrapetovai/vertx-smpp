import org.gradle.api.tasks.testing.logging.TestLogEvent.*

plugins {
  java
}

group = "io.vertx"
version = "0.0.1-SNAPSHOT"

repositories {
  mavenCentral()
}

val vertxVersion = "4.2.5"
val logbackVersion = "1.2.10"
val junitJupiterVersion = "5.8.2"

dependencies {
  // vertx
  implementation(platform("io.vertx:vertx-stack-depchain:$vertxVersion"))
  implementation("io.vertx:vertx-core")

  // misc
  implementation("joda-time:joda-time:2.10.13") // needed by coludhopper classes
  // TODO remove
  implementation("com.cloudhopper:ch-commons-charset:3.0.2")

  // logs
  implementation("org.slf4j:slf4j-api:1.7.19")
  testImplementation("ch.qos.logback:logback-core:$logbackVersion")
  testImplementation("ch.qos.logback:logback-classic:$logbackVersion")

  // demo
  testImplementation("io.vertx:vertx-mail-client:$vertxVersion")
  testImplementation("org.subethamail:subethasmtp:3.1.7")

  testImplementation("junit:junit:4.13.1")
  testImplementation("org.mockito:mockito-core:4.4.0")
  testImplementation("io.vertx:vertx-unit:4.2.6")

  testImplementation("io.vertx:vertx-junit5")
  testImplementation("org.junit.jupiter:junit-jupiter:$junitJupiterVersion")
}

java {
  sourceCompatibility = JavaVersion.VERSION_11
  targetCompatibility = JavaVersion.VERSION_11
}

tasks.withType<Test> {
  useJUnitPlatform()
  testLogging {
    events = setOf(PASSED, SKIPPED, FAILED)
  }
}

