output "aks_id" {
  value = azurerm_kubernetes_cluster.aks.id
}

output "acr_id" {
  value = azurerm_container_registry.acr.id
}

output "acr_login_server" {
  value = azurerm_container_registry.acr.login_server
}

output "load_balancer_ip" {
  value = kubernetes_service_v1.loadbalancer.status.0.load_balancer.0.ingress.0.ip
}