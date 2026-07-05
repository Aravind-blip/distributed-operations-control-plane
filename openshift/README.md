# OpenShift Deployment Notes

This directory holds the OpenShift-specific objects that replace vanilla
Kubernetes `Ingress` with OpenShift `Route`s. Everything else
(`Deployment`, `Service`, `ConfigMap`, `Secret`) is reused as-is from `../k8s/`.

## Differences from vanilla Kubernetes

| Concern              | Vanilla Kubernetes (`k8s/`)      | OpenShift (`openshift/`)                                   |
|-----------------------|-----------------------------------|--------------------------------------------------------------|
| External access       | `Ingress` + ingress controller    | `Route` (built-in HAProxy router, no separate controller)     |
| TLS                   | Configured on Ingress/controller  | `tls.termination: edge` on the Route (see the two Route files) |
| CLI                   | `kubectl`                         | `oc` (superset of `kubectl`; `kubectl` also works against OpenShift) |
| App creation shortcut | n/a                               | `oc new-app`, `oc new-build`, `oc start-build`                |
| Security posture       | Depends on cluster PodSecurityPolicy/PSA config | Restricted SCC enforced by default (see below)         |

## Applying the manifests

```bash
oc new-project ops-control-plane   # or: oc apply -f ../k8s/namespace.yaml

oc apply -f ../k8s/configmap.yaml
cp ../k8s/secret.yaml.example ../k8s/secret.yaml   # fill in real values first
oc apply -f ../k8s/secret.yaml

oc apply -f ../k8s/postgres-deployment.yaml
oc apply -f ../k8s/postgres-service.yaml
oc apply -f ../k8s/kafka-deployment.yaml
oc apply -f ../k8s/kafka-service.yaml

oc apply -f ../k8s/backend-deployment.yaml
oc apply -f ../k8s/backend-service.yaml
oc apply -f ../k8s/frontend-deployment.yaml
oc apply -f ../k8s/frontend-service.yaml

oc apply -f backend-route.yaml
oc apply -f frontend-route.yaml

oc get routes -n ops-control-plane
```

Alternatively, for a fully OpenShift-native build/deploy flow you could use
`oc new-app` pointing at the backend/frontend Dockerfiles or a Git repo,
letting OpenShift's internal build system (S2I/Docker strategy) produce and
push images to the internal registry automatically. The manifests here
assume images are already built and pushed to a registry the cluster can
pull from, which keeps the demo consistent with the plain-Kubernetes path.

## SecurityContextConstraints (SCC) note - run as non-root

OpenShift enforces the `restricted` SCC by default, which (among other
things) **disallows running containers as root** and assigns each
namespace an arbitrary, non-predictable UID at runtime. Practical
implications for this project:

- The `backend/Dockerfile` and `frontend/Dockerfile` (owned by the sibling
  backend/frontend agents) need a non-root `USER` directive and must not
  hardcode a specific UID that the container depends on - the app must be
  able to run as *any* UID assigned by OpenShift (typically achieved by
  making relevant directories group-writable for GID 0, per Red Hat's
  guidelines for "Support Arbitrary User IDs").
- nginx (used to serve the frontend) needs its default config adjusted to
  listen on an unprivileged port (>1024) and avoid writing to paths owned
  by root - common community images already do this, but it's worth
  double-checking whichever base image the frontend Dockerfile uses.
- This is called out here as a **coordination point**: the k8s/OpenShift
  manifests in this repo do not set `runAsUser`, assuming the images
  themselves are already non-root-compatible. If the backend/frontend
  Dockerfiles run as root, Pods will be rejected (or silently fail) under
  the `restricted` SCC until that's fixed.

## If you don't have an OpenShift cluster

You don't need a full OpenShift installation to try this out:

- **Red Hat OpenShift Local (formerly CodeReady Containers / CRC)** - runs
  a single-node OpenShift cluster on your laptop. See
  https://developers.redhat.com/products/openshift-local/overview
- **Red Hat Developer Sandbox** - a free, hosted, time-limited OpenShift
  namespace for evaluation, no local install required. See
  https://developers.redhat.com/developer-sandbox

Either option is sufficient to `oc apply` the manifests in this directory
and confirm Route/SCC behavior without provisioning real infrastructure.
