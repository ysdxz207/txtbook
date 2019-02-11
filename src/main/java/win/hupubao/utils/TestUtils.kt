package win.hupubao.utils

import org.jsoup.Jsoup

object TestUtils {

    val BASE_URL = "https://m.x23wxw.com"


    fun parseChapterContent(url: String): String {
        val document = Jsoup.connect(url).execute().parse()


        val pageSizeE = document.select("p")

        val e = document.select("div").filter{!it.textNodes().isEmpty() && it.text().length > 50}[0]
        e.select("p").remove()

        return e.textNodes().filter { !it.isBlank }.joinToString(separator = "\n", transform = {"\t" + it.text().trim()})
    }

    @JvmStatic
    fun main(args: Array<String>) {
        val response = Jsoup.connect("$BASE_URL/135/135574/").execute()
        val document = response.parse()
        val elements = document.select("a")

        elements.forEach { a ->
            if (a.attr("href").matches(".*[0-9]+\\.html".toRegex())) {
                val title = a.text()
                val url = BASE_URL + a.attr("href")

                val content = parseChapterContent(url)
                println(content)
            }
        }
    }
}