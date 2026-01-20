cd admin-frontend
rm -rf dist node_modules/.vite

npm run build

rm -rf /var/www/travel/admin/*

sudo cp -r /root/travel/travel/admin-frontend/dist/* /var/www/travel/admin/
