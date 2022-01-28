pipeline {
    agent none
    options {
        disableConcurrentBuilds()
        timeout(time: 7, unit: 'DAYS')
    }
    stages {
        stage('Import into staging schema') {
            agent { label 'gretl' }
            steps {
                script { currentBuild.description = "${params.buildDescription}" }
                git url: "${env.GIT_REPO_URL}", branch: "${params.BRANCH ?: 'main'}", changelog: false
                dir(env.JOB_BASE_NAME) {
                    sh 'gretl importDataToStage refreshOerebWMSTablesStage'
                    zip zipFile: 'xtfdata.zip', glob: '*.xtf', archive: true
                }
                stash name: "gretljob"
                emailext (
                    recipientProviders: [requestor()],
                    subject: "ÖREB-Daten zum Review bereit (GRETL-Job ${JOB_NAME} ${BUILD_DISPLAY_NAME})",
                    body: "Mit dem GRETL-Job ${JOB_NAME} (${BUILD_DISPLAY_NAME}) wurden ÖREB-Daten bereitgestellt, die ein Review erfordern. Nach dem Review unter https://${ORG_GRADLE_PROJECT_geoservicesHostName}/map?t=oereb_review können Sie unter folgendem Link die Publikation der Daten veranlassen oder abbrechen: ${RUN_DISPLAY_URL}."
                )
            }
        }
        stage('Validation') {
            agent { label 'master' }
            steps {
                input message: "Fortfahren und die Daten publizieren?", ok: "OK"
            }
        }
        stage('Import into live schema') {
            agent { label 'gretl' }
            steps {
                unstash name: "gretljob"
                dir(env.JOB_BASE_NAME) {
                    sh 'gretl importDataToLive refreshOerebWMSTablesLive uploadXtfToS3Geodata'
                }
            }
        }
    }
    post {
        success {
            emailext (
                recipientProviders: [requestor()],
                subject: "ÖREB-Daten sind publiziert (GRETL-Job ${JOB_NAME} ${BUILD_DISPLAY_NAME})",
                body: "Die ÖREB-Daten des GRETL-Jobs ${JOB_NAME} (${BUILD_DISPLAY_NAME}) wurden erfolgreich publiziert. Die Log-Meldungen dazu finden Sie unter ${RUN_DISPLAY_URL}."
            )
        }
        unsuccessful {
            emailext (
                to: '${DEFAULT_RECIPIENTS}',
                recipientProviders: [requestor()],
                subject: "GRETL-Job ${JOB_NAME} (${BUILD_DISPLAY_NAME}) ist fehlgeschlagen",
                body: "Die Ausführung des GRETL-Jobs ${JOB_NAME} (${BUILD_DISPLAY_NAME}) war nicht erfolgreich. Details dazu finden Sie in den Log-Meldungen unter ${RUN_DISPLAY_URL}."
            )
        }
    }
}
