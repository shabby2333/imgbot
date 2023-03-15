package icu.shabby.imgbot

import net.mamoe.mirai.console.plugin.jvm.JvmPluginDescription
import net.mamoe.mirai.console.plugin.jvm.KotlinPlugin
import net.mamoe.mirai.contact.Contact.Companion.sendImage
import net.mamoe.mirai.event.GlobalEventChannel
import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.message.data.Image
import net.mamoe.mirai.message.data.Image.Key.queryUrl
import net.mamoe.mirai.utils.MiraiInternalApi
import java.io.InputStream
import java.net.URL
import java.security.MessageDigest
import kotlin.random.Random

/**
 * 使用 kotlin 版请把
 * `src/main/resources/META-INF.services/net.mamoe.mirai.console.plugin.jvm.JvmPlugin`
 * 文件内容改成 `org.example.mirai.plugin.PluginMain` 也就是当前主类全类名
 *
 * 使用 kotlin 可以把 java 源集删除不会对项目有影响
 *
 * 在 `settings.gradle.kts` 里改构建的插件名称、依赖库和插件版本
 *
 * 在该示例下的 [JvmPluginDescription] 修改插件名称，id和版本，etc
 *
 * 可以使用 `src/test/kotlin/RunMirai.kt` 在 ide 里直接调试，
 * 不用复制到 mirai-console-loader 或其他启动器中调试
 */

// PERFIX不可为 正则需要转义的字符串
val PERFIX = "/"
val _imageRegex = Regex("""^${PERFIX}([\S^\[]+)\s*(\[图片]\s*)*\s*${'$'}""")
val _random = Random(System.currentTimeMillis())

object ImgbotMain : KotlinPlugin(
    JvmPluginDescription(
        id = "icu.shabby.imgbot",
        name = "棒图bot",
        version = "0.1.1"
    ) {
        author("shabby")
        info("jvm平台重构的棒图bot（群友黑历史处刑）")
    }
) {
    @OptIn(MiraiInternalApi::class)
    override fun onEnable() {
        logger.info("Imgbot loaded")
        val eventChannel = GlobalEventChannel.parentScope(this)
        eventChannel.subscribeAlways<GroupMessageEvent> {
            val messageStr = message.contentToString()

            // 查询群组对应文件夹是否存在，不存在则创建
            val groupDataPath = resolveDataPath("${group.id}")
            val groupDataFolder = groupDataPath.toFile()
            if (!groupDataFolder.exists()) groupDataFolder.mkdirs()

            // ls逻辑
            if (messageStr == "ls") {
                val list = groupDataFolder.list()
                if (null == list || list.isEmpty()) return@subscribeAlways
                group.sendMessage(list.joinToString(", "))
                return@subscribeAlways
            }

            //增、查图片均需'/'为前缀触发
            if (!messageStr.startsWith(PERFIX)) return@subscribeAlways

            val matchResult = _imageRegex.find(messageStr)
            if (matchResult == null || matchResult.groups.isEmpty()) return@subscribeAlways

            // 存图片操作
            if (matchResult.groupValues.size >= 3 && matchResult.groupValues[2].isNotEmpty()) {
                val imgs = message.filterIsInstance<Image>()
                if (imgs.isEmpty()) {
                    logger.warning("匹配到[图片]，但未找到Image对象，该提示在发送者发'[图片]'时可能不是错误")
                    return@subscribeAlways
                }
                val msg = matchResult.groupValues[1]
                val dir = groupDataPath.resolve(msg)
                if (!dir.toFile().exists()) dir.toFile().mkdirs()

                imgs.forEach {
                    var stream: InputStream? = null
                    try {
                        if (!groupDataFolder.exists()) groupDataFolder.mkdirs()
                        stream = URL(it.queryUrl()).openConnection().getInputStream()
                        val memory = stream.readBytes()
                        val md5 = MessageDigest.getInstance("MD5").digest(memory)
                            .joinToString("") { e -> "%02x".format(e) }
                        val fileName = "${md5}.${it.imageType.formatName}"
                        val path = dir.resolve(fileName)
                        val file = path.toFile()
                        if (file.exists()) file.delete()
                        file.createNewFile()
                        file.writeBytes(memory)

                        logger.info("[${group.id}]: [${sender.nick}(${sender.id})]发送的图片已经存储到${file.absolutePath}")
                        group.sendMessage("存好啦~")
                    } finally {
                        stream?.close()
                    }
                }
            }
            // 取图片操作
            else {
                val msg = matchResult.groupValues[1]
                val dir = groupDataPath.resolve(msg).toFile()
                if (!dir.exists()) return@subscribeAlways
                if (dir.isFile) {
                    group.sendImage(dir)
                    return@subscribeAlways
                }

                val fileNames = dir.list()!!
                val imgFileName = fileNames[_random.nextInt(fileNames.size)]
                group.sendImage(dir.toPath().resolve(imgFileName).toFile())
                return@subscribeAlways
            }

        }

    }
}
