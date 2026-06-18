plugins {
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}

dependencies {
    implementation(project(":fakeplayer-core"))
    implementation(project(":fakeplayer-api"))
    implementation(project(":fakeplayer-v1_20_1"))
    implementation(project(":fakeplayer-v1_20_2"))
    implementation(project(":fakeplayer-v1_20_3"))
    implementation(project(":fakeplayer-v1_20_4"))
    implementation(project(":fakeplayer-v1_20_5"))
    implementation(project(":fakeplayer-v1_20_6"))
    implementation(project(":fakeplayer-v1_21"))
    implementation(project(":fakeplayer-v1_21_1"))
    implementation(project(":fakeplayer-v1_21_3"))
    implementation(project(":fakeplayer-v1_21_4"))
    implementation(project(":fakeplayer-v1_21_5"))
    implementation(project(":fakeplayer-v1_21_6"))
    implementation(project(":fakeplayer-v1_21_7"))
    implementation(project(":fakeplayer-v1_21_8"))
    implementation(project(":fakeplayer-v1_21_9"))
    implementation(project(":fakeplayer-v1_21_10"))
    implementation(project(":fakeplayer-v1_21_11"))
}

tasks.named<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar>("shadowJar") {
    archiveFileName.set("fakeplayer-${project.version}.jar")
}