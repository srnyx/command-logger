import xyz.srnyx.gradlegalaxy.data.config.DependencyConfig
import xyz.srnyx.gradlegalaxy.data.config.JavaSetupConfig
import xyz.srnyx.gradlegalaxy.enums.Repository
import xyz.srnyx.gradlegalaxy.enums.repository
import xyz.srnyx.gradlegalaxy.utility.paper
import xyz.srnyx.gradlegalaxy.utility.setupAnnoyingAPI


plugins {
    java
    id("xyz.srnyx.gradle-galaxy") version "2.0.2"
    id("com.gradleup.shadow") version "8.3.9"
}

paper(config = DependencyConfig(version = "1.8.8"))
setupAnnoyingAPI(
    javaSetupConfig = JavaSetupConfig(
        group = "com.srnyx",
        version = "1.1.0",
        description = "Log commands executed by players and console to one file or multiple files"),
    annoyingAPIConfig = DependencyConfig(version = "e9ad7a91ef"))

repository(Repository.PLACEHOLDER_API)
dependencies.compileOnly("me.clip", "placeholderapi", "2.11.6")
