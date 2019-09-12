package com.jetbrains.tools.calculator.lexer

interface LexerSettings {
    fun binaryOperations(): List<String>
    fun unaryOperations(): List<String>
}

