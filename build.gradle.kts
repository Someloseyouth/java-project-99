plugins {
    java
    checkstyle
    id("org.springframework.boot") version "4.0.6"
    id("io.spring.dependency-management") version "1.1.7"
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
    implementation("jakarta.validation:jakarta.validation-api:3.0.0")
    implementation("org.openapitools:jackson-databind-nullable:0.2.10")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310")
    implementation("org.mapstruct:mapstruct:1.6.3")
    implementation("org.springframework.security:spring-security-crypto:6.4.5")
    implementation("net.datafaker:datafaker:2.0.1")
    implementation("org.instancio:instancio-junit:3.3.0")
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
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

checkstyle {
    configDirectory.set(layout.projectDirectory.dir("config/checkstyle"))
}

tasks.withType<Test> {
    useJUnitPlatform()
}
