plugins {
    java
    id("io.papermc.paperweight.userdev") version "2.0.0-beta.21" apply false
    // 静态分析: pmd 是 Gradle 核心插件 (无需声明版本), spotbugs 只对
    // fakeplayer-api / fakeplayer-core 启用; 其余模块 (版本模块) 未启用
    id("com.github.spotbugs") version "6.5.13" apply false
    // 格式化: google-java-format, 只做校验 (spotlessCheck), 不自动改文件
    id("com.diffplug.spotless") version "8.9.0" apply false
}

allprojects {
    group = "io.github.hello09x.fakeplayer"
    version = "fp.build20"

    repositories {
        mavenCentral()
        maven("https://libraries.minecraft.net/")
        maven("https://repo.papermc.io/repository/maven-public/")
        maven("https://oss.sonatype.org/content/groups/public/")
        maven("https://repo.dmulloy2.net/repository/public/")
        maven("https://jitpack.io")
    }
}

subprojects {
    apply(plugin = "java")
    apply(plugin = "com.diffplug.spotless")

    extensions.configure<com.diffplug.gradle.spotless.SpotlessExtension> {
        java {
            target("src/main/java/**/*.java", "src/test/java/**/*.java")
            // 与命令行 google-java-format 的输出保持完全一致, 不叠加 removeUnusedImports
            googleJavaFormat("1.28.0")
        }
    }

    java {
        // Java 25: Paper 26.x 的 paper-api 以 class file 69 (Java 25) 发布,
        // 引用它必须将编译目标提升至 25; 各版本模块可在此基础上覆盖
        sourceCompatibility = JavaVersion.VERSION_25
        targetCompatibility = JavaVersion.VERSION_25
        toolchain {
            languageVersion.set(JavaLanguageVersion.of(25))
        }
    }

    dependencies {
        compileOnly("org.projectlombok:lombok:1.18.48")
        annotationProcessor("org.projectlombok:lombok:1.18.48")
        compileOnly("com.mojang:authlib:4.0.43")
        compileOnly("dev.jorel:commandapi-paper-core:12.1.0")
        compileOnly("com.mojang:brigadier:1.1.8")
        compileOnly("io.netty:netty-transport:4.2.20.Final")
    }
}
