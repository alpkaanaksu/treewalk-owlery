package owlery;

import java.util.List;

abstract class Stmt {
  interface Visitor<R> {
    R visitExpressionStmt(Expression stmt);
    R visitPrintStmt(Print stmt);
    R visitEmptyStmt(Empty stmt);
    R visitBlockStmt(Block stmt);
    R visitIfStmt(If stmt);
    R visitLoopConditionStmt(LoopCondition stmt);
    R visitLoopRangeStmt(LoopRange stmt);
    R visitLoopRangeInclStmt(LoopRangeIncl stmt);
  }
  static class Expression extends Stmt {
    Expression(Expr expression) {
      this.expression = expression;
    }

    @Override
    <R> R accept(Visitor<R> visitor) {
      return visitor.visitExpressionStmt(this);
    }

    final Expr expression;
  }
  static class Print extends Stmt {
    Print(Expr expression) {
      this.expression = expression;
    }

    @Override
    <R> R accept(Visitor<R> visitor) {
      return visitor.visitPrintStmt(this);
    }

    final Expr expression;
  }
  static class Empty extends Stmt {
    Empty(int i) {
      this.i = i;
    }

    @Override
    <R> R accept(Visitor<R> visitor) {
      return visitor.visitEmptyStmt(this);
    }

    final int i;
  }
  static class Block extends Stmt {
    Block(List<Stmt> statements) {
      this.statements = statements;
    }

    @Override
    <R> R accept(Visitor<R> visitor) {
      return visitor.visitBlockStmt(this);
    }

    final List<Stmt> statements;
  }
  static class If extends Stmt {
    If(Expr cond, Stmt thenBlock, Stmt elseBlock) {
      this.cond = cond;
      this.thenBlock = thenBlock;
      this.elseBlock = elseBlock;
    }

    @Override
    <R> R accept(Visitor<R> visitor) {
      return visitor.visitIfStmt(this);
    }

    final Expr cond;
    final Stmt thenBlock;
    final Stmt elseBlock;
  }
  static class LoopCondition extends Stmt {
    LoopCondition(Expr condition, Stmt body) {
      this.condition = condition;
      this.body = body;
    }

    @Override
    <R> R accept(Visitor<R> visitor) {
      return visitor.visitLoopConditionStmt(this);
    }

    final Expr condition;
    final Stmt body;
  }
  static class LoopRange extends Stmt {
    LoopRange(Expr from, Expr to, Stmt body) {
      this.from = from;
      this.to = to;
      this.body = body;
    }

    @Override
    <R> R accept(Visitor<R> visitor) {
      return visitor.visitLoopRangeStmt(this);
    }

    final Expr from;
    final Expr to;
    final Stmt body;
  }
  static class LoopRangeIncl extends Stmt {
    LoopRangeIncl(Expr from, Expr to, Stmt body) {
      this.from = from;
      this.to = to;
      this.body = body;
    }

    @Override
    <R> R accept(Visitor<R> visitor) {
      return visitor.visitLoopRangeInclStmt(this);
    }

    final Expr from;
    final Expr to;
    final Stmt body;
  }

  abstract <R> R accept(Visitor<R> visitor);
}
