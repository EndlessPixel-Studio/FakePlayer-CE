plugins {
    id("io.papermc.paperweight.userdev")
}

java {
    sourceCompatibility = JavaVersion.VERSION_25
    targetCompatibility = JavaVersion.VERSION_25
    toolchain {
        languageVersion = JavaLanguageVersion.of(25)
    }
}

dependencies {
    compileOnly(project(":fakeplayer-api"))
    compileOnly(project(":fakeplayer-core"))
    compileOnly("org.projectlombok:lombok:1.18.42")
    annotationProcessor("org.projectlombok:lombok:1.18.42")
    paperweight.paperDevBundle("26.2.build.+")
    compileOnly("com.github.tanyaofei.devtools:devtools-core:0.1.7-SNAPSHOT")
}

tasks.named("reobfJar") {
    enabled = false
}
