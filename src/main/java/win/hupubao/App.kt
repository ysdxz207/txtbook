package win.hupubao

import io.javalin.Javalin
import io.javalin.staticfiles.Location


fun main(args: Array<String>) {
    val app = Javalin.create().apply {
        enableStaticFiles("static", Location.EXTERNAL)
    }.start(7000)


    app.get("/") { ctx ->
        ctx.render("/templates/index.html", mapOf("env" to "John", "dbUrl" to "Doe"))
    }
}