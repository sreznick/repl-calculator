package com.jetbrains.tools.calculator

import com.jetbrains.tools.calculator.evaluator.Evaluator
import com.jetbrains.tools.calculator.evaluator.EvaluatorException
import com.jetbrains.tools.calculator.lexer.LexerException
import com.jetbrains.tools.calculator.parser.Parser
import com.jetbrains.tools.calculator.parser.ParserException
import java.util.*

class REPL(private val parser: Parser, private val evaluator: Evaluator) {
    private val scanner = Scanner(System.`in`)

    private fun notifyUserProblem(e: Exception) {
        System.err.println("something went wrong...")
        System.err.println(e.message)
    }

    fun run() {
        while (true) {
            print(">>> ")
            try {
                val s = scanner.nextLine()

                if (s.trim() == ":exit") break

                parser.reset(s).parseLine()?.let { tree ->
                    evaluator.evaluateLine(tree)?.let { result ->
                        println(result)
                    }
                }
            } catch (e: EvaluatorException) {
                notifyUserProblem(e)
            } catch (e: ParserException) {
                notifyUserProblem(e)
            } catch (e: LexerException) {
                notifyUserProblem(e)
            } catch (e: Throwable) {
                System.err.println("It looks like some internal problem appeared. Please let developer know about it")
                System.err.println(e)
            }
        }
    }
}