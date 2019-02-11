package win.hupubao.utils

import org.apache.commons.io.FileUtils
import org.jsoup.Connection
import org.jsoup.nodes.Document
import win.hupubao.common.http.Page
import java.io.File

object TestUtils {

    val USER_AGENT = "Mozilla/5.0 (iPhone; CPU iPhone OS 12_1_3 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/12.0 Mobile/15E148 Safari/604.1"
    val TIMEOUT = 5000

    val BASE_URL = "https://m.x23wxw.com"
    val OUT_PATH = "F:/迷途.txt"

    fun request(url: String): Document {
        return Page.create().userAgent(USER_AGENT).readTimeout(TIMEOUT).retryTimes(6).request(url, null, Connection.Method.GET).parse()
    }

    fun parseChapterContent(url: String): String {
        val document = request(url)

        val matchResult = "([0-9]+)/([0-9]+)页".toRegex().find(document.html())

        if (matchResult != null && matchResult.groupValues.size == 3) {
            val pageCount = matchResult.groupValues[2].toInt()

            var contentCurrent = parseChapterPageContent(document)

            for (n in 2..pageCount) {
                // 获取下一页地址
                val nextPageUrl = BASE_URL + document.select("a").filter { it.text() == "下一页" }[0].attr("href")
                contentCurrent += parseChapterPageContent(nextPageUrl)
            }
            return contentCurrent
        }

        return parseChapterPageContent(document)
    }

    fun parseChapterPageContent(url: String): String {
        return parseChapterPageContent(request(url))
    }

    fun parseChapterPageContent(document: Document): String {
        val e = document.select("div").filter { !it.textNodes().isEmpty() && it.text().length > 50 }[0]
        e.select("p").remove()

        return e.textNodes().filter { !it.isBlank }.joinToString(separator = "\n", transform = { "\t" + it.text().trim() })
    }

    @JvmStatic
    fun main(args: Array<String>) {
        val file = File(OUT_PATH)
        if (file.exists()) {
            file.delete()
        }

        val document = request("$BASE_URL/135/135574/")
        val elements = document.select("a")

        val chapterList = elements.filter { it.attr("href").matches(".*[0-9]+\\.html".toRegex()) }

        var n = 0
        chapterList.forEach { a ->
            ++n

            val title = a.text()
            val url = BASE_URL + a.attr("href")

            if (n < 206) {
                return@forEach
            }

            val content = parseChapterContent(url)

            FileUtils.writeStringToFile(file, title + "\n\n" + content + "\n\n\n", "UTF-8", true)

            println("$title - 已下载 [${n}/${chapterList.size}]")
        }
    }
}