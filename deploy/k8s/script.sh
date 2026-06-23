#!/usr/bin/env bash
set -e

k3s kubectl apply -f configmap.yaml
k3s kubectl apply -f secret.yaml
k3s kubectl apply -f postgres-service.yaml
k3s kubectl apply -f postgres-statefulset.yaml
k3s kubectl apply -f server-service.yaml
k3s kubectl apply -f server-deployment.yaml
k3s kubectl rollout status statefulset/postgres --timeout=120s
k3s kubectl rollout status deployment/server --timeout=120s
