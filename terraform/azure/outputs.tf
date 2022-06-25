output "aks_id" {
  value = azurerm_kubernetes_cluster.aks.id
}

output "acr_id" {
  value = azurerm_container_registry.acr.id
}

output "acr_login_server" {
  value = azurerm_container_registry.acr.login_server
}