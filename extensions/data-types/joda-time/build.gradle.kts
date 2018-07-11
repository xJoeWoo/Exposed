import org.gradle.internal.impldep.org.fusesource.jansi.AnsiRenderer.test
import org.gradle.internal.impldep.org.junit.experimental.categories.Categories.CategoryFilter.include
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.net.URI

val exposed_version = rootProject.findProperty("version")
group = "org.jetbrains.exposed.extensions"

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
    plugin("kotlin")
}


repositories {
    mavenCentral()
}

dependencies {
    compile(rootProject)
    compile("joda-time:joda-time:2.9.9")
    testCompile("joda-time:joda-time:2.9.9")
}


