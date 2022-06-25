# Create a resource group
resource "azurerm_resource_group" "rg" {
  name     = var.resource_group_name
  location = var.location

  tags = {
    environment = "cr-test"
  }
}

# create azure container registry
resource "azurerm_container_registry" "acr" {
  name                = var.acr_name
  resource_group_name = azurerm_resource_group.rg.name
  location            = var.location
  sku                 = "Basic"
  admin_enabled       = false
}

# add image to the container registry
resource "null_resource" "build-and-push-image" {
  provisioner "local-exec" {
    command = "az acr build --image rule-eval-main-service:v6 --registry ${var.acr_name} --file ../../rule-evaluator-2/target/docker/stage/Dockerfile ../../rule-evaluator-2/target/docker/stage"
  }

  depends_on = [azurerm_container_registry.acr]
}

# create AKS cluster
resource "azurerm_kubernetes_cluster" "aks" {
  name                = var.cluster_name
  location            = azurerm_resource_group.rg.location
  resource_group_name = azurerm_resource_group.rg.name
  dns_prefix          = var.cluster_name
  default_node_pool {
    name                = "system"
    node_count          = var.system_node_count
    vm_size             = "Standard_D4as_v5"
  }

  identity {
    type = "SystemAssigned"
  }
}

resource "azurerm_role_assignment" "role_acrpull" {
  scope                            = azurerm_container_registry.acr.id
  role_definition_name             = "AcrPull"
  principal_id                     = azurerm_kubernetes_cluster.aks.kubelet_identity.0.object_id
  skip_service_principal_aad_check = true
}