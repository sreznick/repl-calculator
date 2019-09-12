package com.jetbrains.tools.calculator.lexer

import com.jetbrains.tools.calculator.DefaultConfigurator

interface Lexer {
    fun getToken(): Token
    fun lastToken(): Token
    fun reset(text: String): Lexer
}

private fun Sequence<Char>.asString() = this.joinToString("")

class DefaultLexer(settings: LexerSettings = DefaultConfigurator.lexerSettings) : Lexer {
    private var charsSeq = "".asSequence()
    private var finished = false

    companion object {
        private val DecimalDigits = "0123456789".toSet()
        private const val SmallEnglishLetters = "abcdefghijklmnokprstuvwxyz"
        private val AlphaChars = (SmallEnglishLetters + SmallEnglishLetters.toUpperCase()).toSet()
        private val singleCharacterTokens = mapOf(
            '(' to TokenKind.LEFT_PAREN,
            ')' to TokenKind.RIGHT_PAREN,
            '=' to TokenKind.ASSIGN
        )
    }

    private val binaryOps = settings.binaryOperations().toSet()
    private val unaryOps = settings.unaryOperations().toSet()
    private val operationChars = (settings.binaryOperations() + settings.unaryOperations()).joinToString("").toSet()


    private fun skipBlanks() {
        charsSeq = charsSeq.dropWhile { it == ' ' }
    }

    private fun eatTail(allowed: Set<Char>): String {
        val head = charsSeq.take(1).asString()
        val tail = (charsSeq.drop(1).takeWhile { it in allowed }).asString()
        charsSeq = charsSeq.drop(tail.length + 1)
        return head + tail
    }

    private fun getNumber(): String  = eatTail(DecimalDigits)

    private fun getIdentifier(): String = eatTail(DecimalDigits + AlphaChars)

    private fun getOperation(): String = eatTail(operationChars)

    private var last: Token? = null

    override fun reset(text: String): Lexer {
        last = null
        charsSeq = text.asSequence()
        finished = false
        return this
    }

    override fun lastToken() = last ?: throw IllegalStateException()

    override fun getToken(): Token {
        if (finished) throw JunkAtEndException(charsSeq.joinToString(""))

        skipBlanks()

        val nextChar = charsSeq.take(1).joinToString("").firstOrNull()

        val result = when {
            nextChar == null -> {
                TokenKind.END
                finished = true
                Token(TokenKind.END, "")
            }
            nextChar in DecimalDigits -> {
                val result = Token(TokenKind.NUMBER, getNumber())
                val after = charsSeq.take(1).asString()
                if (after.isNotEmpty() && after[0] in AlphaChars) {
                    throw LexerException("number merged with identifier")
                }
                result
            }
            nextChar in AlphaChars -> {
                val repr = getIdentifier()
                if (repr == "let") {
                    Token(TokenKind.KW_LET, repr)
                } else {
                    Token(TokenKind.IDENTIFIER, repr)
                }
            }
            nextChar in operationChars -> {
                val repr = getOperation()
                if (repr in binaryOps + unaryOps) {
                    Token(TokenKind.OPERATION, repr)
                } else {
                    throw IllegalOperationException(repr)
                }
            }
            nextChar in singleCharacterTokens.keys -> {
                charsSeq = charsSeq.drop(1)
                Token(singleCharacterTokens[nextChar]!!)
            }
            else -> {
                throw UnexpectedCharException(charsSeq.joinToString(""))
            }
        }

        last = result

        return result
    }
}

open class LexerException(override val message: String) : Exception(message)
class JunkAtEndException(text: String) : LexerException("junk unexpeced stuff: '$text'")
class UnexpectedCharException(text: String) : LexerException("unexpeced input: '$text'")
class IllegalOperationException(text: String) : LexerException("illegal operation: '$text'")
