# bids-bridge
Bridge BIDS data and pipeline

- BIDS dataset collected from [OpenNeuro](https://www.openneuro.org) etc.
- BIDS App collected from [BIDS Apps](https://bids-apps.neuroimaging.io/) etc.
- Local-first data storage and computing
- Customize pipeline engine, [Argo Workflow](https://argoproj.github.io/workflows/)/[Docker](https://www.docker.com)/[GitLab CI Local](https://github.com/firecow/gitlab-ci-local) etc.

![Arch](./docs/bids-bridge.png)


## OpenAPI (Draft)

### Dataset

- List dataset: /datasets GET
- View specific dataset: /datasets/{id} GET
- Create new dataset: /datasets POST
- Update dataset: /datasets/{id} PUT
- Deleted dataset: /datasets/{id} DELETE
- Collect dataset files from source: /datasets/{id}/collection POST
- Get updates from source: /datasets/{id}/updates GET
- List files of dataset: /datasets/{id}/files GET
- Reclaim dataset local storage: /datasets/{id}/files DELETE

### Pipeline
- List pipeline: /pipelines GET
- View specific pipeline: /pipelines/{id} GET
- Create new pipeline: /pipelines POST
- Update pipeline: /pipelines/{id} PUT
- Delete pipeline: /pipelines/{id} DELETE

### Job
- List jobs: /jobs GET
- View specific job: /jobs/{id} GET
- Create new job (run pipeline): /jobs POST
- Delete job: /jobs/{id} DELETE
- Get job status: /jobs/{id}/status GET