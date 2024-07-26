### 1. create storage account
### 2. create managed identity/aad app(spn)
### 3. role assignment in the storage account to above spn/identity (have read/wirte access)
### 4. created federated credential in the identity/spn, oidc url and serviceaccount name must match with the kubernetes pod
### 5. code like this project