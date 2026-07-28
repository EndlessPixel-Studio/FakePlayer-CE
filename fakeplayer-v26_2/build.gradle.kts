plugins {
    id("io.papermc.paperweight.userdev")
}

dependencies {
    compileOnly(project(":fakeplayer-api"))
    compileOnly(project(":fakeplayer-core"))
    paperweight.paperDevBundle("26.2.build.+")
}

paperweight.reobfArtifactConfiguration = io.papermc.paperweight.userdev.ReobfArtifactConfiguration.MOJANG_PRODUCTION
