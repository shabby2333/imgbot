# imgbot
## 棒图bot的mirai重构版本
### 功能
在群中发送`/${perfix} ${图片}`可将图片添加到插件存储 该`${groupId}/${perfix}`对应文件夹中
在群中发送`/${perfix}`获取`${groupId}/${perfix}`对应文件夹下 随机一张图片

### 旧版(coolq/mirai-native)迁移
将原存图片文件夹中所有内容 手动拷贝至`${miraiRoot}/data/icu.shabby.imgbot`即可

### 配置
如果您不喜欢前缀为`/`或者列出指令为`ls`, 您可以修改`${miraiRoot}/config/icu.shabby.imgbot`中的配置项