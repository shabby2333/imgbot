import icu.shabby.imgbot.PERFIX

fun main() {

    val _imageRegex = Regex("^$PERFIX([\\u4e00-\\u9fa5\\w]+)\\s*((\\[图片]\\s*)|(\\[动画表情]\\s*))*\\s*\$")
    println(_imageRegex.matches("/asd"))
    println(_imageRegex.find("/test1[动画表情][图片]")?.groupValues)

}