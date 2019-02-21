package win.hupubao.utils

import com.alibaba.fastjson.JSONArray
import com.alibaba.fastjson.JSONObject
import org.apache.commons.io.FileUtils
import win.hupubao.beans.History
import java.io.File

object HistoryUtils {

    val encoding = Charsets.UTF_8
    val path = System.getProperty("user.dir") + "/history/download.history"
    var file: File

    init {
        file = File(path)
        if (!file.parentFile.exists()) {
            file.parentFile.mkdirs()
        }
        if (!file.exists()) {
            file.createNewFile()
        }
    }

    @Synchronized
    fun saveHistory(history: History) {
        val configs = getHistory()
        configs.removeIf { (it as JSONObject).getString("name") == history.name }
        configs.add(history)
        file.writeText(configs.toJSONString(), encoding)
    }

    fun getHistory(): JSONArray {
        val str = FileUtils.readFileToString(file, encoding)
        var configs = JSONArray()
        if (!str.isNullOrEmpty()) {
            configs = JSONArray.parseArray(str)
        }
        return configs
    }
}

