def p = [:]

def GRADLE_ENTERPRISE_SECRET_ACCESS_KEY =
	string(credentialsId: 'gradle_enterprise_secret_access_key', variable: 'GRADLE_ENTERPRISE_ACCESS_KEY')

node {
	checkout scm
	p = readProperties interpolate: true, file: 'ci/pipeline.properties'
}

pipeline {

	agent any

	options {
		buildDiscarder(logRotator(numToKeepStr: '10'))
		disableConcurrentBuilds()
	}

	triggers {
		cron('@daily')
	}

	stages {

		stage('Build') {
			options {
				timeout(time: 15, unit: "MINUTES")
			}
			steps {
				script {
					docker.image(p['docker.container.image.java.main']).inside(p['docker.container.inside.env.full']) {
						withCredentials([GRADLE_ENTERPRISE_SECRET_ACCESS_KEY]) {
							withEnv(["GRADLE_ENTERPRISE_ACCESS_KEY=${GRADLE_ENTERPRISE_ACCESS_KEY}"]) {

								sh "echo 'Setup build environment...'"
								sh "ci/setup.sh"

								// Cleanup any prior build system resources
								try {
									sh "echo 'Clean up GemFire/Geode files & build artifacts...'"
									sh "ci/cleanupGemFiles.sh"
									sh "ci/cleanupArtifacts.sh"
								}
								catch (ignore) { }

								// Run the SBDG project Gradle build using JDK 8 inside Docker
								try {
									sh "echo 'Building SSDG...'"
									sh "ci/check.sh"
								}
								catch (e) {
									currentBuild.result = "FAILED: build"
									throw e
								}
								finally {
									junit '**/build/test-results/*/*.xml'
								}
							}
						}
					}
				}
			}
		}

		stage ('Deploy Docs') {
			options {
				timeout(time: 15, unit: "MINUTES")
			}
			steps {
				script {
					docker.image(p['docker.container.image.java.main']).inside(p['docker.container.inside.env.basic']) {
						withCredentials([file(credentialsId: 'docs.spring.io-jenkins_private_ssh_key', variable: 'DEPLOY_SSH_KEY')]) {
							try {
								sh "ci/deployDocs.sh"
							}
							catch (e) {
								currentBuild.result = "FAILED: deploy docs"
								throw e
							}
						}
					}
				}
			}
		}

		stage ('Deploy Artifacts') {
			options {
				timeout(time: 15, unit: "MINUTES")
			}
			steps {
				script {
					docker.image(p['docker.container.image.java.main']).inside(p['docker.container.inside.env.basic']) {
						withCredentials([file(credentialsId: 'spring-signing-secring.gpg', variable: 'SIGNING_KEYRING_FILE')]) {
							withCredentials([string(credentialsId: 'spring-gpg-passphrase', variable: 'SIGNING_PASSWORD')]) {
								withCredentials([usernamePassword(credentialsId: 'oss-token', passwordVariable: 'OSSRH_PASSWORD', usernameVariable: 'OSSRH_USERNAME')]) {
									withCredentials([usernamePassword(credentialsId: '02bd1690-b54f-4c9f-819d-a77cb7a9822c', usernameVariable: 'ARTIFACTORY_USERNAME', passwordVariable: 'ARTIFACTORY_PASSWORD')]) {
										try {
											sh "ci/deployArtifacts.sh"
										}
										catch (e) {
											currentBuild.result = "FAILED: deploy artifacts"
											throw e
										}
									}
								}
							}
						}
					}
				}
			}
		}
	}

	post {
		changed {
			script {

				def BUILD_SUCCESS = hudson.model.Result.SUCCESS.toString()
				def buildStatus = currentBuild.result
				def buildNotSuccess = !BUILD_SUCCESS.equals(buildStatus)
				def previousBuildStatus = currentBuild.previousBuild?.result
				def previousBuildNotSuccess = !BUILD_SUCCESS.equals(previousBuildStatus)

				if (buildNotSuccess || previousBuildNotSuccess) {

					def RECIPIENTS = [[$class: 'DevelopersRecipientProvider'], [$class: 'RequesterRecipientProvider']]
					def subject = "${buildStatus}: Build ${env.JOB_NAME} ${env.BUILD_NUMBER} status is now ${buildStatus}"
					def details = "The build status changed to ${buildStatus}. For details see ${env.BUILD_URL}"

					emailext(subject: subject, body: details, recipientProviders: RECIPIENTS, to: "$GEODE_TEAM_EMAILS")

					slackSend(color: (currentBuild.currentResult == 'SUCCESS') ? 'good' : 'danger',
						channel: '#spring-data-dev',
						message: "${currentBuild.fullDisplayName} - `${currentBuild.currentResult}`\n${env.BUILD_URL}")
				}
			}
		}
	}
}
