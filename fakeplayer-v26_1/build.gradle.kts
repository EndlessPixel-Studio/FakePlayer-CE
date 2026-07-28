plugins {
    java
    id("io.papermc.paperweight.userdev")
}

group = "io.github.hello09x.fakeplayer"
version = "0.0.1"

dependencies {
    compileOnly(project(":fakeplayer-core"))
    compileOnly(project(":fakeplayer-api"))
    paperweight.paperDevBundle("26.1-R0.1-SNAPSHOT")
    compileOnly("com.github.tanyaofei.devtools:devtools-core:0.1.7-SNAPSHOT")
}
