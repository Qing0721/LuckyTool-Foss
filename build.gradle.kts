buildscript {
    dependencies {
        classpath("com.joom.paranoid:paranoid-gradle-plugin:0.3.14")
        classpath("com.github.megatronking.stringfog:gradle-plugin:5.1.0")
        classpath("com.github.megatronking.stringfog:xor:5.0.0")
    }
}
plugins {
    id("com.android.application") version "8.10.1" apply false
    id("com.android.library") version "8.10.1" apply false
    id("org.jetbrains.kotlin.android") version "2.2.0" apply false
}

tasks {
    register("clean", Delete::class) {
        delete(layout.buildDirectory)
    }
}
