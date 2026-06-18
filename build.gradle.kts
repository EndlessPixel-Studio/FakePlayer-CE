plugins {
    java
    id("com.github.johnrengelman.shadow") version "8.1.1" apply false
}

allprojects {
    group = "io.github.hello09x.fakeplayer"
    version = "0.3.19"

    repositories {
        mavenLocal()  // 本地 Maven 仓库，包含 BuildTools 构建的 spigot
        mavenCentral()
        maven("https://libraries.minecraft.net/")
        maven("https://repo.papermc.io/repository/maven-public/")
        maven("https://oss.sonatype.org/content/groups/public/")
        maven("https://repo.dmulloy2.net/repository/public/")
        maven("https://jitpack.io")
        maven("https://repo.extendedclip.com/content/repositories/placeholderapi/")
    }
}

subprojects {
    apply(plugin = "java")

    java {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
        toolchain {
            languageVersion.set(JavaLanguageVersion.of(21))
        }
    }

    dependencies {
        compileOnly("io.papermc.paper:paper-api:1.21.7-R0.1-SNAPSHOT")
        compileOnly("org.projectlombok:lombok:1.18.36")
        annotationProcessor("org.projectlombok:lombok:1.18.36")
        compileOnly("com.mojang:authlib:4.0.43")
        compileOnly("dev.jorel:commandapi-paper-core:11.0.0")
        compileOnly("com.mojang:brigadier:1.1.8")
        compileOnly("io.netty:netty-transport:4.1.82.Final")
    }
}