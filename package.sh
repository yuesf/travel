echo "前端服务"
cd admin-frontend
rm -rf dist node_modules/.vite
echo "前端编译"
npm run build
echo "删除历史"
rm -rf /var/www/travel/admin/*
echo "复制前端"
sudo cp -r /root/travel/travel/admin-frontend/dist/* /var/www/travel/admin/
cd ..
echo "停止服务"
ps -ef | grep java | grep -v grep | awk '{print $2}' | xargs kill -9
echo "编译服务"
mvn clean package -DskipTests -Dskip.frontend.build=true
echo "启动服务"
nohup java -jar travel/target/travel-platform-1.0.0.jar \
--server.port=8081 \
--spring.profiles.active=prod \
--MYSQL_PWD= \
--WECHAT_APPID=wxec792e1911ef2bde \
--WECHAT_SECRET=0275a3dc14da6e1280837611d98fea66 \
--OSS_ENABLED=true \
--OSS_ENDPOINT=oss-cn-beijing.aliyuncs.com \
--OSS_ACCESS_KEY_ID=LTAI5t9mJwcKfpxwfptvPkNT \
--OSS_ACCESS_KEY_SECRET=Byp7tivKt0jv9j3l9Huh8Avertm0S5 \
--OSS_BUCKET_NAME=wuqiao-travel
  2>&1 &