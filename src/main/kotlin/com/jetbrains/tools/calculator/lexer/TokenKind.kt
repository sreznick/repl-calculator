package com.jetbrains.tools.calculator.lexer

enum class TokenKind {
    END, NUMBER, IDENTIFIER, OPERATION, LEFT_PAREN, RIGHT_PAREN, ASSIGN,
    KW_LET
}