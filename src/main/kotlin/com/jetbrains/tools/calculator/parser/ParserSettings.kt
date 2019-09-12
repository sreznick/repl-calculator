package com.jetbrains.tools.calculator.parser

interface ParserSettings {
    fun numberOfPriorities(): Int
    fun binaryOperationOf(repr: String): BinaryOperation
    fun isBinaryPriority(repr: String, priority: Int): Boolean
    fun isRightAssociative(priority: Int): Boolean

    fun unaryOperationOf(repr: String): UnaryOperation
    fun unaryPriority(repr: String): Int
}
