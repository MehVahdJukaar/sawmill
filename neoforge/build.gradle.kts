plugins {
    id("com.possible-triangle.neoforge")
}

neoforge {
    dependOn(project(":common"))
    accessWidener(project(":common"))
}

val moonlight_version: String by extra

dependencies {
    modImplementation("net.mehvahdjukaar:moonlight-neoforge:${moonlight_version}")
    accessTransformers("net.mehvahdjukaar:moonlight-neoforge:${moonlight_version}")

    // Mirror of common deps
    modImplementation("curse.maven:emi-580555:6205506")
    modCompileOnly("curse.maven:jei-238222:5846880")
    modCompileOnly("me.shedaniel:RoughlyEnoughItems-neoforge:16.0.777")
    modCompileOnly("curse.maven:rhino-416294:5589424")

    modCompileOnly("curse.maven:repurposed-structures-368293:4823487")
    modCompileOnly("curse.maven:framedblocks-441647:5143589")

    // issue 127 repro
    modRuntimeOnly(":natures-spirit:2.3.2-1.21.1")
    modRuntimeOnly(":lithostitched:1.8.1-neoforge-21.1")
    modRuntimeOnly(":biomes-o-plenty:21.1.0.14")
    modRuntimeOnly(":glitchcore:2.1.0.2")
    modRuntimeOnly(":terrablender:4.1.0.8")
}

sourceSets.named("main") {
    resources.srcDir("src/generated/resources")
}
