grammar Rule;
expr        :   TEXT                                        # textExpr
            |   NUM                                         # numExpr
            |   ('true' | 'false')                          # boolExpr
            |   ID                                          # varExpr
            |   expr opcode=('*' | '/' | '%') expr          # mulOrDivideOprExpr
            |   expr opcode=('+' | '-') expr                # addOrSubOprExpr
            |   ID'(' params? ')'                           # callFuncExpr
            ;
params      :   expr (',' expr)*
            ;
NUM         :   DIGIT+
            |   DIGIT* '.' DIGIT+ 
            ;
ID          :   [a-zA-Z][a-zA-Z0-9]*
            ;
TEXT        :   '\'' ((ESC|.)*?) '\'' ;
ADD         :   '+' ;
SUB         :   '-' ;
MUL         :   '*' ;
DIVIDE      :   '/' ;
MODULO      :   '%' ;

fragment
DIGIT       :   [0-9] ;

fragment
ESC         :   '\\\''
            |   '\\\\'
            ;

WS          :   [ \t]+ -> skip ; // toss out whitespace