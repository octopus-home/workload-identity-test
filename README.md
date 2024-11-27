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
az login --tenant 46d94fba-20cf-4c54-bc37-19ad68bb0dd5
az account get-access-token --resource https://servicebus.azure.net

az aks create --resource-group=matt_learn --name=m01akscluster --attach-acr m01registry --dns-name-prefix=m01aksclusterkubernetes --generate-ssh-keys
az aks show -g matt_learn -n m01akscluster
az aks get-credentials --resource-group=matt_learn --name=m01akscluster
az aks update  --resource-group matt_learn --name m01akscluster --enable-oidc-issuer --enable-workload-identity
az aks show --name m01akscluster --resource-group matt_learn  --query oidcIssuerProfile.issuerUrl  --output tsv


az acr login --name m01registry --resource-group matt_learn
az acr repository delete --name m01registry --image demo

mvn clean -DskipTests package
docker build -t m01registry.azurecr.io/demo .
docker push m01registry.azurecr.io/demo

cat <<EOF | kubectl apply -f -
apiVersion: v1
kind: ServiceAccount
metadata:
  name: demo-sa
  namespace: default
EOF


cat <<EOF | kubectl apply -f -
apiVersion: v1
kind: Pod
metadata:
  name: demo
  namespace: default
  labels:
    azure.workload.identity/use: "true"
spec:
  serviceAccountName: demo-sa
  containers:
    - image: m01registry.azurecr.io/demo
      name: demo
EOF

kubectl expose pod demo --type=LoadBalancer --port=80 --target-port=8080

kubectl exec -it demo --namespace=default -- /bin/bash

"metadata":{"annotations":{},"labels":{"azure.workload.identity/use":"true"},"name":"demo-spn","namespace":"default"}


```
other
---
### haven't use this time
``` azure

kubectl run demo-redis--image=m01registry.azurecr.io/demo:v0
kubectl run demo-docker --image=m01registry.azurecr.io/demo:v0
kubectl expose pod demo-docker --type=LoadBalancer --port=80 --target-port=8080

kubectl run demo-credential --image=m01registry.azurecr.io/demo-credential
kubectl expose pod demo-credential --type=LoadBalancer --port=80 --target-port=8080

kubectl run demo-spn --image=m01registry.azurecr.io/demo-spn --env="dbspn=726e2f44-b628-44c8-b726-720c29886427"
kubectl patch pod demo-spn -p '{"metadata":{"labels":{"azure.workload.identity/use":"true"}}}'

networkWatchers
az group deployment list --resource-group NetworkWatcherRG --query "[?properties.targetResourceGroup=='NetworkWatcherRG'].{Name:name, Timestamp:properties.timestamp}"

cat <<EOF | kubectl apply -f -
apiVersion: v1
kind: Pod
metadata:
  name: demo
  namespace: default
  labels:
    azure.workload.identity/use: "true"
spec:
  serviceAccountName: demo-sa
  containers:
    - image: m01registry.azurecr.io/demo
      name: demo
      env:
      - name: AZURE_CLIENT_ID
        value: a61679a0-523b-407c-ab40-fc98c6664ba3
EOF
```

CREATE USER [m01app] FROM EXTERNAL PROVIDER;
ALTER ROLE db_owner ADD MEMBER [m01app];

CREATE USER [group1] FROM EXTERNAL PROVIDER;
ALTER ROLE db_owner ADD MEMBER [group1];