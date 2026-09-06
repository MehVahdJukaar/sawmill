plugins {
    id("com.possible-triangle.neoforge")
}

neoforge {
    dependOn(project(":common"))
    accessWidener(project(":common"))
}

val mc_version: String by extra
val moonlight_version: String by extra
val codecui_version: String by extra
val jei_version: String by extra
val rei_version: String by extra
val rrv_version: String by extra

dependencies {
    modImplementation("net.mehvahdjukaar:moonlight-neoforge:${moonlight_version}")
    accessTransformers("net.mehvahdjukaar:moonlight-neoforge:${moonlight_version}")

    modCompileOnly("mezz.jei:jei-${mc_version}-neoforge-api:${jei_version}")

    modCompileOnly("me.shedaniel:RoughlyEnoughItems-neoforge:${rei_version}")
    modCompileOnly("me.shedaniel:RoughlyEnoughItems-api:${rei_version}")

    modImplementation("cc.cassian.rrv:reliable-recipe-viewer-neoforge:${rrv_version}+${mc_version}")
}

sourceSets.named("main") {
    resources.srcDir("src/generated/resources")
}
