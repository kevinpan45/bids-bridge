- Workflow Template

```bash
# Create a workflow template
argo template create <template yaml file>
# Update a workflow template
argo template update <template yaml file>
```

- Submit workflow

```bash
# Submit a workflow
argo submit <workflow yaml file>
```

Sample:

```bash
argo submit --from WorkflowTemplate/bids-apps -p dataset=ds004776 -n pipeline
```