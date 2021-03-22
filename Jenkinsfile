node {
    docker.image('maven:3-amazoncorretto-11').inside {
        stage('Build') {
            sh 'mvn -B -DskipTests clean package'
            archiveArtifacts artifacts: 'target/echo-*.jar', fingerprint: true
        }
        stage('Test') {
            sh 'mvn -B test'
        }
    }
}