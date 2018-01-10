import org.gradle.internal.impldep.org.junit.experimental.categories.Categories.CategoryFilter.include
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

val exposed_version = rootProject.findProperty("version")
group = "org.jetbrains.exposed.extensions"

version = exposed_version!!

buildscript {

    repositories {
        mavenCentral()
    }

    val kotlin_version : String by rootProject.extra

    dependencies {
        classpath(module("org.jetbrains.kotlin", "kotlin-gradle-plugin", "$kotlin_version"))
    }
    
}

apply {
    plugin("kotlin")
}


repositories {
    mavenCentral()
}

dependencies {
    compile(rootProject)
    compile("joda-time:joda-time:2.9.9")
}


