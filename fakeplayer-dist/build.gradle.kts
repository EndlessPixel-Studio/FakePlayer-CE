import java.util.zip.ZipFile

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}

tasks.named<Jar>("jar") {
    enabled = false
}

dependencies {
    compileOnly(project(":fakeplayer-core"))
    compileOnly(project(":fakeplayer-api"))
    compileOnly(project(":fakeplayer-v1_20_1"))
    compileOnly(project(":fakeplayer-v1_20_2"))
    compileOnly(project(":fakeplayer-v1_20_3"))
    compileOnly(project(":fakeplayer-v1_20_4"))
    compileOnly(project(":fakeplayer-v1_20_5"))
    compileOnly(project(":fakeplayer-v1_20_6"))
    compileOnly(project(":fakeplayer-v1_21"))
    compileOnly(project(":fakeplayer-v1_21_1"))
    compileOnly(project(":fakeplayer-v1_21_3"))
    compileOnly(project(":fakeplayer-v1_21_4"))
    compileOnly(project(":fakeplayer-v1_21_5"))
    compileOnly(project(":fakeplayer-v1_21_6"))
    compileOnly(project(":fakeplayer-v1_21_7"))
    compileOnly(project(":fakeplayer-v1_21_8"))
    compileOnly(project(":fakeplayer-v1_21_9"))
    compileOnly(project(":fakeplayer-v1_21_10"))
    compileOnly(project(":fakeplayer-v1_21_11"))
    compileOnly(project(":fakeplayer-v26_1"))
    compileOnly(project(":fakeplayer-v26_1_1"))
    compileOnly(project(":fakeplayer-v26_1_2"))
    compileOnly(project(":fakeplayer-v26_2"))
}

