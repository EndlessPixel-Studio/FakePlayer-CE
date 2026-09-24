plugins {
    id("pmd")
    id("com.github.spotbugs")
}

pmd {
    toolVersion = "7.27.0"
    ruleSetConfig = rootProject.resources.text.fromFile(rootProject.file("config/pmd/ruleset.xml"))
}

spotbugs {
    excludeFilter.set(rootProject.file("config/spotbugs/exclude-filter.xml"))
}

tasks.withType<Pmd>().configureEach {
    // 初始接入阶段不阻断构建, 仅生成报告; 后续视情况改为 false 作为门禁
    ignoreFailures = true
    reports {
        html.required.set(true)
        xml.required.set(true)
    }
}

tasks.withType<com.github.spotbugs.snom.SpotBugsTask>().configureEach {
    // 初始接入阶段不阻断构建, 仅生成报告
    ignoreFailures = true
    reports.maybeCreate("html").required.set(true)
    reports.maybeCreate("xml").required.set(true)
}

dependencies {
    compileOnly(project(":fakeplayer-api"))
    compileOnly("io.papermc.paper:paper-api:1.21-R0.1-SNAPSHOT")
    compileOnly("org.jetbrains:annotations:24.0.1")
    
    // devtools libraries
    implementation("com.github.tanyaofei.devtools:devtools-core:0.1.7-SNAPSHOT")
    implementation("com.github.tanyaofei.devtools:devtools-command:0.1.7-SNAPSHOT")
    implementation("com.github.tanyaofei.devtools:devtools-database:0.1.7-SNAPSHOT")
    
    // CommandAPI for command handling
    compileOnly("dev.jorel:commandapi-paper-core:12.0.0")
    
    // Adventure API for text components.
    // Shaded (implementation) so the plugin carries its own adventure copy: Leaf/Paper do not
    // expose the server's adventure to plugin classloaders, which caused devtools (which still
    // references net.kyori.adventure.translation.* and the removed UTF8ResourceBundleControl) to
    // fail with NoClassDefFoundError on newer servers (e.g. Leaf 26.2).
    implementation("net.kyori:adventure-api:4.17.0")
    implementation("net.kyori:adventure-text-minimessage:4.17.0")
    
    // Other dependencies
    compileOnly("commons-io:commons-io:2.22.0")
    compileOnly("com.github.lishid:openinv:4.1.8")
    compileOnly("me.clip:placeholderapi:2.12.3")
}

val pluginRevision: String = version.toString()

tasks.named<ProcessResources>("processResources") {
    inputs.property("revision", pluginRevision)
    filesMatching("plugin.yml") {
        expand(inputs.properties)
    }
}
