package win.hupubao.beans

class PageInfo<T> {
    var total: Int = 0
    var current: Int = 1
    var size: Int = 50
    var pages: Int = 0
    var records: List<T> = mutableListOf()
}
