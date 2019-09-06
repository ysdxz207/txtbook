package win.hupubao.utils

import com.hupubao.common.http.Page
import org.jsoup.Connection

private const val search_engine_url = "http://caup.cn"
private const val TIMEOUT = 5000

fun search(bookName: String) {
    val res = Page.create()
            .connectionTimeout(TIMEOUT)
            .requestTimeout(TIMEOUT)
            .readTimeout(TIMEOUT).request(search_engine_url, null, Connection.Method.GET)

    val cells = res.parse().select(".res_cell")
    cells.forEach {

    }
}

fun main(args: Array<String>) {

}