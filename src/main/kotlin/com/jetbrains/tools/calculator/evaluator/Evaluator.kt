package com.jetbrains.tools.calculator.evaluator

import com.jetbrains.tools.calculator.parser.*
import java.lang.ArithmeticException
import java.math.BigInteger

class Evaluator(initialState: Map<String, BigInteger>) {
    private val state = initialState.toMutableMap()

    private fun evaluateExpr(tree: ParseTree): BigInteger {
        return when (tree.tag) {
            Operand.NUMBER -> BigInteger(tree.repr)
            Operand.VARIABLE -> state[tree.repr] ?: throw UndefinedVariableException(tree.repr)
            UnaryOperation.MINUS -> -evaluateExpr(tree.first())
            UnaryOperation.PLUS -> evaluateExpr(tree.first())
            BinaryOperation.MINUS -> evaluateExpr(tree.first()) - evaluateExpr(tree.second())
            BinaryOperation.PLUS -> evaluateExpr(tree.first()) + evaluateExpr(tree.second())
            BinaryOperation.MULT -> evaluateExpr(tree.first()) * evaluateExpr(tree.second())
            BinaryOperation.DIV -> evaluateExpr(tree.first()) / evaluateExpr(tree.second())
            BinaryOperation.POWER -> evaluateExpr(tree.first()).pow(evaluateExpr(tree.second()).toInt())
            else -> throw EvaluatorException("Not supported operation: ${tree.tag}")
        }
    }

    fun evaluateLine(tree: ParseTree): BigInteger? {
        try {
            return when (tree.tag) {
                Assign -> {state[tree.first().repr] = evaluateExpr(tree.second())
                    null
                }
                else -> evaluateExpr(tree)
            }
        } catch (e: ArithmeticException) {
            throw ArithmeticEvaluatorException(e)
        }
    }
}

open class EvaluatorException(message: String, cause: Throwable? = null) : Exception("Execution problem: $message", cause)
class UndefinedVariableException(name: String) : EvaluatorException("undefined variable $name")
class ArithmeticEvaluatorException(cause: ArithmeticException) : EvaluatorException("arithmetic exception happened", cause)
