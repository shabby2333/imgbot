import icu.shabby.imgbot.PERFIX

fun main() {

    val _imageRegex = Regex("^$PERFIX([\\S^\\[]+)\\s*(\\[图片]\\s*)*\\s*\$")
    println(_imageRegex.matches("/asd"))
    println(_imageRegex.matches("/asd[图片]"))

}