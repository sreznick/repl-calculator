package com.jetbrains.tools.calculator.lexer

import org.junit.Test
import org.junit.Assert.*

class LexerTest {
    private fun tillEnd(lexer: Lexer): List<Token> {
        val result = mutableListOf<Token>()
        while (lexer.getToken().kind != TokenKind.END) {
            result.add(lexer.lastToken())
        }
        return result.toList()
    }

    private fun checkToken(expectedKind: TokenKind, expectedRepr: String, token: Token) {
        assertEquals(expectedKind, token.kind)
        assertEquals(expectedRepr, token.repr)
    }

    private fun checkNumber(expectedRepr: String, token: Token) {
        checkToken(TokenKind.NUMBER, expectedRepr, token)
    }

    private fun checkIdentifier(expectedRepr: String, token: Token) {
        checkToken(TokenKind.IDENTIFIER, expectedRepr, token)
    }

    private fun checkOperation(expectedRepr: String, token: Token) {
        checkToken(TokenKind.OPERATION, expectedRepr, token)
    }

    private fun checkEnd(token: Token) {
        checkToken(TokenKind.END, "", token)
    }

    @Test
    fun testEmptyInput() {
        checkEnd(DefaultLexer().getToken())
    }

    @Test
    fun testBlankInput() {
        checkEnd(DefaultLexer().reset("   ").getToken())
    }

    @Test
    fun testNumber() {
        listOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "42353265").forEach { decimal ->
            val lexer = DefaultLexer().reset(decimal)
            checkNumber(decimal, lexer.getToken())
            checkEnd(lexer.getToken())
        }
    }

    @Test
    fun testIdentifier() {
        listOf("a", "bc", "def123").forEach { name ->
            val lexer = DefaultLexer().reset(name)
            checkIdentifier(name, lexer.getToken())
            checkEnd(lexer.getToken())
        }
    }

    @Test
    fun testOperation() {
        listOf("+", "-", "/", "*", "**").forEach { op ->
            val lexer = DefaultLexer().reset(op)

            checkOperation(op, lexer.getToken())
            checkEnd(lexer.getToken())
        }
    }

    @Test
    fun testParens() {
        listOf("(", ")").zip(listOf(TokenKind.LEFT_PAREN, TokenKind.RIGHT_PAREN)).forEach { (op, tokenKind) ->
            val lexer = DefaultLexer().reset(op)

            checkToken(tokenKind, "", lexer.getToken())
            checkEnd(lexer.getToken())
        }
    }

    @Test
    fun testAssign() {
        val lexer = DefaultLexer().reset("=")

        checkToken(TokenKind.ASSIGN, "", lexer.getToken())
        checkEnd(lexer.getToken())
    }

    @Test
    fun testKeywords() {
        listOf("let").zip(listOf(TokenKind.KW_LET)).forEach { (repr, tokenKind) ->
            val lexer = DefaultLexer().reset(repr)

            checkToken(tokenKind, repr, lexer.getToken())
            checkEnd(lexer.getToken())
        }
    }

    @Test
    fun testExpr() {
        val tokens = tillEnd(DefaultLexer().reset("5+2"))
        assertEquals(3, tokens.size)
        checkNumber( "5", tokens[0])
        checkOperation( "+", tokens[1])
        checkNumber( "2", tokens[2])
    }

    @Test
    fun testExprWithParens() {
        val tokens = tillEnd(DefaultLexer().reset("(5 + 2)*12"))
        assertEquals(7, tokens.size)
        checkToken(TokenKind.LEFT_PAREN, "", tokens[0])
        checkNumber( "5", tokens[1])
        checkOperation( "+", tokens[2])
        assertEquals(Token(TokenKind.NUMBER, "2"), tokens[3])
        checkToken(TokenKind.RIGHT_PAREN, "", tokens[4])
        checkOperation( "*", tokens[5])
        assertEquals(Token(TokenKind.NUMBER, "12"), tokens[6])
    }

    @Test
    fun testExprWithVariable() {
        val tokens = tillEnd(DefaultLexer().reset("(5 + x)*12"))
        assertEquals(7, tokens.size)
        checkToken(TokenKind.LEFT_PAREN, "", tokens[0])
        checkNumber( "5", tokens[1])
        checkOperation( "+", tokens[2])
        assertEquals(Token(TokenKind.IDENTIFIER, "x"), tokens[3])
        checkToken(TokenKind.RIGHT_PAREN, "", tokens[4])
        checkOperation( "*", tokens[5])
        assertEquals(Token(TokenKind.NUMBER, "12"), tokens[6])
    }
}
