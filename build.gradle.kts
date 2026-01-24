plugins {
    kotlin("jvm") version "2.3.0"
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    // HTTP client for making requests
    implementation("com.squareup.okhttp3:okhttp:5.0.0-alpha.14")

    // HTML parser with CSS selector support
    implementation("org.jsoup:jsoup:1.18.1")

    // Kotlin coroutines for parallel scraping
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.1")

    // CSV export for scraped data
    implementation("com.github.doyaaaaaken:kotlin-csv-jvm:1.10.0")
    testImplementation(kotlin("test"))
}

kotlin {
    jvmToolchain(25)
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<JavaExec> {
    // 1. Аргументы для самой JVM
    jvmArgs = listOf("-Dfile.encoding=UTF-8", "-Dconsole.encoding=UTF-8")

}