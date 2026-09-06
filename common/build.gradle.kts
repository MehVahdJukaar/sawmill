plugins {
    id("com.possible-triangle.common")
}

common {
    accessWidener()
}

val mc_version: String by extra
val moonlight_version: String by extra
val jei_version: String by extra
val rei_version: String by extra
val rei_annotations_version: String by extra
val rrv_version: String by extra

dependencies {
    //@jar skips moonlight's module metadata: its jar variants are tagged neoforge-only, so in this module gradle
    //would otherwise fall back to the access transformer variant and the whole api would be missing from the classpath
    modCompileOnly("net.mehvahdjukaar:moonlight-neoforge:${moonlight_version}@jar")
    accessTransformers("net.mehvahdjukaar:moonlight-neoforge:${moonlight_version}")

    modCompileOnly("mezz.jei:jei-${mc_version}-common-api:${jei_version}")

    modCompileOnly("me.shedaniel:RoughlyEnoughItems-api:${rei_version}")
    modCompileOnly("me.shedaniel:RoughlyEnoughItems-neoforge:${rei_version}")
    modCompileOnly("me.shedaniel:REIPluginCompatibilities-forge-annotations:${rei_annotations_version}")

    compileOnly("cc.cassian.rrv:reliable-recipe-viewer-neoforge:${rrv_version}+${mc_version}")
}
