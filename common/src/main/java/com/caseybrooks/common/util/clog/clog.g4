grammar clog;

// Parser Rules

replace
    :
    R_START
        LIT_INDEX ( formatter )*
    R_END
    ;

formatter
    :
    PIPE IDENTIFIER ( LPAREN ( formatter_param_list )? RPAREN )?
    ;

formatter_param_list
    :
    formatter_param (COMMA formatter_param)*
    ;

formatter_param
    :
    LIT_INDEX | LIT_STRING | LIT_NUMBER | LIT_BOOLEAN
    ;

// Lexer Rules

// literals for use as formatter params
LIT_INDEX               : DOLLARSIGN DIGIT+ ;
LIT_STRING              : SINGLEQUOTE.*SINGLEQUOTE ;
LIT_NUMBER              : DIGIT+ (DOT DIGIT+)?;
LIT_BOOLEAN             : 'true' | 'false';

IDENTIFIER: LETTER (LETTER | DIGIT | '_')*;

// language-important characters
fragment DOLLARSIGN     : '$';
fragment SINGLEQUOTE    : '\'';
COMMA                   : ',';
DOT                     : '.';
PIPE                    : '|';
LPAREN                  : '(';
RPAREN                  : ')';
R_START                 : '{{' ;
R_END                   : '}}' ;

// basic character types
fragment DIGIT          : '0' | [1-9];
fragment LETTER         : [a-zA-Z];

// whitespace is optional
WS: [ \t\r\n\u000C]+ -> skip;

