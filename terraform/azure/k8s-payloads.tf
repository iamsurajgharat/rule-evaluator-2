resource "kubernetes_deployment_v1" "rule-eval-main-service-deployment" {
    metadata {
        name = "rule-eval-main-service-deployment"
    }

    spec {
        replicas = 2
        selector {
            match_labels = {
                app = "ruleevalactorsystem"
            }
        }

        template {
            metadata {
              labels = {
                  app = "ruleevalactorsystem"
              }
            }

            spec {
                container {
                  image = "${azurerm_container_registry.acr.login_server}/rule-eval-main-service:v7"
                  name = "rule-eval-main-service"
                  env {
                    name = "APPLICATION_SECRET"
                    value = "${var.app_secret}"
                  }

                  env {
                    name = "RUNTIME_MODE"
                    value = "k8s"
                  }
                }
            }
        }
    }
}

resource "kubernetes_service_v1" "loadbalancer" {
  metadata {
    name = "rule-eval-main-app-load-balancer"
  }
  spec {
    selector = {
      app = "ruleevalactorsystem"
    }
    
    port {
      port        = 80
      target_port = 9000
    }

    type = "LoadBalancer"
  }
}

resource "kubernetes_cluster_role_v1" "pod-reader-aks-role" {
  metadata {
    name = "pod-reader"
  }

  rule {
    api_groups = [""]
    resources  = ["pods"]
    verbs      = ["get", "list", "watch"]
  }
}

resource "kubernetes_cluster_role_binding_v1" "read-pods-aks-role-binding" {
  metadata {
    name = "read-pods"
  }
  role_ref {
    api_group = "rbac.authorization.k8s.io"
    kind      = "ClusterRole"
    name      = "pod-reader"
  }
  subject {
    kind      = "User"
    name      = "system:serviceaccount:default:default"
  }
}
