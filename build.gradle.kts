plugins {
    id("dev.prism")
}

group = "com.portingdeadmods"
version = "2.0.2"

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
            optional("iris")
        }
    }

    version("26.1.2") {

        changelog = "26.1.2 port"
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

                compileOnly("curse.maven:irisshaders-455508:7867946")
                compileOnly("curse.maven:sodium-394468:8038693")
            }
        }
    }
}
