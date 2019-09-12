package com.jetbrains.tools.calculator.lexer

class RollbackLexer(private val lexer: Lexer) : Lexer {
    private var toReread: Token? = null

    override fun getToken(): Token = toReread?.let { token ->
        toReread = null
        token
    } ?: lexer.getToken()

    override fun lastToken(): Token = lexer.lastToken()

    override fun reset(text: String): Lexer {
        toReread = null
        return lexer.reset(text)
    }

    fun unread() {
        toReread = lexer.lastToken()
    }
}