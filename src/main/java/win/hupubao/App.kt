package win.hupubao

import io.javalin.Javalin
import win.hupubao.utils.BookDownloader
import win.hupubao.utils.HistoryUtils
import java.io.File
import java.io.FileInputStream


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

    app.get("/download") { ctx ->
        val file = BookDownloader.getBookFile(ctx.queryParam("name", "")!!)
        ctx.res.setContentLengthLong(file.length())

        val inputStream = file.inputStream()
        ctx.res.outputStream.write(inputStream.readBytes())
    }
}