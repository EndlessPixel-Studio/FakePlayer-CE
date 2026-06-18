dependencies {
    compileOnly(project(":fakeplayer-api"))
    compileOnly("io.papermc.paper:paper-api:1.21-R0.1-SNAPSHOT")
    compileOnly("org.jetbrains:annotations:24.0.1")
    
    // devtools libraries
    implementation("com.github.tanyaofei.devtools:devtools-core:0.1.7-SNAPSHOT")
    implementation("com.github.tanyaofei.devtools:devtools-command:0.1.7-SNAPSHOT")
    implementation("com.github.tanyaofei.devtools:devtools-database:0.1.7-SNAPSHOT")
    
    // CommandAPI for command handling
    compileOnly("dev.jorel:commandapi-paper-core:11.0.0")
    
    // Adventure API for text components
    compileOnly("net.kyori:adventure-api:4.17.0")
    compileOnly("net.kyori:adventure-text-minimessage:4.17.0")
    
    // Other dependencies
    compileOnly("commons-io:commons-io:2.7")
    compileOnly("com.github.lishid:openinv:4.1.8")
    compileOnly("me.clip:placeholderapi:2.11.6")
}