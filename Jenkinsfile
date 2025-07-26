pipeline {
    agent any

    stages {
        stage('Checkout') {
            steps {
                git url: 'https://github.com/lucaseduardo76/SIP.git', branch: 'main'
            }
        }

        stage('Subir containers com Docker Compose') {
            steps {
                script {
                    sh 'docker-compose down || true'
                    sh 'docker-compose up -d'
                }
            }
        }

        stage('Verificar containers') {
            steps {
                sh 'docker ps'
            }
        }
    }
}
