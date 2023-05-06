# Project

Simple project with Scala, Akka, Cassandra, Kafka and Circe. I divided the project into 6 modules to have a better scalability, maintenance and performance.

## Setup the environment

Generate the docker images:

    sbt docker:publishLocal

Set the deployment version (ex: 0.1.0-SNAPSHOT):

    export VERSION=0.1.0-SNAPSHOT

Launch the services:

    docker-compose up

## Interacting with the sample

### Postman

You can test the endpoints by using the postman collection `api.postman_collection.json`. You can directly import it into Postman.

### Endpoints

Get all available vehicles:

    curl --location --request GET 'http://127.0.0.1:8080/api/vehicles/list'

Get vehicle last position by vehicle_id (ex: vehicle_id=1):

    curl --location --request GET 'http://127.0.0.1:8080/api/vehicles/vehicle/1/lastPosition'

Get all filled tiles:

    curl --location --request GET 'http://127.0.0.1:8080/api/tiles/filled'

Get available vehicles from tile_id (ex: tile_id=1_1):

    curl --location --request GET 'http://127.0.0.1:8080/api/tiles/tile/1_1/availableVehicles'

Count available vehicles per tile:

    curl --location --request GET 'hhttp://127.0.0.1:8080/api/tiles/usecase/vehicleCount'
