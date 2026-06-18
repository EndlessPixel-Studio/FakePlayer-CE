plugins {
    id("io.papermc.paperweight.userdev")
}

dependencies {
    compileOnly(project(":fakeplayer-core"))
    compileOnly(project(":fakeplayer-api"))
    paperweight.paperDevBundle("1.21.7-R0.1-SNAPSHOT")
    compileOnly("com.github.tanyaofei.devtools:devtools-core:0.1.7-SNAPSHOT")
}
