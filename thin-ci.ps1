mvn -B package

docker build -f thin.Dockerfile . -t registry.cn-hangzhou.aliyuncs.com/crucialize/numeral:latest

docker push registry.cn-hangzhou.aliyuncs.com/crucialize/numeral:latest