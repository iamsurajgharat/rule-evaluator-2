# rule-evaluator-2

## Use minikube docker daemon. We need to run this on every terminal where we want to use minikube's docker daemon.
eval $(minikube docker-env)

## SBT command to create and publish docker image to local docker daemon
> docker publishLocal

## Run the created docker image as container
docker run --rm -p 9000:9000 --env APPLICATION_SECRET=abcdefghijklmnopqrstuvwxyz --env RUNTIME_MODE=Local surajgharat/rule-eval-main-service:4

## Minikube - create node port service
kubectl apply -f main-node-port-service.yaml 

## Minikube - create role and role bindings
kubectl apply -f akka-cluster.yaml 

## Minikube - create main service deployment
kubectl apply -f main-deployment.yaml 