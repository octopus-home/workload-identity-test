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
az aks create --resource-group=matt_learn --name=m01akscluster --attach-acr m01registry --dns-name-prefix=m01aksclusterkubernetes --generate-ssh-keys
az aks show -g matt_learn -n m01akscluster
az aks get-credentials --resource-group=matt_learn --name=m01akscluster


kubectl run workload-identity-docker --image=m01registry.azurecr.io/workload-identity:v0

kubectl expose pod workload-identity-docker --type=LoadBalancer --port=80 --target-port=8080


networkWatchers
az group deployment list --resource-group NetworkWatcherRG --query "[?properties.targetResourceGroup=='NetworkWatcherRG'].{Name:name, Timestamp:properties.timestamp}"

az acr login --name m01registry --resource-group matt_learn
az acr repository delete --name m01registry --image workload-identity

docker build -t m01registry.azurecr.io/workload-identity .
docker push m01registry.azurecr.io/workload-identity


kubectl run workload-identity-credential --image=m01registry.azurecr.io/workload-identity-credential
kubectl expose pod workload-identity-credential --type=LoadBalancer --port=80 --target-port=8080

kubectl run workload-identity-spn --image=m01registry.azurecr.io/workload-identity-spn --env="dbspn=726e2f44-b628-44c8-b726-720c29886427"
kubectl patch pod workload-identity-spn -p '{"metadata":{"labels":{"azure.workload.identity/use":"true"}}}'



https://eastasia.oic.prod-aks.azure.com/0044550f-19ec-4c35-9c61-994af34191fe/6fb7205b-48c0-43be-a9e0-228275e67bbb/

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
  name: workload-identity
  namespace: default
  labels:
    azure.workload.identity/use: "true"
spec:
  serviceAccountName: workload-identity-sa
  containers:
    - image: m01registry.azurecr.io/workload-identity
      name: workload-identity
      env:
      - name: dbspn
        value: 726e2f44-b628-44c8-b726-720c29886427
EOF

kubectl expose pod workload-identity-spn --type=LoadBalancer --port=80 --target-port=8080

"metadata":{"annotations":{},"labels":{"azure.workload.identity/use":"true"},"name":"workload-identity-spn","namespace":"default"}

kubectl exec -it workload-identity --namespace=default -- /bin/bash
```
other
---
