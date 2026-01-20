ps -ef | grep java | grep -v grep | awk '{print $2}' | xargs kill -9

cd admin-frontend
rm -rf dist node_modules/.vite

npm run build

rm -rf /var/www/travel/admin/*

sudo cp -r /root/travel/travel/admin-frontend/dist/* /var/www/travel/admin/
