package com.jetbrains.tools.calculator.parser

interface NodeTag

enum class BinaryOperation : NodeTag {
    PLUS, MINUS, MULT, DIV, POWER
}

enum class UnaryOperation : NodeTag {
    MINUS, PLUS
}

enum class Operand : NodeTag {
    NUMBER, VARIABLE
}

object Assign: NodeTag

data class ParseTree(val tag: NodeTag, val repr: String, val children: List<ParseTree>) {
    companion object {
        fun leaf(tag: NodeTag, repr: String = "") = ParseTree(tag, repr, emptyList())
        fun unary(tag: NodeTag, child: ParseTree) = ParseTree(tag, "", listOf(child))
        fun binary(tag: NodeTag, left: ParseTree, right: ParseTree) = ParseTree(tag, "", listOf(left, right))
    }

    fun first(): ParseTree = children.firstOrNull() ?: throw AbsentFirstChildException(this)
    fun second(): ParseTree = children.getOrNull(1) ?: throw AbsentSecondChildException(this)
}

open class InconsistentParseTreeException(message: String) : Exception(message)
open class AbsentChildException(node: ParseTree, childRef: String) :
    InconsistentParseTreeException("No $childRef child for node with tag ${node.tag} " +
            "and representation '${node.repr}'")
class AbsentFirstChildException(node: ParseTree) : AbsentChildException(node, "first")
class AbsentSecondChildException(node: ParseTree) : AbsentChildException(node, "second")
