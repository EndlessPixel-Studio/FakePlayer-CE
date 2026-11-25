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
    compileOnly("org.jetbrains:annotations:26.1.0")
    
    // devtools libraries
    implementation("com.github.tanyaofei.devtools:devtools-core:0.1.7-SNAPSHOT")
    implementation("com.github.tanyaofei.devtools:devtools-command:0.1.7-SNAPSHOT")
    implementation("com.github.tanyaofei.devtools:devtools-database:0.1.7-SNAPSHOT")
    
    // HTTP 管理接口: JDK 自带的 com.sun.net.httpserver 无法限制连接/请求体, 换 Undertow
    implementation("io.undertow:undertow-core:2.3.27.Final")

    // MySQL 驱动: 只有 database.type = mysql 时才会加载, 但必须打进产物 (服务端不自带)
    runtimeOnly("com.mysql:mysql-connector-j:9.1.0")

    // CommandAPI for command handling
    // 12.1.0 adds Minecraft 26.3 support (required for the v26_3 module)
    compileOnly("dev.jorel:commandapi-paper-core:12.1.0")
    
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
    // PlaceholderAPI: 官方仓库 (repo.helpch.at) 为单点且故障频繁, 直接使用 vendors 的 jar。
    // 升级时到 github.com/PlaceholderAPI/PlaceholderAPI/releases 下载替换 libs/ 下的文件
    compileOnly(files("libs/placeholderapi-2.12.3.jar"))
}

val pluginRevision: String = version.toString()

// 构建标识 commit 短号: CI 优先用 GITHUB_SHA (checkout 一定是触发构建的提交),
// 否则读本地 git HEAD; 两者都拿不到 (例如直接下发不含 .git 的源码包) 时回退 unknown。
fun resolveCommit(): String {
    System.getenv("GITHUB_SHA")
        ?.takeIf { it.isNotBlank() }
        ?.let { return it.take(7) }
    return try {
        val process =
            ProcessBuilder("git", "rev-parse", "--short=7", "HEAD")
                .directory(rootProject.projectDir)
                .redirectErrorStream(true)
                .start()
        val output = process.inputStream.bufferedReader().readText().trim()
        process.waitFor()
        if (process.exitValue() == 0 && output.isNotBlank()) output else "unknown"
    } catch (ignored: Exception) {
        "unknown"
    }
}

val pluginCommit: String = resolveCommit()

tasks.named<ProcessResources>("processResources") {
    inputs.property("revision", pluginRevision)
    inputs.property("commit", pluginCommit)
    filesMatching("plugin.yml") {
        expand(inputs.properties)
    }
    filesMatching("build-info.properties") {
        expand(inputs.properties)
    }
}
