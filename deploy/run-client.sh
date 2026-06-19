#!/bin/bash
export SERVER_HOST="45.146.131.117"
export SERVER_PORT="45205"

cd ../
gradle :client:shadowJar
java -jar client/build/libs/client-all.jar
