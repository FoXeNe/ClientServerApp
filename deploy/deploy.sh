#!/bin/bash

USER="postgres"
PASSWORD="postgres"

git clone https://github.com/FoXeNe/ClientServerApp
cd ClientServerApp
git checkout feat/deploy

cat <<EOF >.env
DB_PORT=5432
DB_NAME=studs
DB_USER=$USER
DB_PASSWORD=$PASSWORD
EOF

docker-compose up -d