tasks.register<Jar>("shadowJar") {
    archiveFileName.set("fakeplayer-${version}.jar")
    dependsOn(":fakeplayer-core:build")
    dependsOn(":fakeplayer-api:build")
    dependsOn(":fakeplayer-v1_20_1:build")
    dependsOn(":fakeplayer-v1_20_2:build")
    dependsOn(":fakeplayer-v1_20_3:build")
    dependsOn(":fakeplayer-v1_20_4:build")
    dependsOn(":fakeplayer-v1_20_5:build")
    dependsOn(":fakeplayer-v1_20_6:build")
    dependsOn(":fakeplayer-v1_21:build")
    dependsOn(":fakeplayer-v1_21_1:build")
    dependsOn(":fakeplayer-v1_21_3:build")
    dependsOn(":fakeplayer-v1_21_4:build")
    dependsOn(":fakeplayer-v1_21_5:build")
    dependsOn(":fakeplayer-v1_21_6:build")
    dependsOn(":fakeplayer-v1_21_7:build")
    dependsOn(":fakeplayer-v1_21_8:build")
    dependsOn(":fakeplayer-v1_21_9:build")
    dependsOn(":fakeplayer-v1_21_10:build")
    dependsOn(":fakeplayer-v1_21_11:build")
    dependsOn(":fakeplayer-v26_1:build")
    dependsOn(":fakeplayer-v26_1_1:build")
    dependsOn(":fakeplayer-v26_1_2:build")
    dependsOn(":fakeplayer-v26_2:build")

    val coreJar = project(":fakeplayer-core").tasks.named<Jar>("jar")
    from(coreJar.map { zipTree(it.archiveFile.get().asFile) })
    from(project(":fakeplayer-api").sourceSets.main.get().output)
    from(project(":fakeplayer-v1_20_1").sourceSets.main.get().output)
    from(project(":fakeplayer-v1_20_2").sourceSets.main.get().output)
    from(project(":fakeplayer-v1_20_3").sourceSets.main.get().output)
    from(project(":fakeplayer-v1_20_4").sourceSets.main.get().output)
    from(project(":fakeplayer-v1_20_5").sourceSets.main.get().output)
    from(project(":fakeplayer-v1_20_6").sourceSets.main.get().output)
    from(project(":fakeplayer-v1_21").sourceSets.main.get().output)
    from(project(":fakeplayer-v1_21_1").sourceSets.main.get().output)
    from(project(":fakeplayer-v1_21_3").sourceSets.main.get().output)
    from(project(":fakeplayer-v1_21_4").sourceSets.main.get().output)
    from(project(":fakeplayer-v1_21_5").sourceSets.main.get().output)
    from(project(":fakeplayer-v1_21_6").sourceSets.main.get().output)
    from(project(":fakeplayer-v1_21_7").sourceSets.main.get().output)
    from(project(":fakeplayer-v1_21_8").sourceSets.main.get().output)
    from(project(":fakeplayer-v1_21_9").sourceSets.main.get().output)
    from(project(":fakeplayer-v1_21_10").sourceSets.main.get().output)
    from(project(":fakeplayer-v1_21_11").sourceSets.main.get().output)
    from(project(":fakeplayer-v26_1").sourceSets.main.get().output)
    from(project(":fakeplayer-v26_1_1").sourceSets.main.get().output)
    from(project(":fakeplayer-v26_1_2").sourceSets.main.get().output)
    from(project(":fakeplayer-v26_2").sourceSets.main.get().output)

    // Include fakeplayer-dist's own resources (SPI services, etc.)
    from(sourceSets.main.get().output)

    // Include runtime dependencies (devtools + Guice) from fakeplayer-core
    val runtimeClasspath = project(":fakeplayer-core").configurations.runtimeClasspath.get()
    from(runtimeClasspath.map { if (it.isDirectory) it else zipTree(it) })

    // Handle duplicate service files from dependency JARs
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE

    doLast {
        ZipFile(archiveFile.get().asFile).use { jar ->
            check(jar.getEntry("plugin.yml") != null) { "Distribution JAR is missing plugin.yml" }
            val services = jar.getEntry("META-INF/services/io.github.hello09x.fakeplayer.api.spi.NMSBridge")
            check(services != null) {
                "Distribution JAR is missing the NMS bridge service descriptor"
            }
            val providers = jar.getInputStream(services).bufferedReader().useLines { lines ->
                lines.map(String::trim).filter { it.isNotEmpty() && !it.startsWith("#") }.toList()
            }
            val expectedProviders = setOf(
                "io.github.hello09x.fakeplayer.v1_20_1.spi.NMSBridgeImpl",
                "io.github.hello09x.fakeplayer.v1_20_2.spi.NMSBridgeImpl",
                "io.github.hello09x.fakeplayer.v1_20_3.spi.NMSBridgeImpl",
                "io.github.hello09x.fakeplayer.v1_20_4.spi.NMSBridgeImpl",
                "io.github.hello09x.fakeplayer.v1_20_5.spi.NMSBridgeImpl",
                "io.github.hello09x.fakeplayer.v1_20_6.spi.NMSBridgeImpl",
                "io.github.hello09x.fakeplayer.v1_21.spi.NMSBridgeImpl",
                "io.github.hello09x.fakeplayer.v1_21_1.spi.NMSBridgeImpl",
                "io.github.hello09x.fakeplayer.v1_21_3.spi.NMSBridgeImpl",
                "io.github.hello09x.fakeplayer.v1_21_4.spi.NMSBridgeImpl",
                "io.github.hello09x.fakeplayer.v1_21_5.spi.NMSBridgeImpl",
                "io.github.hello09x.fakeplayer.v1_21_6.spi.NMSBridgeImpl",
                "io.github.hello09x.fakeplayer.v1_21_7.spi.NMSBridgeImpl",
                "io.github.hello09x.fakeplayer.v1_21_8.spi.NMSBridgeImpl",
                "io.github.hello09x.fakeplayer.v1_21_9.spi.NMSBridgeImpl",
                "io.github.hello09x.fakeplayer.v1_21_10.spi.NMSBridgeImpl",
                "io.github.hello09x.fakeplayer.v1_21_11.spi.NMSBridgeImpl",
                "io.github.hello09x.fakeplayer.v26_1.spi.NMSBridgeImpl",
                "io.github.hello09x.fakeplayer.v26_1_1.spi.NMSBridgeImpl",
                "io.github.hello09x.fakeplayer.v26_1_2.spi.NMSBridgeImpl",
                "io.github.hello09x.fakeplayer.v26_2.spi.NMSBridgeImpl"
            )
            check(providers.toSet() == expectedProviders) {
                "Distribution JAR has an incomplete NMS bridge provider set"
            }
            providers.forEach { provider ->
                check(jar.getEntry(provider.replace('.', '/') + ".class") != null) {
                    "Distribution JAR is missing NMS bridge provider $provider"
                }
            }
        }
    }
}

tasks.named("assemble") {
    dependsOn("shadowJar")
}
