# 默认图片资源

## default-product.png

这是商品的默认占位图片，当商品没有图片时显示。

### 图片要求
- 尺寸：建议 400x300 像素或更大
- 格式：PNG（支持透明背景）
- 内容：可以是简单的占位图、品牌 Logo 或通用商品图标

### 如何添加图片

1. 将你的默认商品图片命名为 `default-product.png`
2. 放置在此目录下：`miniprogram/static/images/default-product.png`
3. 确保图片文件存在，否则小程序会尝试从服务器加载，导致 500 错误

### 临时解决方案

如果暂时没有图片，可以使用以下 base64 编码的 1x1 透明 PNG 作为占位图：

```
iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mNk+M9QDwADhgGAWjR9awAAAABJRU5ErkJggg==
```

将上述 base64 字符串解码后保存为 `default-product.png` 文件。
