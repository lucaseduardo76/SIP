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
                    sh 'docker-compose down || true'  // derruba containers antigos, se existirem
                    sh 'docker-compose up -d'         // sobe os containers em segundo plano
                }
            }
        }

        stage('Verificar containers') {
            steps {
                sh 'docker ps'  // lista os containers em execução
            }
        }
    }
}
