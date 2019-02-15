package win.hupubao

import io.javalin.Javalin
import win.hupubao.utils.BookDownloader


fun main(args: Array<String>) {
    val app = Javalin.create().apply {
        enableStaticFiles("static")
    }.start(7000)


    app.get("/") { ctx ->
        ctx.render("/templates/index.html", mapOf("env" to "John", "dbUrl" to "Doe"))
    }

    app.get("/try") { ctx ->
        ctx.json(BookDownloader.tryToParse(ctx.queryParam("url", "")))
    }
}