git pull
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
