plugins {
    id("io.papermc.paperweight.userdev")
}

dependencies {
    compileOnly(project(":fakeplayer-core"))
    compileOnly(project(":fakeplayer-api"))
    compileOnly(project(":fakeplayer-v1_21_9"))
    paperweight.paperDevBundle("1.21.10-R0.1-SNAPSHOT")
    compileOnly("com.github.tanyaofei.devtools:devtools-core:0.1.7-SNAPSHOT")
}
