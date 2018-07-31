#!groovy
@Library(['Divorce', 'Reform'])
import uk.gov.hmcts.Packager
@Library(['Divorce', 'Reform'])
import uk.gov.hmcts.Packager
@Library(['Divorce', 'Reform'])
import uk.gov.hmcts.Packager
@Library(['Divorce', 'Reform'])
import uk.gov.hmcts.Packager
@Library(['Divorce', 'Reform'])
import uk.gov.hmcts.Packager
@Library(['Divorce', 'Reform'])
import uk.gov.hmcts.Packager
@Library(['Divorce', 'Reform'])
import uk.gov.hmcts.Packager
@Library(['Divorce', 'Reform'])
import uk.gov.hmcts.Packager
import uk.gov.hmcts.Versioner

properties(
        [[$class: 'GithubProjectProperty', projectUrlStr: 'https://git.reform.hmcts.net/divorce/validaion-service'],
         pipelineTriggers([[$class: 'GitHubPushTrigger']])]
)

Packager packager = new Packager(this, 'divorce')
Versioner versioner = new Versioner(this)

def channel = '#div-dev'


timestamps {
    lock(resource: "div-validation-service-${env.BRANCH_NAME}", inversePrecedence: true) {
        node {
            try {
                def version

                def onDevelopOrMaster = env.BRANCH_NAME == "master" || env.BRANCH_NAME == "develop"

                String validationServiceRPMVersion

                stage('Checkout') {
                    deleteDir()
                    checkout scm
                    env.CURRENT_SHA = gitSha()
                }


                stage('Build') {

                    onDevelop {
                        sh "./gradlew clean developAddRelaseSuffix build -x test"
                    }

                    onPR {
                        sh "./gradlew clean developAddRelaseSuffix build -x test"
                    }

                    onMaster {
                        sh "./gradlew clean build -x test"
                    }
                }

                stage('OWASP dependency check') {
                    try {
                        sh "./gradlew -DdependencyCheck.failBuild=true dependencyCheck"
                    } catch (ignored) {
                        archiveArtifacts 'build/reports/dependency-check-report.html'
                        notifyBuildResult channel: channel, color: 'warning',
                                message: 'OWASP dependency check failed see the report for the errors'
                    }
                }

                stage('Test (Unit)') {
                    try {
                        sh "./gradlew test"
                    } finally {
                        junit 'build/test-results/test/**/*.xml'
                    }
                }

                stage('Sonar') {
                    onPR {
                        sh "./gradlew -Dsonar.analysis.mode=preview -Dsonar.host.url=$SONARQUBE_URL sonarqube"
                    }

                    if (onDevelopOrMaster) {
                        sh "./gradlew -Dsonar.host.url=$SONARQUBE_URL sonarqube"
                    }
                }

                stage('Package (JAR)') {
                    versioner.addJavaVersionInfo()
                    sh "./gradlew installDist bootRepackage"
                }

                stage('Package (Docker)') {
                    validationServiceVersion = dockerImage imageName: 'divorce/div-validation-service'
                }

                stage('Package (RPM)') {

                    if (onDevelopOrMaster) {
                        validationServiceRPMVersion = packager.javaRPM('div-validation-service',
                                'build/libs/div-validation-service-$(./gradlew -q printVersion).jar',
                                'springboot',
                                'src/main/resources/application.yml')
                        println("ValidationServicePRMVersion " + validationServiceRPMVersion)

                        version = "{validation_service_buildnumber: ${validationServiceRPMVersion} }"

                        packager.publishJavaRPM('div-validation-service')
                    }

                }

                onDevelop {
                    deploy(validationServiceRPMVersion, 'dev')
                }

                onMaster() {
                    deploy(validationServiceRPMVersion, 'test')
                }

            } catch (err) {
                notifyBuildFailure channel: channel
                throw err
            } finally {
                step([$class           : 'InfluxDbPublisher',
                      customProjectName: 'Validation Service',
                      target           : 'Jenkins Data'])
            }
        }
    }

    notifyBuildFixed channel: channel
}

private void deploy(version, onEnv) {
    lock(resource: "Divorce-deploy-" + onEnv, inversePrecedence: true) {
        stage('Deploy ') {
            deploy app: 'div-validation-service', version: version, sha: env.CURRENT_SHA, env: onEnv
        }
    }
}
