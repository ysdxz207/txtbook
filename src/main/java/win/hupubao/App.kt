package win.hupubao

import io.javalin.Javalin
import win.hupubao.utils.BookDownloader
import win.hupubao.utils.HistoryUtils


fun main(args: Array<String>) {
    val app = Javalin.create().apply {
        enableStaticFiles("static")
    }.start(7000)


    app.get("/") { ctx ->
        ctx.render("/templates/index.html", mapOf("env" to "John", "dbUrl" to "Doe"))
    }

    app.get("/chapters") { ctx ->
        ctx.json(BookDownloader.getChapterList(ctx.queryParam("url", "")))
    }

    app.get("/content") { ctx ->
        ctx.json(BookDownloader.parseChapterContent(ctx.queryParam("url", "")!!))
    }

    app.get("/pack") { ctx ->
        ctx.json(BookDownloader.downloadBook(ctx.queryParam("url", "")!!,
                ctx.queryParam("name", "")!!))
    }

    app.get("/history") { ctx ->
        ctx.json(HistoryUtils.getHistory())
    }
}