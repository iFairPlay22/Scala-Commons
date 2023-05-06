pipeline {
    // https://github.com/sbt/docker-sbt
    agent { docker {
        image 'ebouquet/images/scala:latest'
        args  '--net="ebouquet/jenkins-network"'
    } }
    environment {
        VEHICLES_API_HOST               = '0.0.0.0'
        VEHICLES_API_PORT               = '8080'
        BROKER_VEHICLES_TOPIC           = 'vehicles'
        BROKER_BOOTSTRAP_SERVERS        = 'broker:9092'
        BROKER_SCHEDULER_INITIAL_DELAY  = '5 seconds'
        BROKER_SCHEDULER_REFRESH_DELAY  = '1 minute'
        DATABASE_CONTACT_POINT          = 'cassandra:9042'
    }
    stages {
        stage('Compilation') {
            steps {
                echo 'Compiling...'
                sh 'sbt clean compile'
            }
        }
        stage('Format') {
            steps {
                echo 'Checking format...'
                sh 'sbt scalafmtCheckAll'
            }
        }
        stage('General tests') {
            steps {
                echo 'General testing...'
                sh 'sbt domain/test'
                sh 'sbt database/test'
            }
        }
        stage('API tests') {
            steps {
                echo 'API testing...'
                sh 'sbt api/test'
            }
        }
        stage('Broker tests') {
            steps {
                echo 'Broker testing...'
                sh 'sbt brokerProducer/test'
                sh 'sbt brokerConsumer/test'
            }
        }
        stage('Newman tests') {
            steps {
                echo 'Newman testing...'
                // TODO
            }
        }
    }
}