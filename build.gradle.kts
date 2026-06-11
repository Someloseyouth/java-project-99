plugins {
    java
    checkstyle
    jacoco
    id("org.springframework.boot") version "4.0.6"
    id("io.spring.dependency-management") version "1.1.7"
    id("org.sonarqube") version "7.3.1.8318"
    id("io.sentry.jvm.gradle") version "5.3.0"
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
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-oauth2-resource-server")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-devtools")
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:3.0.1")
    implementation("org.openapitools:jackson-databind-nullable:0.2.10")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310")
    implementation("org.mapstruct:mapstruct:1.6.3")
    implementation("io.sentry:sentry-spring-boot-4:8.33.0")


    compileOnly("org.projectlombok:lombok:1.18.42")
    annotationProcessor("org.projectlombok:lombok:1.18.42")
    annotationProcessor("org.mapstruct:mapstruct-processor:1.6.3")

    runtimeOnly("com.h2database:h2")
    runtimeOnly("org.postgresql:postgresql")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.boot:spring-boot-starter-webmvc-test")
    testImplementation("org.springframework.security:spring-security-test")
    testImplementation(platform("org.junit:junit-bom:5.12.0"))
    testImplementation("org.junit.jupiter:junit-jupiter:5.12.0")
    testImplementation("net.datafaker:datafaker:2.0.1")
    testImplementation("org.instancio:instancio-junit:3.3.0")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

checkstyle {
    configDirectory.set(layout.projectDirectory.dir("config/checkstyle"))
}

val coverageExclusions = listOf(
    "**/AppApplication*",
    "**/DataInitializer*",
    "**/config/**",
    "**/component/**",
    "**/mapper/ReferenceMapper*",
    "**/dto/**",
    "**/model/**",
    "**/exception/**"
)

sentry {
    includeSourceContext.set(true)
    org.set("someloseyouth")
    projectName.set("java-project-99")
    authToken.set(System.getenv("SENTRY_AUTH_TOKEN"))
}

sonar {
    properties {
        property("sonar.projectKey", "Someloseyouth_java-project-99")
        property("sonar.organization", "someloseyouth")
        property("sonar.coverage.exclusions", coverageExclusions.joinToString(","))
    }
}

tasks.test {
    finalizedBy(tasks.jacocoTestReport)
}

tasks.jacocoTestReport {
    dependsOn(tasks.test)
    reports {
        xml.required.set(true)
    }
    classDirectories.setFrom(
        files(classDirectories.files.map {
            fileTree(it) {
                exclude(coverageExclusions)
            }
        })
    )
}

tasks.withType<Test> {
    useJUnitPlatform()
}

configurations.all {
    if (name != "checkstyle" && name != "checkstyleMain" && name != "checkstyleTest") {
        resolutionStrategy.activateDependencyLocking()
    }
}
