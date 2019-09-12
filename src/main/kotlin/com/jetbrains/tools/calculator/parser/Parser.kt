package com.jetbrains.tools.calculator.parser

import com.jetbrains.tools.calculator.DefaultConfigurator
import com.jetbrains.tools.calculator.lexer.DefaultLexer
import com.jetbrains.tools.calculator.lexer.Lexer
import com.jetbrains.tools.calculator.lexer.RollbackLexer
import com.jetbrains.tools.calculator.lexer.TokenKind

class Parser(lexer: Lexer = DefaultLexer(), private val settings: ParserSettings = DefaultConfigurator.parserSettings) {
    private val rbLexer = RollbackLexer(lexer)

    private fun parseFactor(): ParseTree {
        val token = rbLexer.getToken()

        return when {
            token.kind == TokenKind.NUMBER -> ParseTree.leaf(Operand.NUMBER, token.repr)

            token.kind == TokenKind.IDENTIFIER -> ParseTree.leaf(Operand.VARIABLE, token.repr)

            token.kind == TokenKind.LEFT_PAREN -> {
                val parenTree = parsePrioLevel(0)
                if (rbLexer.getToken().kind != TokenKind.RIGHT_PAREN) {
                    throw ParserException("Closing parentheses expected")
                }
                parenTree
            }

            settings.unaryPriority(token.repr) >= 0 ->
                // унарная операция
                ParseTree.unary(
                    settings.unaryOperationOf(token.repr),
                    parsePrioLevel(settings.unaryPriority(token.repr) + 1)
                )

            else -> throw ParserException("Unexpected token: $token")
        }
    }

    private fun parsePrioLevel(priority: Int): ParseTree {
        if (priority == settings.numberOfPriorities()) {
            return parseFactor()
        }

        val left = parsePrioLevel(priority + 1)

        return if (settings.isRightAssociative(priority)) {
            val token = rbLexer.getToken()

            if (settings.isBinaryPriority(token.repr, priority)) {

                val right = parsePrioLevel(priority)

                ParseTree.binary(settings.binaryOperationOf(token.repr), left, right)
            } else {
                rbLexer.unread()
                left
            }
        } else {

            var root = left

            var token = rbLexer.getToken()

            while (settings.isBinaryPriority(token.repr, priority)) {
                root = ParseTree.binary(settings.binaryOperationOf(token.repr), root, parsePrioLevel(priority + 1))
                token = rbLexer.getToken()
            }

            rbLexer.unread()

            root
        }
    }

    private fun parseExpr(): ParseTree {
        return parsePrioLevel(0)
    }

    fun reset(text: String): Parser {
        rbLexer.reset(text)
        return this
    }

    fun parseLine(): ParseTree? {
        val token = rbLexer.getToken()
        val result = when (token.kind) {
            TokenKind.KW_LET -> {
                val variableToken = rbLexer.getToken()

                if (variableToken.kind != TokenKind.IDENTIFIER) throw ParserException("'let' keyword must be followed by variable name")

                val assignToken = rbLexer.getToken()

                if (assignToken.kind != TokenKind.ASSIGN) throw ParserException("assignment missing")

                ParseTree.binary(Assign, ParseTree.leaf(Operand.VARIABLE, variableToken.repr), parseExpr())
            }
            TokenKind.END -> {
                null
            }
            else -> {
                rbLexer.unread()
                parsePrioLevel(0)
            }
        }

        if (result != null) {
            val nextToken = rbLexer.getToken()
            if ( nextToken.kind != TokenKind.END) {
                throw ParserException("some junk after expression found: '${token.repr}'")
            }
        }

        return result
    }
}

open class ParserException(override val message: String) : Exception(message)
