plugins {
    id("com.possible-triangle.common")
}

common {
    accessWidener()
}

val moonlight_version: String by extra

dependencies {
    //@jar skips moonlight's module metadata: its jar variants are tagged neoforge-only, so in this module gradle
    //would otherwise fall back to the access transformer variant and the whole api would be missing from the classpath
    modCompileOnly("net.mehvahdjukaar:moonlight-neoforge:${moonlight_version}@jar")
    accessTransformers("net.mehvahdjukaar:moonlight-neoforge:${moonlight_version}")

    // neoforge build of EMI (common compiles against the neoforge/mojmap mc); the fabric
    // build (6420930) leaks intermediary names like class_332 and breaks the common compile
    modImplementation("curse.maven:emi-580555:6205506")
    modCompileOnly("curse.maven:jei-238222:5846880")
    modCompileOnly("me.shedaniel:RoughlyEnoughItems-neoforge:16.0.777")
    modCompileOnly("curse.maven:rhino-416294:5589424")
}
