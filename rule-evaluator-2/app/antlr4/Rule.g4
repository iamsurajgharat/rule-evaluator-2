grammar Rule;
expr    :   BOOL                                                # bool
        |   NUM                                                 # num
        |   TEXT                                                # text
        |   ID                                                  # id
        |   expr opcode=('*'|'/'|'%') expr                      # mulOrDiv
        |   expr opcode=('+'|'-') expr                          # addOrSub
        |   expr opcode=('>'|'<'|'<='|'>=') expr                # comparison
        |   '-' expr                                            # negate
        |   func                                                # callFunc
        |   '(' expr ')'                                        # bracket
        ;
func    :   ID'(' params ')'                                    # callFuncWithArgs
        |   ID'(' ')'                                           # callFuncWithoutArgs
        ;
params  :   expr (',' expr )*
        ;
BOOL    :   'true'
        |   'false'
        ;
TEXT    :   '\''.*?'\''
        ;
ID      :   [a-zA-Z][a-zA-Z0-9]*
        ;
NUM     :   [0-9]+
        ;
MUL     :   '*' ; 
DIV     :   '/' ;
ADD     :   '+' ;
SUB     :   '-' ;
LT      :   '<' ;
GT      :   '>' ;
WS      :   [ \t\r\n]+ -> skip
        ;