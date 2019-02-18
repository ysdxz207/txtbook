package win.hupubao.beans

class History {
    var name: String = ""
    var progress: Double = 0.toDouble()

    constructor(name: String, progress: Double) {
        this.name = name
        this.progress = progress
    }
}
