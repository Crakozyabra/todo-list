# Stop and remove containers. Remove images. Repack application. Run containers

echo 'Stop containers:'
docker ps
docker stop server-with-app-container db-container
sleep 3

echo 'Remove containers:'
docker ps -a
docker rm server-with-app-container db-container
sleep 3

echo 'Remove images:'
docker images
docker rmi server-with-app-image mysql:8.0.33
sleep 3

echo 'Repack .war application:'
mvn clean package
sleep 3

echo 'Run containers:'
docker compose up