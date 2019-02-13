package win.hupubao.utils

import kotlinx.coroutines.*
import org.apache.commons.io.FileUtils
import org.jsoup.Connection
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import win.hupubao.beans.Chapter
import win.hupubao.common.http.Page
import java.io.File
import kotlin.collections.HashMap
import kotlin.collections.distinctBy
import kotlin.collections.filter
import kotlin.collections.forEach
import kotlin.collections.forEachIndexed
import kotlin.collections.joinToString
import kotlin.collections.set

object TestUtils {

    val USER_AGENT = "Mozilla/5.0 (iPhone; CPU iPhone OS 12_1_3 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/12.0 Mobile/15E148 Safari/604.1"
    val TIMEOUT = 5000

    val BASE_URL = "https://m.x23wxw.com"
    val URI = "/135/135574/"
    val OUT_PATH = "F:/book.txt"

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

    fun downloadBook(uri: String) {
        val file = File(OUT_PATH)
        if (file.exists()) {
            file.delete()
        }

        val document = request("$BASE_URL$uri")
        val elements = document.select("a")

        val chapterList = elements.filter { it.attr("href").matches(".*[0-9]+\\.html".toRegex()) }
                .distinctBy { it.attr("href") }

        val map = HashMap<Int, Element>()

        chapterList.forEachIndexed { index, a ->
            map[index + 1] = a
        }

        val jobs = mutableListOf<Job>()

        val chapters = mutableListOf<Chapter>()

        runBlocking {
            map.entries.forEach{ entry ->

                val job = GlobalScope.launch {
                    val title = entry.value.text()
                    val url = BASE_URL + entry.value.attr("href")

                    val content = parseChapterContent(url)

                    val chapter = Chapter()
                    chapter.title = title
                    chapter.url = url
                    chapter.content = content
                    chapter.num = entry.key
                    chapters.add(chapter)
                }
                jobs.add(job)
            }

            jobs.forEachIndexed { index, job ->
                runBlocking {
                    job.join()
                    println("已下载${getPercentage(index + 1, chapterList.size)}")
                }
            }


            chapters.sortBy { it.num }
            chapters.forEachIndexed{ index, chapter ->
                FileUtils.writeStringToFile(file, chapter.title + "\n\n" + chapter.content + "\n\n\n", "UTF-8", true)
                println("${chapter.title} - 已保存${getPercentage(index + 1, chapterList.size)} [${chapter.num}/${chapterList.size}]")
            }
        }
    }

    fun getPercentage(a: Int, b: Int): String {
        return (String.format("%.2f", a.toDouble().div(b) * 100).toDouble()).toString() + "%"
    }

    @JvmStatic
    fun main(args: Array<String>) {

        downloadBook(URI)
    }
}