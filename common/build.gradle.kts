plugins {
    id("com.possible-triangle.common")
}

common {
    accessWidener()
}

val moonlight_version: String by extra

dependencies {
    modCompileOnly("net.mehvahdjukaar:moonlight-neoforge:${moonlight_version}")
    accessTransformers("net.mehvahdjukaar:moonlight-neoforge:${moonlight_version}")

    // neoforge build of EMI (common compiles against the neoforge/mojmap mc); the fabric
    // build (6420930) leaks intermediary names like class_332 and breaks the common compile
    modImplementation("curse.maven:emi-580555:6205506")
    modCompileOnly("curse.maven:jei-238222:5846880")
    modCompileOnly("me.shedaniel:RoughlyEnoughItems-neoforge:16.0.777")
    modCompileOnly("curse.maven:rhino-416294:5589424")
}
