How to use
---
Prerequisite
+ Create an Azure account
+ Cloud shell is ready

Before run
---

used command as below
---
```
az aks create --resource-group=matt_learn --name=m01akscluster --attach-acr m02registry --dns-name-prefix=m01aksclusterkubernetes --generate-ssh-keys
az aks show -g matt_learn -n m01akscluster
az aks get-credentials --resource-group=matt_learn --name=m01akscluster
az aks show --name m01akscluster --resource-group matt_learn --query "oidcIssuerProfile.issuerUrl" --output tsv

kubectl run workload-identity-docker --image=m02registry.azurecr.io/workload-identity:v0

kubectl expose pod workload-identity-docker --type=LoadBalancer --port=80 --target-port=8080


networkWatchers
az group deployment list --resource-group NetworkWatcherRG --query "[?properties.targetResourceGroup=='NetworkWatcherRG'].{Name:name, Timestamp:properties.timestamp}"

az acr login --name m02registry --resource-group matt_learn
az acr repository delete --name m02registry --image workload-identity

docker build -t m02registry.azurecr.io/workload-identity-rest .
docker push m02registry.azurecr.io/workload-identity-rest


kubectl run workload-identity-credential --image=m02registry.azurecr.io/workload-identity-credential
kubectl expose pod workload-identity-credential --type=LoadBalancer --port=80 --target-port=8080

kubectl run workload-identity-spn --image=m02registry.azurecr.io/workload-identity-spn --env="dbspn=726e2f44-b628-44c8-b726-720c29886427"
kubectl patch pod workload-identity-spn -p '{"metadata":{"labels":{"azure.workload.identity/use":"true"}}}'



https://eastasia.oic.prod-aks.azure.com/186a3027-ecbc-40e9-8bd6-2ccdcbc15e61/af348259-75ec-4d77-9724-e0248827182c/

cat <<EOF | kubectl apply -f -
apiVersion: v1
kind: ServiceAccount
metadata:
  annotations:
    azure.workload.identity/client-id: 726e2f44-b628-44c8-b726-720c29886427
  name: workload-identity-sa
  namespace: default
EOF


cat <<EOF | kubectl apply -f -
apiVersion: v1
kind: Pod
metadata:
  name: workload-identity-rest
  namespace: default
  labels:
    azure.workload.identity/use: "true"
spec:
  serviceAccountName: workload-identity-sa
  containers:
    - image: m02registry.azurecr.io/workload-identity-rest
      name: workload-identity-rest
EOF

kubectl expose pod workload-identity-spn --type=LoadBalancer --port=80 --target-port=8080

"metadata":{"annotations":{},"labels":{"azure.workload.identity/use":"true"},"name":"workload-identity-spn","namespace":"default"}

kubectl exec -it workload-identity --namespace=default -- /bin/bash

/var/run/secrets/azure/tokens
```
other
---
