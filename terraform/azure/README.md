Steps to deploy this app on Microsoft Azure cloud

1. Build Dockerfile and dist for main-service by using sbt shell command "docker:stage"
2. Azure cli login. Execute command "az login" and follow the instructions
3. Terraform apply. To do this navigate to this file's directory in cmd, and execute "terraform apply"