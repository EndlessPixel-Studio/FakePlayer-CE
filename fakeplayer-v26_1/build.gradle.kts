plugins {
    id("io.papermc.paperweight.userdev")
}

dependencies {
    compileOnly(project(":fakeplayer-core"))
    compileOnly(project(":fakeplayer-api"))
    paperweight.paperDevBundle("26.1.2.build.+")
    compileOnly("com.github.tanyaofei.devtools:devtools-core:0.1.7-SNAPSHOT")
}

paperweight.reobfArtifactConfiguration = io.papermc.paperweight.userdev.ReobfArtifactConfiguration.MOJANG_PRODUCTION
