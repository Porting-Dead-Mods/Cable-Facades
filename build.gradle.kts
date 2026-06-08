plugins {
    id("dev.prism")
}

group = "com.portingdeadmods"
version = "2.1.2"

prism {
    metadata {
        modId = "cable_facades"
        name = "Cable Facades"
        description = "Adds facades for most cables in the game."
        license = "ARR"
        author("Leclowndu93150")
        author("Thepigcat76")
        author("SuperMartijn642")
        author("Heather White")
        author("IMS212")
    }

    curseMaven()
    maven("BlameJared", "https://maven.blamejared.com/")
    maven("Bawnorton", "https://maven.bawnorton.com/releases")
    maven("Enjarai", "https://maven.enjarai.dev/mirrors")
    maven("CaffeineMC", "https://maven.caffeinemc.net/releases")

    publishing {

        curseforge {
            accessToken = providers.environmentVariable("CURSEFORGE_TOKEN")
            projectId = "1140577"
        }

        modrinth {
            accessToken = providers.environmentVariable("MODRINTH_TOKEN")
            projectId = "twipgzWx"
        }

        dependencies {
            optional("jei")
        }
    }

    version("1.18.2") {
        changelogFile = "changelogs/1.18.2.md"
        forge {
            loaderVersion = "40.3.11"

            dependencies {
                compileOnly("com.github.bawnorton.mixinsquared:mixinsquared-common:0.3.7-beta.1")
                annotationProcessor("com.github.bawnorton.mixinsquared:mixinsquared-common:0.3.7-beta.1")
                jarJar("com.github.bawnorton.mixinsquared:mixinsquared-forge:0.3.7-beta.1")

                compileOnly("io.github.llamalad7:mixinextras-common:0.5.4")
                annotationProcessor("io.github.llamalad7:mixinextras-common:0.5.4")
                jarJar("io.github.llamalad7:mixinextras-forge:0.5.4")

                modCompileOnly("curse.maven:jei-238222:5846864")
                modRuntimeOnly("curse.maven:jei-238222:5846864")

                modCompileOnly("curse.maven:oculus-581495:4578744")
                modCompileOnly("curse.maven:embeddium-908741:5322305")
                modRuntimeOnly("curse.maven:embeddium-908741:5322305")

                modCompileOnly("curse.maven:fusion-connected-textures-854949:7471530")
                modRuntimeOnly("curse.maven:fusion-connected-textures-854949:7471530")

                modRuntimeOnly("curse.maven:pipez-443900:3819249")

                modRuntimeOnly("curse.maven:chipped-456956:4293291")
                modRuntimeOnly("curse.maven:ctm-267602:3933537")

                modRuntimeOnly("curse.maven:rechiseled-558998:7687363")
                modRuntimeOnly("curse.maven:connected-glass-383129:6811777")
                modRuntimeOnly("curse.maven:supermartijn642s-core-lib-454372:7783313")
                modRuntimeOnly("curse.maven:supermartijn642s-config-lib-438332:4715404")
            }
        }
    }

    version("1.20.1") {
        changelogFile = "changelogs/1.20.1.md"
        forge {
            loaderVersion = "47.3.12"

            dependencies {
                compileOnly("com.github.bawnorton.mixinsquared:mixinsquared-common:0.3.7-beta.1")
                annotationProcessor("com.github.bawnorton.mixinsquared:mixinsquared-common:0.3.7-beta.1")
                jarJar("com.github.bawnorton.mixinsquared:mixinsquared-forge:0.3.7-beta.1")

                compileOnly("io.github.llamalad7:mixinextras-common:0.5.4")
                annotationProcessor("io.github.llamalad7:mixinextras-common:0.5.4")
                jarJar("io.github.llamalad7:mixinextras-forge:0.5.4")

                val jeiVersion = "15.20.0.105"
                modCompileOnly("mezz.jei:jei-1.20.1-common-api:$jeiVersion")
                modCompileOnly("mezz.jei:jei-1.20.1-forge-api:$jeiVersion")
                modRuntimeOnly("mezz.jei:jei-1.20.1-forge:$jeiVersion")

                modCompileOnly("curse.maven:oculus-581495:6020952")
                modCompileOnly("curse.maven:embeddium-908741:5681725")
                modRuntimeOnly("curse.maven:embeddium-908741:5681725")
                modRuntimeOnly("curse.maven:oculus-581495:6020952")

                modCompileOnly("curse.maven:fusion-connected-textures-854949:7471518")
                modRuntimeOnly("curse.maven:fusion-connected-textures-854949:7471518")
                modRuntimeOnly("curse.maven:athena-841890:5176879")

                modRuntimeOnly("curse.maven:pipez-443900:6945379")

                modRuntimeOnly("curse.maven:chipped-456956:5813138")
                modRuntimeOnly("curse.maven:resourceful-lib-570073:5659871")

                modRuntimeOnly("curse.maven:rechiseled-558998:7687383")
                modRuntimeOnly("curse.maven:connected-glass-383129:6811804")
                modRuntimeOnly("curse.maven:supermartijn642s-core-lib-454372:7783320")
                modRuntimeOnly("curse.maven:supermartijn642s-config-lib-438332:4715408")

                modRuntimeOnly("curse.maven:chisel-modern-1392308:7650699")
                modRuntimeOnly("curse.maven:baguettelib-1264423:7544807")
                modRuntimeOnly("curse.maven:ctm-267602:5983309")
            }
        }
    }

    version("1.21.1") {
        changelogFile = "changelogs/1.21.1.md"

        parchmentMinecraftVersion = "1.21.1"
        parchmentMappingsVersion = "2024.11.13"

        neoforge {
            loaderVersion = "21.1.169"

            dependencies {
                compileOnly("com.github.bawnorton.mixinsquared:mixinsquared-common:0.3.7-beta.1")
                annotationProcessor("com.github.bawnorton.mixinsquared:mixinsquared-common:0.3.7-beta.1")
                jarJar("com.github.bawnorton.mixinsquared:mixinsquared-neoforge:0.3.7-beta.1")

                val jeiVersion = "19.21.0.247"
                compileOnly("mezz.jei:jei-1.21.1-common-api:$jeiVersion")
                compileOnly("mezz.jei:jei-1.21.1-neoforge-api:$jeiVersion")
                runtimeOnly("mezz.jei:jei-1.21.1-neoforge:$jeiVersion")

                compileOnly("curse.maven:irisshaders-455508:6661598")
                compileOnly("curse.maven:sodium-394468:8038693")

                compileOnly("curse.maven:fusion-connected-textures-854949:7471474")
                runtimeOnly("curse.maven:athena-841890:8061947")

                implementation("curse.maven:pipez-443900:5757713")

                runtimeOnly("curse.maven:chipped-456956:5813117")
                runtimeOnly("curse.maven:resourceful-lib-570073:5973188")

                runtimeOnly("curse.maven:sophisticated-backpacks-422301:8145672")
                runtimeOnly("curse.maven:sophisticated-core-618298:8145747")
            }
        }
    }

    version("26.1.2") {
        changelogFile = "changelogs/26.1.2.md"
        neoforge {
            loaderVersion = "26.1.2.43-beta"
            loaderVersionRange = "[4,)"

            dependencies {
                val jeiVersion = "29.5.0.28"
                compileOnly("mezz.jei:jei-26.1.2-common-api:$jeiVersion")
                compileOnly("mezz.jei:jei-26.1.2-neoforge-api:$jeiVersion")
                runtimeOnly("mezz.jei:jei-26.1.2-neoforge:$jeiVersion")
                implementation("curse.maven:pipez-443900:8053422")
                compileOnly("com.github.bawnorton.mixinsquared:mixinsquared-common:0.3.7-beta.1")
                annotationProcessor("com.github.bawnorton.mixinsquared:mixinsquared-common:0.3.7-beta.1")
                jarJar("com.github.bawnorton.mixinsquared:mixinsquared-neoforge:0.3.7-beta.1")

                implementation("curse.maven:irisshaders-455508:7867946")
                implementation("curse.maven:sodium-394468:8038693")
                compileOnly("net.caffeinemc:sodium-neoforge-mod:0.8.10+mc26.1.2")

                runtimeOnly("curse.maven:athena-841890:7970442")
                runtimeOnly("curse.maven:resourceful-lib-570073:7927296")
            }
        }
    }
}
