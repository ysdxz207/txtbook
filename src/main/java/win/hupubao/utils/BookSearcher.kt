package win.hupubao.utils

import javassist.compiler.ast.Keyword
import org.jsoup.Connection
import win.hupubao.common.http.Page

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