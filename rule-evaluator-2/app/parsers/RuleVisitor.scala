package io.github.iamsurajgharat
package ruleevaluator
package parsers

import io.github.iamsurajgharat.expressiontree.SExpression
import io.github.iamsurajgharat.expressiontree.expressiontree.DataType
import io.github.iamsurajgharat.expressiontree.SExpOpType
import io.github.iamsurajgharat.ruleevaluator.antlr4.RuleParser
import io.github.iamsurajgharat.ruleevaluator.antlr4.RuleParser._
import io.github.iamsurajgharat.ruleevaluator.antlr4.RuleBaseVisitor
import scala.util.Success
import scala.util.Failure
import io.github.iamsurajgharat.ruleevaluator.models.web.RuleMetadata

class RuleVisitor(val metadata : RuleMetadata) extends RuleBaseVisitor[SExpression]{

    val dataTypes = metadata.dataTypes
    override def visitNum(ctx: RuleParser.NumContext): SExpression = 
        SExpression.constant(ctx.getText().toFloat)

    override def visitId(ctx: RuleParser.IdContext): SExpression = {
        val id = ctx.getText()
        SExpression.variable(id, dataTypes(id))
    }

    override def visitText(x: RuleParser.TextContext): SExpression = {
        SExpression.constant(x.getText())
    }

    // Binary operations
    override def visitMulOrDiv(ctx: RuleParser.MulOrDivContext): SExpression = {
        val e1 = visit(ctx.expr(0))
        val e2 = visit(ctx.expr(1))
        createBinaryOp(e1,e2,ctx.opcode.getType())
    }

    override def visitAddOrSub(ctx: RuleParser.AddOrSubContext): SExpression = {
        val e1 = visit(ctx.expr(0))
        val e2 = visit(ctx.expr(1))
        createBinaryOp(e1,e2,ctx.opcode.getType())
    }

    override def visitComparison(ctx: ComparisonContext): SExpression = {
        val e1 = visit(ctx.expr(0))
        val e2 = visit(ctx.expr(1))
        createBinaryOp(e1,e2,ctx.opcode.getType())
    }

    // func call
    override def visitCallFuncWithArgs(ctx: RuleParser.CallFuncWithArgsContext): SExpression = {
        val attemp1 = ctx.ID().getText() match {
            case "STARTSWITH" => 
                val args = getParams(ctx.params())
                SExpression.createFuncStartsWith(args(0), args(1))
        }

        attemp1 match {
            case Failure(exception) => throw exception
            case Success(value) => value
        }

    }

    private def getParams(ctx:RuleParser.ParamsContext):List[SExpression] = {
        import scala.collection.JavaConverters._
        val (a1::an) = ctx.expr().asScala.toList
        val e1 = visit(a1)
        if(an.isEmpty) 
            List(e1) 
        else 
            e1 :: an.map(visit(_)).toList
    }
    
    private def createBinaryOp(e1:SExpression, e2:SExpression, opcode:Int) : SExpression = {
        val opr = opcode match {
            case RuleParser.MUL => SExpOpType.MultiplyOpr
            case RuleParser.DIV => SExpOpType.DivideOpr
            case RuleParser.ADD => SExpOpType.AddOpr
            case RuleParser.SUB => SExpOpType.SubtractOpr
            case RuleParser.LT  => SExpOpType.LtOpr
            case RuleParser.GT  => SExpOpType.GtOpr
        }
        SExpression.operation(opr, e1, e2)
    }
}