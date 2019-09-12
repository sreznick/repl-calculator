package com.jetbrains.tools.calculator.evaluator

import com.jetbrains.tools.calculator.lexer.DefaultLexer
import com.jetbrains.tools.calculator.parser.Parser
import org.junit.Test
import org.junit.Assert.assertEquals

class ParserTest {
    private fun prepareParser(text: String) = Parser().reset(text)

    @Test
    fun testSingleValue() {
        val parser = prepareParser("1234")
        val tree = parser.parseLine()
        val evaluator = Evaluator(emptyMap())
        tree?.let {
            assertEquals(1234.toBigInteger(), evaluator.evaluateLine(it))
        }
    }

    @Test
    fun testSimpleExpr() {
        val parser = prepareParser("2+3")
        val tree = parser.parseLine()
        val evaluator = Evaluator(emptyMap())
        tree?.let {
            assertEquals(5.toBigInteger(), evaluator.evaluateLine(it))
        }
    }

    @Test
    fun testLeftAssoc() {
        val parser = prepareParser("15-5-2")
        val tree = parser.parseLine()
        val evaluator = Evaluator(emptyMap())
        tree?.let {
            assertEquals(8.toBigInteger(), evaluator.evaluateLine(it))
        }
    }

    @Test
    fun testRightAssoc() {
        val parser = prepareParser("2**2**3")
        val tree = parser.parseLine()
        val evaluator = Evaluator(emptyMap())
        tree?.let {
            assertEquals(256.toBigInteger(), evaluator.evaluateLine(it))
        }
    }

    @Test
    fun testPrio() {
        val parser = prepareParser("2+3*4")
        val tree = parser.parseLine()
        val evaluator = Evaluator(emptyMap())
        tree?.let {
            assertEquals(14.toBigInteger(), evaluator.evaluateLine(it))
        }
    }

    @Test
    fun testParen() {
        val parser = prepareParser("(2+3)*4")
        val tree = parser.parseLine()
        val evaluator = Evaluator(emptyMap())
        tree?.let {
            assertEquals(20.toBigInteger(), evaluator.evaluateLine(it))
        }
    }
}