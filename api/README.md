#API

Aerostat serverless presentation api gateway implemenation.

## Dependencies
1. Ensure you have the repo submodules checked out 
   (`git submodule update --init` if you don't)
1. cd `vendor/aws-apigateway-importer`
1. `mvn assembly:assembly`

## Deployment
`bin/aws-api-import` will take the args available at
https://github.com/awslabs/aws-apigateway-importer/tree/aws-apigateway-importer-1.0.1
