#!/bin/sh

mongod --fork --logpath /var/log/mongodb_provision.log

# install credentials
mongo admin --eval "db.createUser( { user:'admin' , pwd:'devmongo123', roles: []})"
mongod --shutdown
cat /var/log/mongodb_provision.log

mongod --bind_ip 0.0.0.0