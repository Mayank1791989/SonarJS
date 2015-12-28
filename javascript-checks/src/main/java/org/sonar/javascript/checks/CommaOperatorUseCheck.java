/*
 * SonarQube JavaScript Plugin
 * Copyright (C) 2011-2016 SonarSource SA
 * mailto:contact AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.sonar.javascript.checks;

import java.util.LinkedList;
import java.util.List;
import javax.annotation.Nullable;
import org.sonar.api.server.rule.RulesDefinition;
import org.sonar.check.Priority;
import org.sonar.check.Rule;
import org.sonar.plugins.javascript.api.tree.Tree;
import org.sonar.plugins.javascript.api.tree.Tree.Kind;
import org.sonar.plugins.javascript.api.tree.expression.BinaryExpressionTree;
import org.sonar.plugins.javascript.api.tree.expression.ExpressionTree;
import org.sonar.plugins.javascript.api.tree.lexical.SyntaxToken;
import org.sonar.plugins.javascript.api.tree.statement.ForStatementTree;
import org.sonar.plugins.javascript.api.visitors.DoubleDispatchVisitorCheck;
import org.sonar.squidbridge.annotations.SqaleConstantRemediation;
import org.sonar.squidbridge.annotations.SqaleSubCharacteristic;

@Rule(
  key = "S878",
  name = "Comma operator should not be used",
  priority = Priority.MAJOR,
  tags = {Tags.MISRA})
@SqaleSubCharacteristic(RulesDefinition.SubCharacteristics.UNDERSTANDABILITY)
@SqaleConstantRemediation("5min")
public class CommaOperatorUseCheck extends DoubleDispatchVisitorCheck {

  private static final String MESSAGE_ONE_COMMA = "Remove use of this comma operator.";
  private static final String MESSAGE_MANY_COMMAS = "Remove use of all comma operators in this expression.";

  @Override
  public void visitBinaryExpression(BinaryExpressionTree tree) {

    if (!tree.is(Kind.COMMA_OPERATOR)) {
      super.visitBinaryExpression(tree);
      return;
    }

    List<ExpressionTree> expressions = getAllSubExpressions(tree);

    String message = expressions.size() > 2 ? MESSAGE_MANY_COMMAS : MESSAGE_ONE_COMMA;
    addLineIssue(getFirstComma(tree), message);

    for (ExpressionTree expression : expressions) {
      super.scan(expression);
    }
  }

  @Override
  public void visitForStatement(ForStatementTree tree) {
    visitPossibleException(tree.init());
    super.scan(tree.condition());
    visitPossibleException(tree.update());
    super.scan(tree.statement());
  }

  private void visitPossibleException(@Nullable Tree tree) {
    if (tree != null && tree.is(Kind.COMMA_OPERATOR)) {
      List<ExpressionTree> expressions = getAllSubExpressions((BinaryExpressionTree) tree);
      for (ExpressionTree expression : expressions) {
        super.scan(expression);
      }
    } else {
      super.scan(tree);
    }
  }

  private static List<ExpressionTree> getAllSubExpressions(BinaryExpressionTree tree) {
    List<ExpressionTree> result = new LinkedList<>();
    result.add(tree.rightOperand());
    ExpressionTree currentExpression = tree.leftOperand();
    while (currentExpression.is(Kind.COMMA_OPERATOR)) {
      result.add(((BinaryExpressionTree) currentExpression).rightOperand());
      currentExpression = ((BinaryExpressionTree) currentExpression).leftOperand();
    }
    result.add(currentExpression);
    return result;
  }

  private static SyntaxToken getFirstComma(BinaryExpressionTree tree) {
    SyntaxToken result = tree.operator();
    ExpressionTree currentExpression = tree.leftOperand();
    while (currentExpression.is(Kind.COMMA_OPERATOR)) {
      result = ((BinaryExpressionTree) currentExpression).operator();
      currentExpression = ((BinaryExpressionTree) currentExpression).leftOperand();
    }
    return result;
  }

}
