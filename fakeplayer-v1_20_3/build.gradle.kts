plugins {
    id("io.papermc.paperweight.userdev")
}

dependencies {
    compileOnly(project(":fakeplayer-core"))
    compileOnly(project(":fakeplayer-api"))
    compileOnly(project(":fakeplayer-v1_20_4"))
    paperweight.paperDevBundle("1.20.4-R0.1-SNAPSHOT")
    compileOnly("com.github.tanyaofei.devtools:devtools-core:0.1.7-SNAPSHOT")
}
