package com.jetbrains.tools.calculator.parser

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class ParserTest {
    private fun prepareParser(text: String) = Parser().reset(text)


    @Test
    fun testEmptyInput() {
        assertEquals(null, prepareParser("").parseLine())
    }

    @Test
    fun testBlankInput() {
        assertEquals(null, prepareParser("       ").parseLine())
    }

    private fun ParseTree.checkNumberLeaf(expectedRepr: String) {
        assertEquals(Operand.NUMBER, tag)
        assertEquals(expectedRepr, repr)
        assertEquals(emptyList<ParseTree>(), children)
    }

    private fun ParseTree.checkVariableLeaf(expectedRepr: String) {
        assertEquals(Operand.VARIABLE, tag)
        assertEquals(expectedRepr, repr)
        assertEquals(emptyList<ParseTree>(), children)
    }

    private fun ParseTree.checkBinaryOp(expected: BinaryOperation) {
        assertEquals(expected, tag)
        assertEquals("", repr)
        assertEquals(2, children.size)
    }

    private fun ParseTree.checkUnaryOp(expected: UnaryOperation) {
        assertEquals(expected, tag)
        assertEquals("", repr)
        assertEquals(1, children.size)
    }

    private fun ParseTree.checkAssign(expectedName: String) {
        assertEquals(Assign, tag)
        assertEquals("", repr)
        assertEquals(2, children.size)
        first().checkVariableLeaf(expectedName)
    }

    @Test
    fun testSingleConstant() {
        val repr = "12345"
        val parser = prepareParser(repr)
        val tree = parser.parseLine()
        assertNotNull(tree)
        tree?.let {
            tree.checkNumberLeaf(repr)
        }
    }

    @Test
    fun testSingleVariable() {
        val repr = "x12345"
        val parser = prepareParser(repr)
        val tree = parser.parseLine()
        assertNotNull(tree)
        tree?.let {
            tree.checkVariableLeaf(repr)
        }
    }

    @Test
    fun testSingleOp() {
        val parser = prepareParser("5+2")
        val tree = parser.parseLine()
        assertNotNull(tree)
        tree?.let {
            tree.checkBinaryOp(BinaryOperation.PLUS)
            tree.first().checkNumberLeaf("5")
            tree.second().checkNumberLeaf("2")
        }
    }

    @Test
    fun testDoubleOpSinglePrio() {
        val parser = prepareParser("5-2-4")
        val tree = parser.parseLine()
        assertNotNull(tree)
        tree?.let {
            tree.checkBinaryOp(BinaryOperation.MINUS)
            tree.first().checkBinaryOp(BinaryOperation.MINUS)
            tree.first().first().checkNumberLeaf("5")
            tree.first().second().checkNumberLeaf("2")
            tree.second().checkNumberLeaf("4")
        }
    }

    @Test
    fun testMultSub() {
        val parser = prepareParser("5*2-4")
        val tree = parser.parseLine()
        assertNotNull(tree)
        tree?.let {
            tree.checkBinaryOp(BinaryOperation.MINUS)
            tree.first().checkBinaryOp(BinaryOperation.MULT)
            tree.first().first().checkNumberLeaf("5")
            tree.first().second().checkNumberLeaf("2")
            tree.second().checkNumberLeaf("4")
        }
    }

    @Test
    fun testSubMult() {
        val parser = prepareParser("5-2*4")
        val tree = parser.parseLine()
        assertNotNull(tree)
        tree?.let {
            tree.checkBinaryOp(BinaryOperation.MINUS)
            tree.first().checkNumberLeaf("5")
            tree.second().checkBinaryOp(BinaryOperation.MULT)
            tree.second().first().checkNumberLeaf("2")
            tree.second().second().checkNumberLeaf("4")
        }
    }

    @Test
    fun testAddParen() {
        val parser = prepareParser("5+(2+4)")
        val tree = parser.parseLine()
        assertNotNull(tree)
        tree?.let {
            tree.checkBinaryOp(BinaryOperation.PLUS)
            tree.first().checkNumberLeaf("5")
            tree.second().checkBinaryOp(BinaryOperation.PLUS)
            tree.second().first().checkNumberLeaf("2")
            tree.second().second().checkNumberLeaf("4")
        }
    }

    @Test
    fun testDiffPrio() {
        val tree = prepareParser("5+2*3-4").parseLine()
        assertNotNull(tree)
        tree?.let {
            tree.checkBinaryOp(BinaryOperation.MINUS)
            tree.first().checkBinaryOp(BinaryOperation.PLUS)
            tree.second().checkNumberLeaf("4")
            tree.first().first().checkNumberLeaf("5")
            tree.first().second().checkBinaryOp(BinaryOperation.MULT)
            tree.first().second().first().checkNumberLeaf("2")
            tree.first().second().second().checkNumberLeaf("3")
        }
    }

    @Test
    fun testDiffPrio2() {
        val tree = prepareParser("5*2+3").parseLine()
        assertNotNull(tree)
        tree?.let {
            tree.checkBinaryOp(BinaryOperation.PLUS)
            tree.first().checkBinaryOp(BinaryOperation.MULT)
            tree.first().first().checkNumberLeaf("5")
            tree.first().second().checkNumberLeaf("2")
            tree.second().checkNumberLeaf("3")
        }
    }

    @Test
    fun testPower() {
        val tree = prepareParser("2**3").parseLine()
        assertNotNull(tree)
        tree?.let {
            tree.checkBinaryOp(BinaryOperation.POWER)
            tree.first().checkNumberLeaf("2")
            tree.second().checkNumberLeaf("3")
        }    }

    @Test
    fun testPower2() {
        val tree = prepareParser("2**3**4").parseLine()
        assertNotNull(tree)
        tree?.let {
            tree.checkBinaryOp(BinaryOperation.POWER)
            tree.first().checkNumberLeaf("2")
            tree.second().checkBinaryOp(BinaryOperation.POWER)
            tree.second().first().checkNumberLeaf("3")
            tree.second().second().checkNumberLeaf("4")
        }
    }

    @Test
    fun testPower2a() {
        val tree = prepareParser("2**(3**4)").parseLine()
        assertNotNull(tree)
        tree?.let {
            tree.checkBinaryOp(BinaryOperation.POWER)
            tree.first().checkNumberLeaf("2")
            tree.second().checkBinaryOp(BinaryOperation.POWER)
            tree.second().first().checkNumberLeaf("3")
            tree.second().second().checkNumberLeaf("4")
        }
    }

    @Test
    fun testPower3() {
        val tree = prepareParser("(2**3)**4").parseLine()
        assertNotNull(tree)
        tree?.let {
            tree.checkBinaryOp(BinaryOperation.POWER)
            tree.first().checkBinaryOp(BinaryOperation.POWER)
            tree.first().first().checkNumberLeaf("2")
            tree.first().second().checkNumberLeaf("3")
            tree.second().checkNumberLeaf("4")
        }
    }

    @Test
    fun testUnaryMinus() {
        val tree = prepareParser("-2").parseLine()
        assertNotNull(tree)
        tree?.let {
            tree.checkUnaryOp(UnaryOperation.MINUS)
            tree.first().checkNumberLeaf("2")
        }
    }

    @Test
    fun testUnaryMinus2() {
        val tree = prepareParser("-(-2)").parseLine()
        assertNotNull(tree)
        tree?.let {
            tree.checkUnaryOp(UnaryOperation.MINUS)
            tree.first().checkUnaryOp(UnaryOperation.MINUS)
            tree.first().first().checkNumberLeaf("2")
        }
    }

    @Test
    fun testUnaryMinus3() {
        val tree = prepareParser("-  -2 ").parseLine()
        assertNotNull(tree)
        tree?.let {
            tree.checkUnaryOp(UnaryOperation.MINUS)
            tree.first().checkUnaryOp(UnaryOperation.MINUS)
            tree.first().first().checkNumberLeaf("2")
        }
    }

    @Test
    fun testAssign() {
        val parser = prepareParser("let x = 5")
        val tree = parser.parseLine()
        assertNotNull(tree)
        tree?.let {
            tree.checkAssign("x")
            tree.second().checkNumberLeaf("5")
        }
    }
}