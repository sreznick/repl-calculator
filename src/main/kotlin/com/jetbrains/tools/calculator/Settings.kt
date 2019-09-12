package com.jetbrains.tools.calculator

import com.jetbrains.tools.calculator.lexer.LexerSettings
import com.jetbrains.tools.calculator.parser.BinaryOperation
import com.jetbrains.tools.calculator.parser.ParserSettings
import com.jetbrains.tools.calculator.parser.UnaryOperation

enum class Associativity {
    LEFT, RIGHT
}

class Configurator {
    private val binaryOperationsByPriority = mutableListOf<Set<BinaryOperation>>()
    private val associativityByPriority = mutableListOf<Associativity>()
    private var currentUnaryPriority = 0
    private val unaryPriorities = mutableMapOf<UnaryOperation, Int>()

    private val binaryMnemonics = mutableMapOf<BinaryOperation, String>()
    private val unaryMnemonics = mutableMapOf<UnaryOperation, String>()

    private val binaryByRepr = mutableMapOf<String, BinaryOperation>()
    private val unaryByRepr = mutableMapOf<String, UnaryOperation>()

    fun leftAssoc(operations: List<BinaryOperation>) {
        binaryOperationsByPriority.add(operations.toSet())
        associativityByPriority.add(Associativity.LEFT)
    }

    fun rightAssoc(operations: List<BinaryOperation>) {
        binaryOperationsByPriority.add(operations.toSet())
        associativityByPriority.add(Associativity.RIGHT)
    }

    fun unary(operations: List<UnaryOperation>) {
        operations.forEach {
            unaryPriorities[it] = currentUnaryPriority
        }
        currentUnaryPriority += 1
    }

    fun repr(operation: UnaryOperation, repr: String) {
        unaryMnemonics[operation] = repr
        unaryByRepr[repr] = operation
    }

    fun repr(operation: BinaryOperation, repr: String) {
        binaryMnemonics[operation] = repr
        binaryByRepr[repr] = operation
    }

    private class ParserSettingImpl(private val binaryByPriority: List<Set<BinaryOperation>>,
                                    private val binaryByRepr: Map<String, BinaryOperation>,
                                    private val associativityByPriority: List<Associativity>,
                                    private val unaryByRepr: Map<String, UnaryOperation>,
                                    private val unaryPriorities: Map<UnaryOperation, Int>) : ParserSettings {

        override fun numberOfPriorities(): Int = binaryByPriority.size

        override fun binaryOperationOf(repr: String): BinaryOperation =
            binaryByRepr.get(repr) ?: throw InconsistentSettings("no binary operation for representation $repr")

        override fun isBinaryPriority(repr: String, priority: Int): Boolean {
            return  binaryByRepr.get(repr) != null && binaryOperationOf(repr) in binaryByPriority[priority]
        }

        override fun isRightAssociative(priority: Int): Boolean = associativityByPriority[priority] == Associativity.RIGHT

        override fun unaryPriority(repr: String): Int {
            return unaryByRepr[repr]?.let { unary ->
                unaryPriorities[unary] ?: throw InconsistentSettings("No priority for $repr that is specified as unary operation")
            } ?: -1
        }

        override fun unaryOperationOf(repr: String): UnaryOperation =
            unaryByRepr[repr] ?: throw InconsistentSettings("no unary operation for representation $repr")
    }

    private class LexerSettingImpl(private val binaryOperations: List<String>,
                                   private val unaryOperations: List<String>) : LexerSettings {
        override fun binaryOperations(): List<String> = binaryOperations
        override fun unaryOperations(): List<String> = unaryOperations
    }


    fun buildParserSettings(): ParserSettings {
        return ParserSettingImpl(binaryOperationsByPriority, binaryByRepr, associativityByPriority, unaryByRepr, unaryPriorities)
    }

    fun buildLexerSettings(): LexerSettings {
        return LexerSettingImpl(
            binaryOperationsByPriority.flatten().map {
                binaryMnemonics.get(it) ?: throw InconsistentSettings("No representation for binary operation $it")
            },
            unaryPriorities.keys.map {
                unaryMnemonics.get(it) ?: throw InconsistentSettings("No representation for unary operation $it")
            }
        )
    }
}

object DefaultConfigurator {
    private val configurator = Configurator()

    init {
        configurator.leftAssoc(listOf(BinaryOperation.PLUS, BinaryOperation.MINUS))
        configurator.leftAssoc(listOf(BinaryOperation.MULT, BinaryOperation.DIV))
        configurator.rightAssoc(listOf(BinaryOperation.POWER))
        configurator.unary(listOf(UnaryOperation.PLUS, UnaryOperation.MINUS))

        configurator.repr(BinaryOperation.PLUS, "+")
        configurator.repr(BinaryOperation.MINUS, "-")
        configurator.repr(BinaryOperation.MULT, "*")
        configurator.repr(BinaryOperation.DIV, "/")
        configurator.repr(BinaryOperation.POWER, "**")

        configurator.repr(UnaryOperation.PLUS, "+")
        configurator.repr(UnaryOperation.MINUS, "-")
    }

    val parserSettings = configurator.buildParserSettings()
    var lexerSettings = configurator.buildLexerSettings()
}

class InconsistentSettings(message: String) : Exception("something wrong with settings: $message")
