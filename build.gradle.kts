plugins {
java
id("org.springframework.boot") version "4.0.6"
id("io.spring.dependency-management") version "1.1.7"
id("org.sonarqube") version "6.0.1.5171"
}

group = "hexlet.code"
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
implementation("org.springframework.boot:spring-boot-starter")
implementation("org.springframework.boot:spring-boot-starter-webmvc")
implementation("org.springframework.boot:spring-boot-devtools")
testImplementation("org.springframework.boot:spring-boot-starter-test")
testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.withType<Test> {
useJUnitPlatform()
}
