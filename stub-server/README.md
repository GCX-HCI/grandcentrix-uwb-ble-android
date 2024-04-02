# Stub server for demonstration purpose only

If you want to show case the network using the open api generator use this server as a dump response 
machine. This "server" will only respond to [predefined requests and responses](v1/).

### How to use this
Since this server is dockerized you need to build the image first:

`docker build -t stub .`

To run the image use this command:

`docker run -it --rm -p 8081:8081 stub`

If you've made some changes to the contract rerun the image with this command:

`docker run -it -v $(pwd)/v1/:/app/contracts/v1/ --rm -p 8081:8081 stub`

The stub server is accessible via http://localhost:8081