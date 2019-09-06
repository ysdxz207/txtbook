package win.hupubao.utils

import com.alibaba.fastjson.JSONObject
import com.hupubao.common.http.Page
import org.jsoup.Connection
import win.hupubao.beans.Book
import win.hupubao.beans.PageInfo


object BookCollector {

    private const val timeout = 5000
    private const val retryTimes = 3
    private const val pageSize = 50

    private const val byCategoryUrl = "http://api.zhuishushenqi.com/book/by-categories"

    private val majors = arrayOf("玄幻",
            "仙侠",
            "科幻",
            "奇幻")

    fun getMajorCollectBookList(ratio: Double): List<Book> {
        val bookList = mutableListOf<Book>()

        majors.forEach { major ->
            bookList.addAll(getMajorCollectBookList(ratio, major))
        }
        return bookList
    }


    private fun getMajorCollectBookList(ratio: Double, major: String): List<Book> {

        val bookList = mutableListOf<Book>()
        val pageInfo = requestBooks(1, major)
        bookList.addAll(pageInfo.records)

        if (pageInfo.pages > 1) {
            for (current in 2 until pageInfo.pages) {
                bookList.addAll(requestBooks(current, major).records)
            }
        }

        return bookList.filter { it.ratio.compareTo(ratio) >= 0 }
    }


    private fun requestBooks(current: Int, major: String): PageInfo<Book> {
        val params = JSONObject()
        params["type"] = "reputation"
        params["major"] = major
        params["start"] = (current - 1) * pageSize
        params["limit"] = pageSize
        val response = Page.create().requestTimeout(timeout)
                .connectionTimeout(timeout)
                .readTimeout(timeout)
                .retryTimes(retryTimes)
                .request(byCategoryUrl, params, Connection.Method.GET)

        val result: JSONObject = response.toJson(false) as JSONObject
        val books = result.getJSONArray("books")
        val records = mutableListOf<Book>()
        books.forEach { bookObj ->
            val bookJson = bookObj as JSONObject
            val book = Book()
            book.name = bookJson.getString("title")
            book.author = bookJson.getString("author")
            book.ratio = bookJson.getDouble("retentionRatio")
            book.category = bookJson.getString("majorCate")
            records.add(book)
        }
        val total = result.getIntValue("total")
        val pages = total / pageSize + 1

        val pageInfo = PageInfo<Book>()
        pageInfo.total = total
        pageInfo.current = current
        pageInfo.pages = pages
        pageInfo.records = records

        return pageInfo
    }

}