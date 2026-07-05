# Kubernetes Manifests

Plain Kubernetes manifests for the Distributed Operations Control Plane demo.
These are written for local test clusters (kind, minikube) as well as any
standard managed Kubernetes cluster. For OpenShift, see `../openshift/`.

## Prerequisites

- A running cluster (`kind create cluster`, `minikube start`, or similar)
- `kubectl` configured against that cluster
- An ingress controller installed if you want to use `ingress.yaml`
  (e.g. `minikube addons enable ingress`, or install ingress-nginx on kind)
- Backend and frontend images built and available to the cluster
  (for kind: `kind load docker-image ops-control-plane/backend:latest`;
  for minikube: `eval $(minikube docker-env)` before `docker build`)

## Apply order

Manifests reference the namespace and depend on the ConfigMap/Secret being
present before the Deployments start, so apply in this order:

```bash
kubectl apply -f k8s/namespace.yaml
kubectl apply -f k8s/configmap.yaml

# Copy the example and fill in real values first - never commit real secrets.
cp k8s/secret.yaml.example k8s/secret.yaml
# edit k8s/secret.yaml with real values, then:
kubectl apply -f k8s/secret.yaml

kubectl apply -f k8s/postgres-deployment.yaml
kubectl apply -f k8s/postgres-service.yaml
kubectl apply -f k8s/kafka-deployment.yaml
kubectl apply -f k8s/kafka-service.yaml

# wait for postgres/kafka pods to be Ready
kubectl -n ops-control-plane rollout status deployment/postgres
kubectl -n ops-control-plane rollout status deployment/kafka

kubectl apply -f k8s/backend-deployment.yaml
kubectl apply -f k8s/backend-service.yaml
kubectl apply -f k8s/frontend-deployment.yaml
kubectl apply -f k8s/frontend-service.yaml

kubectl apply -f k8s/ingress.yaml
```

Or, more simply, once secrets are in place:

```bash
kubectl apply -f k8s/
```

(Kubernetes will retry Pods whose dependencies aren't ready yet, so applying
the whole directory at once generally works too - the explicit order above
is mainly useful when debugging.)

## Local testing with port-forward

If you don't have an ingress controller set up, use port-forwarding instead:

```bash
kubectl -n ops-control-plane port-forward svc/frontend 3000:80
kubectl -n ops-control-plane port-forward svc/backend 8080:8080
```

Then visit `http://localhost:3000` for the UI and `http://localhost:8080/actuator/health`
to confirm the backend is up.

## Notes

- `secret.yaml.example` is a template only - see the comments inside it.
  Use `kubectl create secret generic` or a real secret manager for anything
  beyond local experimentation.
- Kafka and Postgres here are single-replica, `emptyDir`/`PVC`-backed
  deployments meant for demo purposes, not production HA topologies.
- The backend Deployment expects the images referenced in
  `backend-deployment.yaml` / `frontend-deployment.yaml`
  (`ops-control-plane/backend:latest`, `ops-control-plane/frontend:latest`)
  to already exist in a registry reachable by the cluster, or to be loaded
  directly into kind/minikube's local image store.
