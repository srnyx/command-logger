import xyz.srnyx.gradlegalaxy.utility.paper
import xyz.srnyx.gradlegalaxy.utility.setupAnnoyingAPI


plugins {
    java
    id("xyz.srnyx.gradle-galaxy") version "1.3.5"
    id("com.gradleup.shadow") version "8.3.8"
}

paper("1.8.8")
setupAnnoyingAPI("1c2e7eef30", "com.srnyx", "1.0.0", "Log commands executed by players and console to one file or multiple files")
