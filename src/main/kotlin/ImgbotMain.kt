package icu.shabby.imgbot

import net.mamoe.mirai.console.plugin.jvm.JvmPluginDescription
import net.mamoe.mirai.console.plugin.jvm.KotlinPlugin
import net.mamoe.mirai.contact.Contact.Companion.sendImage
import net.mamoe.mirai.event.GlobalEventChannel
import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.message.data.Image

object ImgbotMain : KotlinPlugin(
    JvmPluginDescription(
        id = "icu.shabby.imgbot",
        name = "棒图bot",
        version = "0.2.3"
    ) {
        author("shabby")
        info("jvm平台重构的棒图bot（群友黑历史处刑）")
    }
) {
    override fun onEnable() {
        // 加载配置文件
        ImgbotConfig.reload()

        val eventChannel = GlobalEventChannel.parentScope(this)
        eventChannel.subscribeAlways<GroupMessageEvent> {
            val messageStr = message.contentToString()

            // 查询群组对应文件夹是否存在，不存在则创建
            val groupDataPath = resolveDataPath("${group.id}")
            val groupDataFolder = groupDataPath.toFile()
            mkdirIfPathNonExists(groupDataPath)

            // ls逻辑
            if (messageStr == ImgbotConfig.listDirsCommand) {
                val list = groupDataFolder.list()
                if (null == list || list.isEmpty()) return@subscribeAlways
                group.sendMessage(list.joinToString(", "))
                return@subscribeAlways
            }

            // 存图片操作
            if (messageStr.startsWith(ImgbotConfig.saveImagePrefix) and message.filterIsInstance<Image>().isNotEmpty()) {
                val images = message.filterIsInstance<Image>()
                val msg = convertMsgToDirKey(messageStr, ImgbotConfig.saveImagePrefix)
                val dir = groupDataPath.resolve(msg)
                if(!dir.toFile().canonicalPath.startsWith(groupDataPath.toFile().canonicalPath)) {
                    logger.warning("目录解析到群组目录以外范围, 拒绝存取: ${dir.toFile().canonicalPath}")
                    return@subscribeAlways
                }

                mkdirIfPathNonExists(dir)
                val (success, fail, override) = saveImages(images, dir)
                group.sendMessage(
                    "图片共${images.size}张存储到: ${msg}, 成功${success}张, 失败${fail}张" +
                        (if (override > 0) ", hash冲突覆盖${override}张" else "")
                )
                return@subscribeAlways
            }
            // 取图片操作
            else if (messageStr.startsWith(ImgbotConfig.getImagePrefix)) {
                val msg = convertMsgToDirKey(messageStr, ImgbotConfig.getImagePrefix)
                val dir = groupDataPath.resolve(msg)
                if(!dir.toFile().canonicalPath.startsWith(groupDataPath.toFile().canonicalPath)) {
                    logger.warning("目录解析到群组目录以外范围, 拒绝存取: ${dir.toFile().canonicalPath}")
                    return@subscribeAlways
                }
                if (!checkPathExists(dir) || msg.isEmpty()) return@subscribeAlways
                logger.info(dir.toFile().canonicalPath)
                val fileNames = dir.toFile().list{e, _ -> !e.isFile}
                if(fileNames == null || fileNames.isEmpty()) return@subscribeAlways
                val imgFileName = fileNames[_random.nextInt(fileNames.size)]
                group.sendImage(dir.resolve(imgFileName).toFile())
                return@subscribeAlways
            }
        }
    }
}
