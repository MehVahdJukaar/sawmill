plugins {
    id("com.possible-triangle.fabric")
}

fabric {
    dependOn(project(":common"))
    accessWidener(project(":common"))
}

val mc_version: String by extra
val moonlight_version: String by extra
val codecui_version: String by extra
val jei_version: String by extra
val rei_version: String by extra
val rrv_version: String by extra
val modmenu_version: String by extra

dependencies {
    modImplementation("net.mehvahdjukaar:moonlight-fabric:${moonlight_version}")
    modRuntimeOnly("net.mehvahdjukaar:codecui-fabric:${codecui_version}")

    modCompileOnly("mezz.jei:jei-${mc_version}-fabric-api:${jei_version}")

    modCompileOnly("me.shedaniel:RoughlyEnoughItems-fabric:${rei_version}")
    modCompileOnly("me.shedaniel:RoughlyEnoughItems-api:${rei_version}")
    modCompileOnly("me.shedaniel:RoughlyEnoughItems-neoforge:${rei_version}")

    modImplementation("cc.cassian.rrv:reliable-recipe-viewer-fabric:${rrv_version}+${mc_version}")

    modCompileOnly("com.terraformersmc:modmenu:${modmenu_version}") {
        exclude(module = "fabric-api")
    }
}
