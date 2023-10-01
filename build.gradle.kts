import org.springframework.boot.gradle.tasks.bundling.BootJar

plugins {
    java
    id("org.springframework.boot") version "3.1.3"
    id("io.spring.dependency-management") version "1.1.3"
}

group = "me.vladislav"
version = "0.0.1-SNAPSHOT"
val outName: String = "${rootProject.name}-$version.jar"
val bashExecutable: String = "sfs"

java {
    sourceCompatibility = JavaVersion.VERSION_17
}

configurations {
    compileOnly {
        extendsFrom(configurations.annotationProcessor.get())
    }
}

repositories {
    mavenCentral()
}

dependencies {
    // spring starter
    implementation("org.springframework.boot:spring-boot-starter")
    implementation("org.springframework.boot:spring-boot-starter-aop")
    compileOnly("org.projectlombok:lombok")
    developmentOnly("org.springframework.boot:spring-boot-devtools")
    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")
    annotationProcessor("org.projectlombok:lombok")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testCompileOnly("org.projectlombok:lombok")
    testAnnotationProcessor("org.projectlombok:lombok")

    implementation("com.google.guava:guava:32.1.2-jre")
    implementation("net.sf.trove4j:trove4j:3.0.3")
    implementation("org.apache.commons:commons-compress:1.24.0")
    implementation("commons-codec:commons-codec:1.16.0")
}

tasks.withType<Test> {
    useJUnitPlatform()
}

tasks.named<BootJar>("bootJar") {
    launchScript()
}

val bootJar: BootJar = tasks.bootJar.get()

val makeBashExecutableBin: Exec = tasks.register<Exec>("makeBashExecutableBin") {
    dependsOn(bootJar)
    group = "build"
    workingDir(bootJar.destinationDirectory.get())
    commandLine(
        "sh", "-c", "echo '#!/usr/bin/java -jar' > $bashExecutable | " +
                "cat $outName >> $bashExecutable | " +
                "chmod +x $bashExecutable"
    )
}.get()

val build: DefaultTask = tasks.build.get()

build.dependsOn(makeBashExecutableBin)