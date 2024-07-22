How to use
---
Prerequisite
+ Create an Azure account
+ Cloud shell is ready

Before run
---
After git clone this repo, <span style='color: red;'><b>change the MySQL password in <u>docker-compose.yml</u> file</b></span>, and corresponding variables in <b>variables.tf</b> to avoid name crash.

script
---
```
az aks create --resource-group=matt_learn --name=m01akscluster --attach-acr m01registry --dns-name-prefix=m01aksclusterkubernetes --generate-ssh-keys

az aks get-credentials --resource-group=matt_learn --name=m01akscluster


kubectl run workload-identity-docker --image=m01registry.azurecr.io/workload-identity:v0

kubectl expose pod workload-identity-docker --type=LoadBalancer --port=80 --target-port=8080


networkWatchers
az group deployment list --resource-group NetworkWatcherRG --query "[?properties.targetResourceGroup=='NetworkWatcherRG'].{Name:name, Timestamp:properties.timestamp}"


az acr repository delete --name m01registry --image workload-identity

docker build -t m01registry.azurecr.io/workload-identity-credential .
docker push m01registry.azurecr.io/workload-identity-credential


kubectl run workload-identity-credential --image=m01registry.azurecr.io/workload-identity-credential
kubectl expose pod workload-identity-credential --type=LoadBalancer --port=80 --target-port=8080

kubectl run workload-identity-spn --image=m01registry.azurecr.io/workload-identity-spn --env="dbspn=xxxxxxxx"
kubectl patch pod workload-identity-spn -p '{"metadata":{"labels":{"azure.workload.identity/use":"true"}}}'



https://eastasia.oic.prod-aks.azure.com/0044550f-19ec-4c35-9c61-994af34191fe/xxxxxxxx/

cat <<EOF | kubectl apply -f -
apiVersion: v1
kind: ServiceAccount
metadata:
  annotations:
    azure.workload.identity/client-id: xxxxxxxx
  name: workload-identity-sa
  namespace: default
EOF


cat <<EOF | kubectl apply -f -
apiVersion: v1
kind: Pod
metadata:
  name: workload-identity-spn
  namespace: default
  labels:
    azure.workload.identity/use: "true"
spec:
  serviceAccountName: workload-identity-sa
  containers:
    - image: m01registry.azurecr.io/workload-identity-spn
      name: workload-identity-spn
      env:
      - name: dbspn
        value: xxxxxxxx
EOF

kubectl expose pod workload-identity-spn --type=LoadBalancer --port=80 --target-port=8080

"metadata":{"annotations":{},"labels":{"azure.workload.identity/use":"true"},"name":"workload-identity-spn","namespace":"default"}

```
other
---
