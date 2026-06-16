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
}

sourceSets.named("main") {
    resources.srcDir("src/generated/resources")
}
