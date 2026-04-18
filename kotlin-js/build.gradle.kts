plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.jetbrainsCompose)
}

kotlin {
    js(IR) {
        browser()
        useEsModules()
        binaries.executable()
    }

    sourceSets {
        jsMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.html.core)
            implementation(libs.kotlinx.browser)
        }
    }
}