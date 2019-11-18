import java.awt.Toolkit
import java.awt.datatransfer.StringSelection
import java.io.File
import java.lang.IllegalArgumentException
import java.lang.IllegalStateException


var rows : Int = 0
var cols : Int = 0

fun main(args: Array<String>) {
    //val inputs = File("level3").listFiles().sorted()
    val inputs = listOf(File("level3/level3-5.in"))
    var first = true
    inputs.forEach {file ->
        if (!first) {
            readLine()
        } else {
            first = false
        }
        file.forEachLine {
            println("$file: \nin $it")
            val result = processLine(it)
            println("out:\n${result}")
            val stringSelection = StringSelection(result)
            val clipboard = Toolkit.getDefaultToolkit().systemClipboard
            clipboard.setContents(stringSelection) { _, _ -> }
        }
    }

}

data class Color(val c: Int, val initial: Boolean)

class Board {
    val content = mutableMapOf<Point, Color>()

}

data class Point(val row: Int, val col: Int) {

    init {
        require(row in 0 until rows && col in 0  until cols) { "$row $col [$rows $cols] " }
    }

    override fun toString(): String {
        return "$row, $col [${row * cols + col + 1}]"
    }

    companion object {
        fun of(pos: Int) = Point((pos-1) / cols, (pos-1) % cols)

    }
}

fun Int.toPoint() = Point.of(this)
fun String.toPoint() = Point.of(this.toInt())

fun processLine(it: String): String {
    val input = it.split(' ')
    rows = input[0].toInt()
    cols = input[1].toInt()
    val count = input[2].toInt()
    val board = Board()

    val points = input.subList(3, 3 + count * 2).map {it.toInt()}.chunked(2).forEach {(pos, col) ->
        board.content[Point.of(pos)] = Color(col, true)
    }

    val path = input.subList(3+count*2, input.size)

    val npaths = path[0].toInt()
    assert(npaths == 1)
    val pathCol = path[1].toInt()
    val initPathPos = path[2].toPoint()
    var pathPos = initPathPos
    val pathLen = path[3].toInt()
    val commands = path.subList(4, 4 + pathLen)

    assert(board.content[pathPos] == Color(pathCol, true))

    var fail = 0

    val result = try {
        commands.forEachIndexed { index, cmd ->
            fail = index + 1
            pathPos = when (cmd) {
                "E" ->  pathPos.copy(col = pathPos.col + 1)
                "W" ->  pathPos.copy(col = pathPos.col - 1)
                "N" -> pathPos.copy(row = pathPos.row - 1)
                "S" -> pathPos.copy(row = pathPos.row + 1)
                else -> throw IllegalStateException("wrong cmd '$cmd' at index $index")
            }

            if (fail == commands.size) {
                if (board.content[pathPos] != Color(pathCol, true) || pathPos == initPathPos) {
                    throw IllegalArgumentException()
                }
            }
            else {
                val cont = board.content[pathPos]
                require(cont == null || (cont.initial && cont.c == pathCol)) { "there is already color: ${board.content[pathPos]} at $pathPos" }
            }
            board.content[pathPos] = Color(pathCol, false)
        }
        1
    } catch(ex: IllegalArgumentException) {
        println(ex)
        -1
    }

    return "$result $fail"
}
