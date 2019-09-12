package com.jetbrains.tools.calculator

import com.jetbrains.tools.calculator.evaluator.Evaluator
import com.jetbrains.tools.calculator.parser.Parser

fun main() {
    val repl = REPL(Parser(), Evaluator(emptyMap()))
    repl.run()
}
