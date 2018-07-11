import org.gradle.internal.impldep.org.junit.experimental.categories.Categories.CategoryFilter.include
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.net.URI

val exposed_version = rootProject.findProperty("version")
group = "org.jetbrains.exposed.extensions.data-types"

version = exposed_version!!

buildscript {

    repositories {
        mavenCentral()
        maven(url = "http://dl.bintray.com/kotlin/kotlin-eap")
    }

    val kotlin_version : String by rootProject.extra

    dependencies {
        classpath(module("org.jetbrains.kotlin", "kotlin-gradle-plugin", "$kotlin_version"))
    }
}

apply {
    plugin("java")
    plugin("kotlin")
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

repositories {
    mavenCentral()
}

dependencies {
    compile(rootProject)
    testCompile(rootProject.files("/out/test/classes"))
}


