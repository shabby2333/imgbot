package icu.shabby.imgbot

import net.mamoe.mirai.message.data.Image
import net.mamoe.mirai.message.data.Image.Key.queryUrl
import net.mamoe.mirai.utils.MiraiInternalApi
import java.io.File
import java.io.InputStream
import java.net.URL
import java.nio.file.Path
import java.security.MessageDigest
import kotlin.random.Random

val _random = Random(System.currentTimeMillis())

fun mkdirIfPathNonExists(path: Path) {
    return mkdirIfPathNonExists(path.toFile())
}
fun mkdirIfPathNonExists(path: File) {
    if(!path.exists())
        path.mkdirs()
}

fun checkPathExists(path: Path): Boolean {
    return checkPathExists(path.toFile())
}
fun checkPathExists(path: File): Boolean {
    return path.exists()
}

@OptIn(MiraiInternalApi::class)
suspend fun saveImages(images: List<Image>, dir: Path):Triple<Int, Int, Int> {
    var success = 0
    var fail = 0
    var override = 0
    images.forEach {
        var stream: InputStream? = null
        try {
            mkdirIfPathNonExists(dir)
            stream = URL(it.queryUrl()).openConnection().getInputStream()
            val memory = stream.readBytes()
            val md5 = MessageDigest.getInstance("MD5").digest(memory)
                .joinToString("") { e -> "%02x".format(e) }
            val fileName = "${md5}.${it.imageType.formatName}"
            val path = dir.resolve(fileName)
            val file = path.toFile()
            if (file.exists()) {
                override++
                file.delete()
            }
            file.createNewFile()
            file.writeBytes(memory)
            success++
        } catch (e: Exception) {
            fail++
        } finally {
            stream?.close()
        }
    }

    return Triple(success, fail, override)
}

fun convertMsgToDirKey(msg: String, perfix: String):String {
    return msg.removePrefix(perfix)
        .replace("[动画表情]", "")
        .replace("[图片]", "").trim()
}