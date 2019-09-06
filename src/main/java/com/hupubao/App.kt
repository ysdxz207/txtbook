package com.hupubao

import com.alibaba.fastjson.JSONObject
import io.javalin.Javalin
import com.hupubao.utils.BookCollector
import com.hupubao.utils.BookDownloader
import com.hupubao.utils.HistoryUtils


fun main(args: Array<String>) {
    val app = Javalin.create().apply {
        enableStaticFiles("static")
    }.start(8005)


    app.get("/") { ctx ->
        ctx.render("/templates/index.html")
    }

    app.get("/chapters") { ctx ->
        val json = JSONObject()
        try {

            json["list"] = BookDownloader.getChapterList(ctx.queryParam("url", ""))
            json["success"] = true
            ctx.json(json)
        } catch (e: Exception) {
            json["success"] = false
            json["message"] = e.message ?: ""
            ctx.json(json)
        }
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

    app.get("/del") { ctx ->
        ctx.json(BookDownloader.del(ctx.queryParam("name", "")!!))
    }

    app.get("/books") { ctx ->
        ctx.json(BookCollector.getMajorCollectBookList(ctx.queryParam("ratio", "70")!!.toDouble()))
    }

}