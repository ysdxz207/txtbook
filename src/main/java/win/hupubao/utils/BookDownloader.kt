package win.hupubao.utils

import kotlinx.coroutines.*
import org.apache.commons.io.FileUtils
import org.jsoup.Connection
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import win.hupubao.beans.Chapter
import win.hupubao.beans.History
import win.hupubao.common.http.Page
import java.io.File
import java.net.URL
import kotlin.collections.distinctBy
import kotlin.collections.filter
import kotlin.collections.forEachIndexed
import kotlin.collections.joinToString

object BookDownloader {

    val USER_AGENT = "Mozilla/5.0 (iPhone; CPU iPhone OS 12_1_3 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/12.0 Mobile/15E148 Safari/604.1"
    val TIMEOUT = 5000

    val OUT_PATH = "/files/txtbook/"

    var domain: String = ""

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
                val nextPageUrl = domain + document.select("a").filter { it.text() == "下一页" }[0].attr("href")
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
        val e = document.select("div").filter { !it.textNodes().isEmpty() && it.textNodes().joinToString { it.text().trim() }.length > 50 }[0]
        e.select("p").remove()

        return e.textNodes().filter { !it.isBlank }.joinToString(separator = "\n", transform = { "    " + it.text().trim() })
    }

    fun parseChapterElements(url: String): List<Element> {
        domain = getDomain(url)

        val document = request(url)

        // 获取章节a标签列表
        var chapterElements = document.select("dd>a").distinctBy { it.attr("href") }
        if (chapterElements.isEmpty()) {
            chapterElements = document.select("li>a").distinctBy { it.attr("href") }
        }
        if (chapterElements.isEmpty()) {
            chapterElements = document.select("p>a").distinctBy { it.attr("href") }
        }

        if (chapterElements.isEmpty()) {
            error("未能获取到章节列表")
        }

        return chapterElements.filter { !it.text().contains("页面底部") && !it.text().contains("页面顶部") }
    }

    fun parseChapterInfo(num: Int, e: Element, withContent: Boolean): Chapter {
        val title = e.text()
        val href = e.attr("href")

        val url = getUrl(href)
        val chapter = Chapter()

        if (withContent) {
            val content = parseChapterContent(url)
            chapter.content = content
        }

        chapter.title = title
        chapter.url = url
        chapter.num = num
        return chapter
    }

    fun getUrl(urx: String): String {
        return when {
            urx.startsWith("/") -> "$domain$urx"
            urx.startsWith("http") -> urx
            else -> "$domain/$urx"
        }
    }

    fun getDomain(url: String): String {
        return URL(url).protocol + "://" + URL(url).host
    }

    fun downloadBook(url: String,
                     name: String) {
        GlobalScope.launch {

            val file = File("$OUT_PATH$name.txt")
            if (!file.parentFile.exists()) {
                file.parentFile.mkdirs()
            }
            if (file.exists()) {
                file.delete()
            }

            val chapterElements = parseChapterElements(url)

            val jobs = mutableListOf<Job>()

            val chapters = mutableListOf<Chapter>()

            runBlocking {
                chapterElements.forEachIndexed { index, e ->

                    val job = GlobalScope.launch {
                        chapters.add(parseChapterInfo(index + 1, e, true))
                        // 更新进度
                        HistoryUtils.saveHistory(History(name, getPercentage(index + 1, chapterElements.size)))
                    }
                    jobs.add(job)
                }


                while (!jobs.all { it.isCompleted }) {
                }


                if (chapters.isEmpty()) {
                    return@runBlocking
                }

                chapters.sortBy { it.num }
                chapters.forEachIndexed { index, chapter ->
                    FileUtils.writeStringToFile(file, chapter.title + "\n\n" + chapter.content + "\n\n\n", "UTF-8", true)
                    // 更新保存进度
//                HistoryUtils.saveHistory(History(name, getPercentage(index + 1, chapterElements.size)))
                }
            }
        }
    }

    fun getPercentage(a: Int, b: Int): Double {
        return String.format("%.2f", a.toDouble().div(b) * 100).toDouble()
    }

    fun getChapterList(url: String?): List<Chapter> {
        if (url.isNullOrEmpty()) {
            error("章节目录地址不正确")
        }

        val chapters = mutableListOf<Chapter>()
        val chapterElements = parseChapterElements(url)

        chapterElements.forEachIndexed { index, e ->
            chapters.add(parseChapterInfo(index + 1, e, false))
        }

        return chapters
    }


    @JvmStatic
    fun main(args: Array<String>) {

        downloadBook("https://m.x23wxw.com/0/122/", "book")
    }

}