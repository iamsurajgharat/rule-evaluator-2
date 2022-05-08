grammar Rule;
expr    :   ID                                  # id
        |   NUM                                 # num
        |   TEXT                                # text
        |   expr opcode=('*'|'/') expr          # mulOrDiv
        |   expr opcode=('+'|'-') expr          # addOrSub
        |   '-' expr                            # negate
        |   func                                # callFunc
        |   '(' expr ')'                        # bracket
        ;
func    :   ID'(' params ')'                    # callFuncWithArgs
        |   ID'(' ')'                           # callFuncWithoutArgs
        ;
params  :   expr (',' expr )*
        ;
ID      :   [a-z][a-z0-9]+
        ;
NUM     :   [0-9]+
        ;
TEXT    :   '\''[.]*'\''
        ;
MUL     :   '*' ; 
DIV     :   '/' ;
ADD     :   '+' ;
SUB     :   '-' ;
WS      :   [\t]+ -> skip
        ;