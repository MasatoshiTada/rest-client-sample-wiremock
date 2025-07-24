plugins {
  java
  id("org.springframework.boot") version "3.5.3"
  id("io.spring.dependency-management") version "1.1.7"
}

group = "com.example"
version = "0.0.1-SNAPSHOT"

java {
  toolchain {
    languageVersion = JavaLanguageVersion.of(21)
  }
}

repositories {
  mavenCentral()
}

dependencies {
  implementation("org.springframework.boot:spring-boot-starter-web")
  implementation("org.apache.httpcomponents.client5:httpclient5")
  testImplementation("org.springframework.boot:spring-boot-starter-test")
  testRuntimeOnly("org.junit.platform:junit-platform-launcher")
  // org.wiremock:wiremockを使用すると、Jettyが足りないため実行時例外になる。
  // 下記はWireMock+Jettyが含まれているので、正常に実行できる。
  testImplementation("org.wiremock:wiremock-jetty12:3.13.1")
}

tasks.withType<Test> {
  useJUnitPlatform()
}
