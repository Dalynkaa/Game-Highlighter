plugins {
    id("dev.kikugie.stonecutter")
    id("fabric-loom") version "1.9-SNAPSHOT" apply false
}
stonecutter active "1.21.1" /* [SC] DO NOT EDIT */

stonecutter registerChiseled tasks.register("chiseledBuild", stonecutter.chiseled) {
    group = "project"
    ofTask("build")
}

stonecutter parameters {
    swap("mod_version", "\"${property("mod.version")}\";")
    const("release", property("mod.id") != "highlighter")
}
