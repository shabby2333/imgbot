package icu.shabby.imgbot

import net.mamoe.mirai.console.data.AutoSavePluginConfig
import net.mamoe.mirai.console.data.value

object ImgbotConfig: AutoSavePluginConfig("config") {
    var saveImagePrefix: String by value("/")
    var getImagePrefix: String by value("/")
    var listDirsCommand: String by value("ls")
}