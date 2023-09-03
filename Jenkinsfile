pipeline {
	agent any

  libraries {
		lib('android-pipeline-library')
	}

  parameters {
		string defaultValue: '', name: 'BUILD_ID'
		string defaultValue: '', name: 'USER_TOKEN'
		string defaultValue: '', name: 'ACCESS_TOKEN'
		string defaultValue: '', name: 'APP_ID'
		string defaultValue: '', name: 'APP_NAME'
		string defaultValue: '', name: 'VERSION_NAME'
		string defaultValue: '', name: 'VERSION_CODE'
		string defaultValue: '', name: 'ONESIGNAL_APP_ID'
		// keystore credentials
		string defaultValue: '', name: 'KEYSTORE_FILE'
		string defaultValue: '', name: 'KEYSTORE_PASSWORD'
		string defaultValue: '', name: 'KEY_ALIAS'
		string defaultValue: '', name: 'KEY_PASSWORD'
	}

	environment {
    USER_TOKEN              = "${params.USER_TOKEN}"
    ACCESS_TOKEN            = "${params.ACCESS_TOKEN}"
    APP_ID            			= "${params.APP_ID}"
    APP_NAME                = "${params.APP_NAME}"
    VERSION_NAME            = "${params.VERSION_NAME}"
    VERSION_CODE            = "${params.VERSION_CODE}"
    ONESIGNAL_APP_ID        = "${params.ONESIGNAL_APP_ID}"
		KEYSTORE_FILE						= "${params.KEYSTORE_FILE}"
    KEYSTORE_PASSWORD       = "${params.KEYSTORE_PASSWORD}"
    KEY_ALIAS               = "${params.KEY_ALIAS}"
    KEY_PASSWORD            = "${params.KEY_PASSWORD}"

    REPOSITORY_PATH         = '/var/www/cloud.mynta.app/repository'
	}

	stages {
		stage('Checkout Source Code') {
			steps {
				script {
					checkout scm
				}
			}
		}

    stage('Configure Application') {
			parallel {
				stage('Set Application Properties') {
					steps {
						script {
							def propertyMap = [
								'PARAMETER_APP_ID': 'APP_ID',
								'PARAMETER_APP_NAME': 'APP_NAME',
								'PARAMETER_APP_VERSION_NAME': 'VERSION_NAME',
								'PARAMETER_APP_VERSION_CODE': 'VERSION_CODE',
								'PARAMETER_APP_ACCESS_TOKEN': 'ACCESS_TOKEN',
								'PARAMETER_APP_ONESIGNAL_APP_ID': 'ONESIGNAL_APP_ID'
							]
							def templateSourcePath = "${WORKSPACE}/local.properties.template"
            	def outputSourceDestination = "${WORKSPACE}/local.properties"

							setTemplateProperties(propertyMap, templateSourcePath, outputSourceDestination)
						}
					}
				}

				stage('Copy Google Services Config') {
					steps {
						script {
							try {
								def googleServicesSourcePath = "${REPOSITORY_PATH}/google_services/${APP_ID}.json"
                def googleServicesDestination = "${WORKSPACE}/app/google-services.json"
								sh "cp -f ${googleServicesSourcePath} ${googleServicesDestination}"
							} catch (Exception ex) {
								currentBuild.result = 'FAILURE'
								error "Error in Copy Google Services Config stage: ${ex.getMessage()}"
							}
						}
					}
				}

				stage('Generate Launcher Icons') {
					steps {
						script {
							def iconSourcePath = "${REPOSITORY_PATH}/icons/${APP_ID}.png"
							def resDirectory = "${WORKSPACE}/app/src/main/res"

							generateLauncherIcons(resDirectory, iconSourcePath)
						}
					}
				}
			}
		}

		stage('Manage Application Signing') {
			steps {
				script {
					try {
						def propertyMap = [
							'PARAMETER_SIGNING_KEYSTORE_FILE': 'KEYSTORE_FILE',
							'PARAMETER_SIGNING_KEYSTORE_PASSWORD': 'KEYSTORE_PASSWORD',
							'PARAMETER_SIGNING_KEY_ALIAS': 'KEY_ALIAS',
							'PARAMETER_SIGNING_KEY_PASSWORD': 'KEY_PASSWORD'
						]
						def templateSourcePath = "${WORKSPACE}/signing.properties.template"
						def outputDestination = "${WORKSPACE}/signing.properties"

						setTemplateProperties(propertyMap, templateSourcePath, outputDestination)

						def keystoreSourcePath = "${REPOSITORY_PATH}/keystores/keystore.${APP_ID}.zip"
						sh "unzip -o ${keystoreSourcePath} -d ${WORKSPACE}"
					} catch (Exception ex) {
						currentBuild.result = 'FAILURE'
						error "Error in Manage Signing Configuration stage: ${ex.getMessage()}"
					}
				}
			}
		}

		stage('Build Release APK and AAB') {
			steps {
				script {
					try {
						sh 'chmod +rx gradlew'
						sh "./gradlew clean assembleRelease bundleRelease"

						def outputsPath = "${WORKSPACE}/app/build/outputs"
						def apkSourcePath = "${outputsPath}/apk/release/app-release.apk"
            def aabSourcePath = "${outputsPath}/bundle/release/app-release.aab"

						def apkDestinationPath = "${REPOSITORY_PATH}/outputs/apk/${APP_ID}.apk"
            def aabDestinationPath = "${REPOSITORY_PATH}/outputs/aab/${APP_ID}.aab"

						sh "mv ${apkSourcePath} ${apkDestinationPath}"
						env.APK_FILE_SIZE = sh(script: "du -sh ${apkDestinationPath} | cut -f1", returnStdout: true).trim()

						sh "mv ${aabSourcePath} ${aabDestinationPath}"
						env.AAB_FILE_SIZE = sh(script: "du -sh ${aabDestinationPath} | cut -f1", returnStdout: true).trim()
					} catch (Exception ex) {
						currentBuild.result = 'FAILURE'
						error "Error in Build Release APK and AAB stage: ${ex.getMessage()}"
					}
				}
			}
		}
	}
}