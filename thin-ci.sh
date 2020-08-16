#!/bin/bash

set -e

echo 'make sure the code is the newest'
sleep 2

echo "checking wait-for-it.sh integrity"
echo "58483b01170dc5cdfb602f5a6da7f6ee49d4482619a6018983207780dc27bdbc *wait-for-it.sh" | sha256sum -c -

mvn package > mvn-package-output.txt

docker build -f thin.Dockerfile . -t registry.cn-hangzhou.aliyuncs.com/crucialize/numeral:latest

echo
echo
echo 'build image success!'

docker push registry.cn-hangzhou.aliyuncs.com/crucialize/numeral:latest

echo
echo
echo 'push success!'