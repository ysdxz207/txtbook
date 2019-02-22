package win.hupubao

import io.javalin.Javalin
import win.hupubao.utils.BookDownloader
import win.hupubao.utils.HistoryUtils


fun main(args: Array<String>) {
    val app = Javalin.create().apply {
        enableStaticFiles("static")
    }.start(8005)


    app.get("/") { ctx ->
        ctx.render("/templates/index.html")
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