plugins {
    id("dev.kikugie.stonecutter")
    id("fabric-loom") version "1.10-SNAPSHOT" apply false
}
stonecutter active "1.21.2" /* [SC] DO NOT EDIT */

stonecutter registerChiseled tasks.register("chiseledBuild", stonecutter.chiseled) {
    group = "project"
    ofTask("buildAndCollect")
}

stonecutter registerChiseled tasks.register("chiseledPublishToModrinth", stonecutter.chiseled) {
    group = "project"
    ofTask("chiseledPublish")
}

stonecutter parameters {
    swap("mod_version", "\"${property("mod.version")}\";")
    const("release", property("mod.id") != "highlighter")
}
