plugins {
    java
    id("io.papermc.paperweight.userdev") version "2.0.0-beta.21" apply false
    // 静态分析: pmd 是 Gradle 核心插件 (无需声明版本), spotbugs 只对
    // fakeplayer-api / fakeplayer-core 启用; 版本模块是 NMS 代码且编译到 Java 25,
    // SpotBugs 读不了那么新的 class 文件
    id("com.github.spotbugs") version "6.5.11" apply false
}

allprojects {
    group = "io.github.hello09x.fakeplayer"
    version = "fp.build11"

    repositories {
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
        compileOnly("org.projectlombok:lombok:1.18.36")
        annotationProcessor("org.projectlombok:lombok:1.18.36")
        compileOnly("com.mojang:authlib:4.0.43")
        compileOnly("dev.jorel:commandapi-paper-core:12.0.0")
        compileOnly("com.mojang:brigadier:1.1.8")
        compileOnly("io.netty:netty-transport:4.1.82.Final")
    }
}
