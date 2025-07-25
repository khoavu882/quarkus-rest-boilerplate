#!/bin/bash
echo "Setting up development environment..."
docker-compose -f src/main/docker/docker-compose.yml up -d
echo "Waiting for services to start..."
sleep 10
./gradlew quarkusDev