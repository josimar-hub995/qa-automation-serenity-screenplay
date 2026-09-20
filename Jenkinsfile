pipeline {
    agent any

    tools {
        jdk 'jdk17'
        maven 'maven3'
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Web and API automation') {
            steps {
                withCredentials([string(credentialsId: 'reqres-api-key', variable: 'REQRES_API_KEY')]) {
                    sh 'mvn -q clean verify -Dcucumber.filter.tags="@automation" -Dheadless.mode=true'
                }
            }
        }
    }

    post {
        always {
            script {
                def dashboard = sh(
                    script: "find reports -type f -path '*/resumen/dashboard.html' 2>/dev/null | sort | tail -1",
                    returnStdout: true
                ).trim()

                archiveArtifacts artifacts: 'reports/**,target/site/serenity/**', allowEmptyArchive: true

                if (dashboard) {
                    // Publica toda la ejecución para conservar los enlaces relativos
                    // del dashboard hacia Word, evidencias y reportes de error.
                    def executionDir = dashboard.substring(0, dashboard.indexOf('/resumen/dashboard.html'))
                    publishHTML target: [
                        allowMissing: false,
                        alwaysLinkToLastBuild: true,
                        keepAll: true,
                        reportDir: executionDir,
                        reportFiles: 'resumen/dashboard.html',
                        reportName: 'Dashboard QA'
                    ]

                    echo '================================================================================================'
                    echo '                              REPORTE PUBLICADO EN JENKINS'
                    echo '================================================================================================'
                    echo "${env.BUILD_URL}artifact/${dashboard}"
                    echo '================================================================================================'
                } else {
                    echo '[REPORTE] No se encontró dashboard.html para publicar.'
                }
            }
            junit testResults: 'target/failsafe-reports/*.xml', allowEmptyResults: true
        }
    }
}
