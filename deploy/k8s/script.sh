#!/usr/bin/env bash
set -e

kubectl apply -f configmap.yaml
kubectl apply -f secret.yaml
kubectl apply -f postgres-service.yaml
kubectl apply -f postgres-statefulset.yaml
kubectl apply -f server-service.yaml
kubectl apply -f server-deployment.yaml
kubectl apply -f client-service.yaml
kubectl apply -f client-deployment.yaml

kubectl rollout status statefulset/postgres --timeout=60s
kubectl rollout status deployment/server --timeout=60s
kubectl rollout status deployment/client --timeout=60s
