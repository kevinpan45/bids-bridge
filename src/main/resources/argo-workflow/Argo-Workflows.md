Premise

- Host server is Linux, preferably Ubuntu 24.04
- Kubernetes cluster has been set up
- kubectl is available with correct kubeconfig

## Argo Workflow Setup

### Install Argo Workflow

```bash
# install Argo-Workflows
export ARGO_WORKFLOWS_VERSION="v3.6.2"
kubectl create namespace argo
kubectl apply -n argo -f "https://github.com/argoproj/argo-workflows/releases/download/${ARGO_WORKFLOWS_VERSION}/quick-start-minimal.yaml"
# forward port to local
nohup kubectl -n argo port-forward --address 0.0.0.0 service/argo-server 2746:2746 > port-forward.log 2>&1 &
```

### Install Argo CLI

- Install Argo CLI

```bash
curl -sLO "https://github.com/argoproj/argo-workflows/releases/download/v3.6.2/argo-linux-amd64.gz"
gunzip argo-linux-amd64.gz
chmod +x argo-linux-amd64
mv argo-linux-amd64 /usr/local/bin/argo
# test
argo version
```

- Set Argo Environment Parameters

write to `~/.bashrc` or `~/.bash_profile`

**_set token value if you have set up the token for Argo Server_**

```bash
export ARGO_SERVER=<argo server host>:2746
export ARGO_TOKEN=<blank if not token has been set>
export ARGO_SECURE=true
export ARGO_INSECURE_SKIP_VERIFY=true
```

```bash
# Create a workflow template
argo template create <template yaml file>
# Update a workflow template
argo template update <template yaml file>
# Submit a workflow
argo submit <workflow yaml file>
# Submit a workflow by template
argo submit --from WorkflowTemplate/<template name> -p <parameter name>=<parameter value> -n <namespace, default argo>
```

## Initiate Neccessary Workflows and Templates

```bash
# create rclone configmap for data loader
kubectl apply -f ./argo-template/configmap/rclone-config.yaml
# create template for bids-apps and bids-collector
argo template create ./argo-template/bids-apps/bids-apps-mriqc.yaml
argo template create ./argo-template/bids-collector/openneuro-collector.yaml
# test template
argo submit --from WorkflowTemplate/openneuro-collector -p dataset=ds004776 -n argo
# excute workflow after dataset is collected
argo submit --from WorkflowTemplate/bids-apps -p dataset=ds004776 -n argo
```
