plugins {
    id("com.possible-triangle.fabric")
}

fabric {
    dependOn(project(":common"))
    accessWidener(project(":common"))
}

val moonlight_version: String by extra

dependencies {
    modImplementation("net.mehvahdjukaar:moonlight-fabric:${moonlight_version}")

    // Mirror of common deps
    modImplementation("curse.maven:emi-580555:6420930")
    modCompileOnly("curse.maven:jei-238222:5846880")
    // neoforge REI build (compileOnly) so common's @REIPluginClient (me.shedaniel.rei.forge.*)
    // resolves when fabric recompiles common sources; never bundled, fabric registers via entrypoint
    modCompileOnly("me.shedaniel:RoughlyEnoughItems-neoforge:16.0.777")
    modCompileOnly("curse.maven:rhino-416294:5589424")

    modCompileOnly("curse.maven:yacl-667299:4574163")
    modCompileOnly("curse.maven:architectury-api-419699:5553799")

    modCompileOnly("com.terraformersmc:modmenu:11.0.3") {
        exclude(module = "fabric-api")
    }
}
