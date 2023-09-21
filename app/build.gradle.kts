plugins {
    Dependencies.plugins.forEach { id(it) }
}

val config = Properties().load(rootProject.file("local.properties"))
val signingConfig = Properties().load(rootProject.file("signing.properties"))

android {
    namespace = "app.mynta.template.android"
    compileSdk = 34

    defaultConfig {
        applicationId = config.getProperty(AppConfig.ID)
        versionCode = config.getProperty(AppConfig.VERSION_NUMBER).toInt()
        versionName = config.getProperty(AppConfig.VERSION_NAME)
        minSdk = 24
        targetSdk = 34

        val resourceValues = listOf(
            ResourceValue("string", "app_name", config.getProperty(AppConfig.NAME))
        )
        resourceValues.forEach { resourceValue ->
            resValue(resourceValue.type, resourceValue.name, resourceValue.value)
        }
        buildConfigField("String", "ACCESS_TOKEN", "\"${config.getProperty(ServerConfig.ACCESS_TOKEN)}\"")
        buildConfigField("String", "ONE_SIGNAL_APP_ID", "\"${config.getProperty(OneSignalConfig.APP_ID)}\"")
        buildConfigField("String", "ADMOB_INTERSTITIAL_AD_UNIT_ID", "\"${config.getProperty(AdmobConfig.INTERSTITIAL_ID)}\"")

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    signingConfigs {
        create("release") {
            storeFile = rootProject.file(signingConfig.getProperty(SigningConfig.KEYSTORE_FILE))
            storePassword = signingConfig.getProperty(SigningConfig.KEYSTORE_PASSWORD)
            keyAlias = signingConfig.getProperty(SigningConfig.KEY_ALIAS)
            keyPassword = signingConfig.getProperty(SigningConfig.KEY_PASSWORD)
        }
    }

    flavorDimensions += "default"
    productFlavors {
        create("regular") {
            dimension = "default"
        }
        create("monetize") {
            dimension = "default"
            manifestPlaceholders += mapOf(
                "admob_app_id" to config.getProperty(AdmobConfig.APP_ID)
            )
        }
    }

    buildTypes {
        release {
            if (config.getProperty(BuildConfig.ENVIRONMENT) == "production") {
                isMinifyEnabled = true
                isShrinkResources = true
            }
            signingConfig = signingConfigs["release"]
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        buildConfig = true
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.3"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    implementDependencies("implementation", Dependencies.core)
    implementDependencies("monetizeImplementation", Dependencies.monetize)
    implementDependencies("testImplementation", Dependencies.test)
    implementDependencies("androidTestImplementation", Dependencies.androidTest)
    implementDependencies("debugImplementation", Dependencies.debug)
    // kotlin symbol processing (KSP)
    Dependencies.ksp.forEach(::ksp)
}