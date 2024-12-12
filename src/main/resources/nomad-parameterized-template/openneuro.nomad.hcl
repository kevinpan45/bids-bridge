job "openneuro-dataset-sync-to-minio" {
  datacenters = ["dc1"]
  type        = "batch"
  parameterized {
    meta_required = ["DATASET"]
  }
  group "dataset-sync" {
    task "openneuro" {
      driver = "docker"

      template {
        data        = <<EOF
                        {{- with nomadVar "nomad/jobs" -}}
                        [minio]
                        type = s3
                        provider = Minio
                        env_auth = false
                        access_key_id = {{ .MINIO_ACCESS_KEY }}
                        secret_access_key = {{ .MINIO_SECRECT_KEY }}
                        region =
                        endpoint = {{ .MINIO_ENDPOINT}}

                        [s3]
                        type = s3
                        provider = AWS
                        endpoint = https://s3.amazonaws.com
                        {{- end }}
                      EOF
        destination = "local/rclone.conf"
      }

      config {
        image = "rclone/rclone:latest"
        args  = ["sync", "s3:openneuro.org/${NOMAD_META_DATASET}", "minio:bids/${NOMAD_META_DATASET}", "--verbose"]
        mounts = [
          {
            type = "bind"
            source = "local"
            target = "/config/rclone/"
          }
        ]
      }
    }
  }
}
