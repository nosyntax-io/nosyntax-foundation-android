pipeline {
  agent {
    label 'core'
  }

  tools {
    gradle 'Gradle 8.8'
  }

  libraries {
    lib('android-pipeline-library')
  }

  parameters {
    /**
     * BUILD_ID: Unique identifier assigned to each initiated build.
     * BUILD_ENVIRONMENT: Indicates the build environment type: 'Production' for optimized settings
     *    including minification and resource shrinking, or 'Development' for
     *    debugging and testing purposes.
     * BUILD_OUTPUT: Specifies the type of output generated by the build, either apk or aab.
     */
    string defaultValue: '', name: 'BUILD_ID'
    string defaultValue: '', name: 'BUILD_ENVIRONMENT'
    string defaultValue: '', name: 'BUILD_OUTPUT'

    /**
     * PROJECT_ID: Unique identifier assigned to the project.
     * PROJECT_ACCESS_TOKEN: Access token for authenticating project access.
     */
    string defaultValue: '', name: 'PROJECT_ID'
    string defaultValue: '', name: 'PROJECT_ACCESS_TOKEN'

    /**
		 * APP_ID: Unique identifier for the application package.
		 * APP_NAME: Human-readable name of the application.
		 * APP_VERSION: version number of the application
		 * APP_BUILD_NUMBER: Numeric identifier for the build version.
		 */
		string defaultValue: '', name: 'APP_ID'
		string defaultValue: '', name: 'APP_NAME'
		string defaultValue: '', name: 'APP_VERSION'
		string defaultValue: '', name: 'APP_BUILD_NUMBER'

    /**
     * ONESIGNAL_APP_ID: Application ID for integrating OneSignal push notification service.
     */
    string defaultValue: '', name: 'ONESIGNAL_APP_ID'

    /**
     * IS_MONETIZE: Flag indicating whether monetization features are enabled.
     * ADMOB_APP_ID: Application ID used for AdMob integration.
     * ADMOB_BANNER_ID: Banner ad unit ID for AdMob integration.
     * ADMOB_INTERSTITIAL_ID: Interstitial ad unit ID for AdMob integration.
     */
    string defaultValue: '', name: 'IS_MONETIZE'
    string defaultValue: '', name: 'ADMOB_APP_ID'
    string defaultValue: '', name: 'ADMOB_BANNER_ID'
    string defaultValue: '', name: 'ADMOB_INTERSTITIAL_ID'

    /**
     * IS_REMOTE_CONFIG: Flag indicating whether remote configuration is enabled.
     * LOCAL_APP_CONFIG: Path to the local application configuration file.
     */
    string defaultValue: '', name: 'IS_REMOTE_CONFIG'
    string defaultValue: '', name: 'LOCAL_APP_CONFIG'

    /**
     * KEYSTORE_FILE: File path of the keystore used for signing the application.
     * KEYSTORE_PASSWORD: Password for accessing the keystore.
     * KEY_ALIAS: Alias name for the key stored in the keystore.
     * KEY_PASSWORD: Password for the key stored in the keystore.
     */
    string defaultValue: '', name: 'KEYSTORE_FILE'
    string defaultValue: '', name: 'KEYSTORE_PASSWORD'
    string defaultValue: '', name: 'KEY_ALIAS'
    string defaultValue: '', name: 'KEY_PASSWORD'
  }

  environment {
    BUILD_ID = "${params.BUILD_ID}"
    BUILD_ENVIRONMENT = "${params.BUILD_ENVIRONMENT}"
    BUILD_OUTPUT = "${params.BUILD_OUTPUT}"
    PROJECT_ID = "${params.PROJECT_ID}"
    PROJECT_ACCESS_TOKEN = "${params.PROJECT_ACCESS_TOKEN}"
    APP_ID = "${params.APP_ID}"
		APP_NAME = "${params.APP_NAME}"
		APP_VERSION = "${params.APP_VERSION}"
		APP_BUILD_NUMBER = "${params.APP_BUILD_NUMBER}"
    ONESIGNAL_APP_ID = "${params.ONESIGNAL_APP_ID}"
    IS_MONETIZE = "${params.IS_MONETIZE}"
    ADMOB_APP_ID = "${params.ADMOB_APP_ID}"
    ADMOB_BANNER_ID = "${params.ADMOB_BANNER_ID}"
    ADMOB_INTERSTITIAL_ID = "${params.ADMOB_INTERSTITIAL_ID}"
    IS_REMOTE_CONFIG = "${params.IS_REMOTE_CONFIG}"
    LOCAL_APP_CONFIG = "${params.LOCAL_APP_CONFIG}"
    KEYSTORE_FILE = "${params.KEYSTORE_FILE}"
    KEYSTORE_PASSWORD = "${params.KEYSTORE_PASSWORD}"
    KEY_ALIAS = "${params.KEY_ALIAS}"
    KEY_PASSWORD = "${params.KEY_PASSWORD}"

    REPOSITORY_PATH = '/var/www/cloud.nosyntax.io/repository'
  }

  stages {
    stage('Configure Application') {
      parallel {
        stage('Set App Properties') {
          steps {
            script {
              withCredentials([string(credentialsId: 'API_AUTH_TOKEN_V1.0', variable: 'AUTH_TOKEN')]) {
                def propertyMap = [
                  'PARAM_BUILD_ENVIRONMENT': 'BUILD_ENVIRONMENT',
                  'PARAM_APP_ID': 'APP_ID',
									'PARAM_APP_NAME': 'APP_NAME',
									'PARAM_APP_VERSION': 'APP_VERSION',
									'PARAM_APP_BUILD_NUMBER': 'APP_BUILD_NUMBER',
                  'PARAM_APP_REMOTE_CONFIG': 'IS_REMOTE_CONFIG',
                  'PARAM_AUTH_TOKEN': 'AUTH_TOKEN',
                  'PARAM_ACCESS_TOKEN': 'PROJECT_ACCESS_TOKEN',
                  'PARAM_ONESIGNAL_APP_ID': 'ONESIGNAL_APP_ID',
                  'PARAM_ADMOB_APP_ID': 'ADMOB_APP_ID',
                  'PARAM_ADMOB_BANNER_ID': 'ADMOB_BANNER_ID',
                  'PARAM_ADMOB_INTERSTITIAL_ID': 'ADMOB_INTERSTITIAL_ID'
                ]
                def templateFile = "${WORKSPACE}/local.properties.template"
                def outputPropertiesFile = "${WORKSPACE}/local.properties"

                setTemplateProperties(propertyMap, templateFile, outputPropertiesFile)
              }
            }
          }
        }

        stage('Copy Google Services Config') {
          steps {
            script {
              def configPath = "${REPOSITORY_PATH}/assets/google_services/${PROJECT_ID}.json"
              def destinationConfigPath = "${WORKSPACE}/app/google-services.json"

              if (fileExists(configPath)) {
                sh "cp -f ${configPath} ${destinationConfigPath}"
              } else {
                def propertyMap = [
                  'PARAM_PACKAGE_NAME': 'APP_ID'
                ]
                def defaultConfigPath = "${REPOSITORY_PATH}/assets/google_services/default.json"
                setTemplateProperties(propertyMap, defaultConfigPath, destinationConfigPath)
              }
            }
          }
        }

        stage('Generate Launcher Icons') {
          steps {
            script {
              def defaultIconPath = "${REPOSITORY_PATH}/assets/launcher_icons/default.png"
              def projectIconPath = "${REPOSITORY_PATH}/assets/launcher_icons/${PROJECT_ID}.png"
              def resDirectory = "${WORKSPACE}/app/src/main/res"

              def icon = fileExists(projectIconPath) ? projectIconPath : defaultIconPath

              generateLauncherIcons(resDirectory, icon)
            }
          }
        }

        stage('Generate Icon Assets') {
          steps {
            script {
              def defaultIconPath = "${REPOSITORY_PATH}/assets/app_icons/default.png"
              def projectIconPath = "${REPOSITORY_PATH}/assets/app_icons/${PROJECT_ID}.png"
              def resDirectory = "${WORKSPACE}/app/src/main/res"

              def icon = fileExists(projectIconPath) ? projectIconPath : defaultIconPath

              generateIconAssets(resDirectory, icon)
            }
          }
        }
      }
    }

    stage('Manage Application Signing') {
      stages {
        stage('Obtain Signing Key') {
          steps {
            script {
              def keystoreFilePath = "${REPOSITORY_PATH}/keystores/${PROJECT_ID}.keystore"

              if (!fileExists(keystoreFilePath)) {
                build job: 'AppSigning', parameters: [
                  string(name: 'ACCESS_TOKEN', value: env.PROJECT_ACCESS_TOKEN),
                  string(name: 'PROJECT_ID', value: env.PROJECT_ID),
                  string(name: 'APP_NAME', value: env.APP_NAME)
                ]
              } else {
                sh "cp -f ${keystoreFilePath} ${WORKSPACE}/${KEYSTORE_FILE}"
              }
            }
          }
        }

        stage('Set Signing Properties') {
          steps {
            script {
              try {
                def propertyMap = [
                  'PARAM_KEYSTORE_FILE': 'KEYSTORE_FILE',
                  'PARAM_KEYSTORE_PASSWORD': 'KEYSTORE_PASSWORD',
                  'PARAM_KEY_ALIAS': 'KEY_ALIAS',
                  'PARAM_KEY_PASSWORD': 'KEY_PASSWORD'
                ]
                def templateFilePath = "${WORKSPACE}/signing.properties.template"
                def destinationFilePath = "${WORKSPACE}/signing.properties"

                setTemplateProperties(propertyMap, templateFilePath, destinationFilePath)
              } catch (Exception ex) {
                currentBuild.result = 'FAILURE'
                error "Error in Set Signing Properties stage: ${ex.getMessage()}"
              }
            }
          }
        }
      }
    }

    stage('Manage Application Sourcecode') {
      when {
        expression {
          BUILD_OUTPUT = ~/(source)/
        }
      }
      steps {
        script {
          def excludes = [
            '*.apk',
            '*.aab',
            '*.aar',
            '*.jks',
            '*.keystore',
            '*.log',
            '*.md',
            '*.template',
            '**/build/',
            '**/.gradle/',
            'pipeline/',
            'Jenkinsfile',
            'init.*'
          ]

          zip zipFile: "source.zip", exclude: excludes.join(','), overwrite: true
          archiveArtifacts artifacts: 'source.zip', fingerprint: true, onlyIfSuccessful: true
        }
      }
    }

    stage('Build Release Artifact') {
      when {
        expression {
          BUILD_OUTPUT = ~/(apk)/
        }
      }
      steps {
        script {
          try {
            sh 'chmod +rx gradlew'

            def buildFlavor = env.IS_MONETIZE == 'enabled' ? 'monetize' : 'regular'

            sh "./gradlew assemble${buildFlavor.capitalize()}Release"

            def apkPath = "${WORKSPACE}/app/build/outputs/apk/${buildFlavor}/release/app-${buildFlavor}-release.apk"
            def destinationApkPath = "${REPOSITORY_PATH}/outputs/apk/${PROJECT_ID}.apk"

            sh "mv ${apkPath} ${destinationApkPath}"
          } catch (Exception ex) {
            currentBuild.result = 'FAILURE'
            error "Error in Build Release Artifact stage: ${ex.getMessage()}"
          }
        }
      }
    }
  }

  post {
    success {
      script {
        updateBuildStatus(BUILD_OUTPUT = ~/(apk)/ ? 1 : 0)
      }
    }
    unsuccessful {
      script {
        updateBuildStatus(BUILD_OUTPUT = ~/(apk)/ ? -1 : 0)
      }
    }
  }
}

def updateBuildStatus(int buildStatus) {
  try {
    withCredentials([string(credentialsId: 'API_KEY', variable: 'API_KEY')]) {
      def url = "https://api.nosyntax.io/cloud/update_build_status.inc.php"

      def postData = [
        project_id: PROJECT_ID,
        access_token: PROJECT_ACCESS_TOKEN,
        build_id: BUILD_ID,
        build_status: buildStatus
      ]
      def encodedData = postData.collect {
        k,
        v ->
        "${URLEncoder.encode(k.toString(), 'UTF-8')}=${URLEncoder.encode(v.toString(), 'UTF-8')}"
      }.join('&')

      def response = httpRequest(
        url: url,
        httpMode: 'POST',
        contentType: 'APPLICATION_FORM',
        requestBody: encodedData,
        validResponseCodes: '200:500',
        customHeaders: [
          [name: 'API-KEY', value: API_KEY, maskValue: true]
        ]
      )

      if (response.status != 200) {
        currentBuild.result = 'FAILURE'
        error "API request failed with status code: ${response.status}"
      }
    }
  } catch (Exception e) {
    currentBuild.result = 'FAILURE'
    error "Failed to send API request: ${e.message}"
  }
}