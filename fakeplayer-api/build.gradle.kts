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
    compileOnly("io.papermc.paper:paper-api:1.21-R0.1-SNAPSHOT")
    compileOnly("org.jetbrains:annotations:26.1.0")
    
    // Adventure API for text components
    compileOnly("net.kyori:adventure-api:5.2.0")
}
