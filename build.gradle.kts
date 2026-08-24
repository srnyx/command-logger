plugins {
    java
    id("xyz.srnyx.gradle-galaxy") version "c151767"
    id("com.gradleup.shadow") version "9.6.1"
    id("me.modmuss50.mod-publish-plugin") version "675051c"
    id("io.papermc.hangar-publish-plugin") version "0.1.4"
    id("xyz.jpenilla.run-paper") version "3.1.0"
}

group = "com.srnyx"
description = "Log commands executed by players and console to one file or multiple files"

galaxy {
    minecraft {
        paper("1.8.8")
        annoyingAPI("45ae893")

        dependency {
            optional {
                repositories.add(PLACEHOLDER_API)
                group = "me.clip"
                artifact = "placeholderapi"
                version = "2.12.2"

                pluginYml = "PlaceholderAPI"
                modrinth = "placeholderapi"
                hangar = "PlaceholderAPI"
            }
        }

        pluginYml {
            developerData(SRNYX)

            command("commandloggerreload") {
                aliases.add("clreload")
                description = "Reloads the Command Logger configuration"

                permission("reload")
            }
        }

        platformPublishing {
            github("srnyx/command-logger")
            modrinth("PMWx2eoO")
            hangar("CommandLogger")
            spigot("126150")
            curseforge("1289091")

            projectData("command-logger")
        }
    }

    testing {
        jUnit("6.1.0")
        mockBukkit("3.9.0")
    }
}
