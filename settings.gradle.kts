pluginManagement {
    repositories {
        gradlePluginPortal()
        maven("https://repo.papermc.io/repository/maven-public/")
    }
}

rootProject.name = "fakeplayer-parent"

include("fakeplayer-api")
include("fakeplayer-core")
include("fakeplayer-dist")

// 版本兼容模块统一收纳于 versions/ 目录, 项目路径与任务名保持不变
listOf(
    "fakeplayer-v1_20_1",
    "fakeplayer-v1_20_2",
    "fakeplayer-v1_20_3",
    "fakeplayer-v1_20_4",
    "fakeplayer-v1_20_5",
    "fakeplayer-v1_20_6",
    "fakeplayer-v1_21",
    "fakeplayer-v1_21_1",
    "fakeplayer-v1_21_3",
    "fakeplayer-v1_21_4",
    "fakeplayer-v1_21_5",
    "fakeplayer-v1_21_6",
    "fakeplayer-v1_21_7",
    "fakeplayer-v1_21_8",
    "fakeplayer-v1_21_9",
    "fakeplayer-v1_21_10",
    "fakeplayer-v1_21_11",
    "fakeplayer-v26_1",
    "fakeplayer-v26_1_1",
    "fakeplayer-v26_1_2",
    "fakeplayer-v26_2",
).forEach {
    include(":$it")
    project(":$it").projectDir = file("versions/$it")
}
