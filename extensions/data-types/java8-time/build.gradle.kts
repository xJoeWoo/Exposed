import org.gradle.internal.impldep.org.junit.experimental.categories.Categories.CategoryFilter.include
//import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

val exposed_version = rootProject.findProperty("version")
group = "org.jetbrains.exposed.extensions.data-types"

version = exposed_version

buildscript {

    repositories {
        mavenCentral()
    }

    val kotlin_version : String by rootProject.extra
//    sourceCompatibility = 1.6
//    targetCompatibility = 1.6

    dependencies {
        classpath(module("org.jetbrains.kotlin", "kotlin-gradle-plugin", "$kotlin_version"))
    }
    
}

apply {
    plugin("java")
    plugin("kotlin")
}


repositories {
    mavenCentral()
}

dependencies {
    compile(rootProject)
    testCompile(rootProject)
}


