// Jenkinsfile - Distributed Operations Control Plane
//
// NOTE: All registry hostnames, image repo names, credential IDs, and the
// KUBECONFIG credential referenced below are PLACEHOLDERS for portfolio
// demonstration purposes only. There is no real registry or cluster wired
// up. Swap the env vars / credential IDs for real values to actually run
// this pipeline.
pipeline {
    agent any

    environment {
        // Placeholder registry - replace with your real registry, e.g.
        // "docker.io/yourorg" or "ghcr.io/yourorg".
        REGISTRY = "registry.example.com/ops-control-plane"
        BACKEND_IMAGE = "${REGISTRY}/backend"
        FRONTEND_IMAGE = "${REGISTRY}/frontend"
        IMAGE_TAG = "${env.BUILD_NUMBER}"
    }

    options {
        timestamps()
        buildDiscarder(logRotator(numToKeepStr: '20'))
    }

    stages {

        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Backend Build & Test') {
            steps {
                dir('backend') {
                    sh 'mvn -B clean verify'
                }
            }
            post {
                always {
                    junit testResults: 'backend/target/surefire-reports/*.xml', allowEmptyResults: true
                }
            }
        }

        stage('Backend Docker Build & Push') {
            steps {
                dir('backend') {
                    // Placeholder credential ID - configure a real
                    // "Username with password" credential in Jenkins
                    // for your actual registry before using this stage.
                    withCredentials([usernamePassword(
                        credentialsId: 'container-registry-credentials',
                        usernameVariable: 'REGISTRY_USER',
                        passwordVariable: 'REGISTRY_PASSWORD'
                    )]) {
                        sh '''
                            docker build -t ${BACKEND_IMAGE}:${IMAGE_TAG} -f Dockerfile .
                            echo "${REGISTRY_PASSWORD}" | docker login ${REGISTRY} -u "${REGISTRY_USER}" --password-stdin
                            docker push ${BACKEND_IMAGE}:${IMAGE_TAG}
                        '''
                    }
                }
            }
        }

        stage('Frontend Build & Test') {
            steps {
                dir('frontend') {
                    sh '''
                        npm ci
                        npm run build
                    '''
                }
            }
        }

        stage('Frontend Docker Build & Push') {
            steps {
                dir('frontend') {
                    withCredentials([usernamePassword(
                        credentialsId: 'container-registry-credentials',
                        usernameVariable: 'REGISTRY_USER',
                        passwordVariable: 'REGISTRY_PASSWORD'
                    )]) {
                        sh '''
                            docker build -t ${FRONTEND_IMAGE}:${IMAGE_TAG} -f Dockerfile .
                            echo "${REGISTRY_PASSWORD}" | docker login ${REGISTRY} -u "${REGISTRY_USER}" --password-stdin
                            docker push ${FRONTEND_IMAGE}:${IMAGE_TAG}
                        '''
                    }
                }
            }
        }

        stage('Deploy') {
            steps {
                // Placeholder KUBECONFIG credential - configure a real
                // "Secret file" credential containing your kubeconfig.
                // OpenShift users would swap `kubectl` for `oc` here
                // (`oc apply -f k8s/`) - the manifests are compatible
                // since OpenShift is a Kubernetes distribution.
                withCredentials([file(credentialsId: 'kubeconfig-credential', variable: 'KUBECONFIG')]) {
                    sh '''
                        kubectl apply -f k8s/namespace.yaml
                        kubectl apply -f k8s/
                        kubectl -n ops-control-plane set image deployment/backend backend=${BACKEND_IMAGE}:${IMAGE_TAG}
                        kubectl -n ops-control-plane set image deployment/frontend frontend=${FRONTEND_IMAGE}:${IMAGE_TAG}
                        kubectl -n ops-control-plane rollout status deployment/backend
                        kubectl -n ops-control-plane rollout status deployment/frontend
                    '''
                }
            }
        }
    }

    post {
        always {
            junit testResults: '**/target/surefire-reports/*.xml', allowEmptyResults: true
            cleanWs()
        }
    }
}
