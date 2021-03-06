package owlery;

import java.util.List;

abstract class Expr {
  interface Visitor<R> {
    R visitDefineExpr(Define expr);
    R visitAssignExpr(Assign expr);
    R visitBooleanBinaryExpr(BooleanBinary expr);
    R visitBinaryExpr(Binary expr);
    R visitGroupingExpr(Grouping expr);
    R visitLiteralExpr(Literal expr);
    R visitUnaryExpr(Unary expr);
    R visitVariableExpr(Variable expr);
    R visitCallExpr(Call expr);
    R visitFunctionExpr(Function expr);
    R visitIndexExpr(Index expr);
    R visitTypeBinaryExpr(TypeBinary expr);
  }
  static class Define extends Expr {
    Define(Token name, Expr value, OType type) {
      this.name = name;
      this.value = value;
      this.type = type;
    }

    @Override
    <R> R accept(Visitor<R> visitor) {
      return visitor.visitDefineExpr(this);
    }

    final Token name;
    final Expr value;
    final OType type;
  }
  static class Assign extends Expr {
    Assign(Token name, Expr value) {
      this.name = name;
      this.value = value;
    }

    @Override
    <R> R accept(Visitor<R> visitor) {
      return visitor.visitAssignExpr(this);
    }

    final Token name;
    final Expr value;
  }
  static class BooleanBinary extends Expr {
    BooleanBinary(Expr left, Token operator, Expr right) {
      this.left = left;
      this.operator = operator;
      this.right = right;
    }

    @Override
    <R> R accept(Visitor<R> visitor) {
      return visitor.visitBooleanBinaryExpr(this);
    }

    final Expr left;
    final Token operator;
    final Expr right;
  }
  static class Binary extends Expr {
    Binary(Expr left, Token operator, Expr right) {
      this.left = left;
      this.operator = operator;
      this.right = right;
    }

    @Override
    <R> R accept(Visitor<R> visitor) {
      return visitor.visitBinaryExpr(this);
    }

    final Expr left;
    final Token operator;
    final Expr right;
  }
  static class Grouping extends Expr {
    Grouping(Expr expression) {
      this.expression = expression;
    }

    @Override
    <R> R accept(Visitor<R> visitor) {
      return visitor.visitGroupingExpr(this);
    }

    final Expr expression;
  }
  static class Literal extends Expr {
    Literal(Object value) {
      this.value = value;
    }

    @Override
    <R> R accept(Visitor<R> visitor) {
      return visitor.visitLiteralExpr(this);
    }

    final Object value;
  }
  static class Unary extends Expr {
    Unary(Token operator, Expr right) {
      this.operator = operator;
      this.right = right;
    }

    @Override
    <R> R accept(Visitor<R> visitor) {
      return visitor.visitUnaryExpr(this);
    }

    final Token operator;
    final Expr right;
  }
  static class Variable extends Expr {
    Variable(Token name) {
      this.name = name;
    }

    @Override
    <R> R accept(Visitor<R> visitor) {
      return visitor.visitVariableExpr(this);
    }

    final Token name;
  }
  static class Call extends Expr {
    Call(Expr callee, Token bang, List<Expr> arguments) {
      this.callee = callee;
      this.bang = bang;
      this.arguments = arguments;
    }

    @Override
    <R> R accept(Visitor<R> visitor) {
      return visitor.visitCallExpr(this);
    }

    final Expr callee;
    final Token bang;
    final List<Expr> arguments;
  }
  static class Function extends Expr {
    Function(List<Token> params, List<Stmt> body) {
      this.params = params;
      this.body = body;
    }

    @Override
    <R> R accept(Visitor<R> visitor) {
      return visitor.visitFunctionExpr(this);
    }

    final List<Token> params;
    final List<Stmt> body;
  }
  static class Index extends Expr {
    Index(Token name, Expr index) {
      this.name = name;
      this.index = index;
    }

    @Override
    <R> R accept(Visitor<R> visitor) {
      return visitor.visitIndexExpr(this);
    }

    final Token name;
    final Expr index;
  }
  static class TypeBinary extends Expr {
    TypeBinary(Expr expression, Token op, OType type) {
      this.expression = expression;
      this.op = op;
      this.type = type;
    }

    @Override
    <R> R accept(Visitor<R> visitor) {
      return visitor.visitTypeBinaryExpr(this);
    }

    final Expr expression;
    final Token op;
    final OType type;
  }

  abstract <R> R accept(Visitor<R> visitor);
}
