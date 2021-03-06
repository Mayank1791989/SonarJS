/*
 * SonarQube JavaScript Plugin
 * Copyright (C) 2011-2020 SonarSource SA
 * mailto:info AT sonarsource DOT com
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

import com.google.common.collect.ImmutableSet;
import java.util.Set;
import java.util.regex.Pattern;
import org.sonar.check.Rule;
import org.sonar.check.RuleProperty;
import org.sonar.javascript.checks.annotations.JavaScriptRule;
import org.sonar.plugins.javascript.api.tree.Tree;
import org.sonar.plugins.javascript.api.tree.lexical.SyntaxToken;
import org.sonar.plugins.javascript.api.tree.lexical.SyntaxTrivia;
import org.sonar.plugins.javascript.api.visitors.SubscriptionVisitorCheck;
import org.sonarsource.analyzer.commons.annotations.DeprecatedRuleKey;

@JavaScriptRule
@Rule(key = "S139")
@DeprecatedRuleKey(ruleKey = "TrailingComment")
public class TrailingCommentCheck extends SubscriptionVisitorCheck {

  private static final String MESSAGE = "Move this trailing comment on the previous empty line.";

  private static final String DEFAULT_LEGAL_COMMENT_PATTERN = "^//\\s*+[^\\s]++$";

  @RuleProperty(
    key = "legalCommentPattern",
    description = "Pattern for text of trailing comments that are allowed.",
    defaultValue = DEFAULT_LEGAL_COMMENT_PATTERN)
  private String legalCommentPattern = DEFAULT_LEGAL_COMMENT_PATTERN;

  private Pattern pattern;
  private int previousTokenLine;

  @Override
  public Set<Tree.Kind> nodesToVisit() {
    return ImmutableSet.of(Tree.Kind.TOKEN);
  }

  @Override
  public void visitFile(Tree tree) {
    previousTokenLine = -1;
    pattern = Pattern.compile(legalCommentPattern);
  }

  @Override
  public void visitNode(Tree tree) {
    SyntaxToken token = (SyntaxToken) tree;
    for (SyntaxTrivia trivia : token.trivias()) {
      if (trivia.line() == previousTokenLine) {
        String comment = trivia.text();
        if (comment.startsWith("//") && !pattern.matcher(comment).matches()) {
          addIssue(trivia, MESSAGE);
        }
      }
    }
    previousTokenLine = token.line();
  }

  public void setLegalCommentPattern(String pattern) {
    this.legalCommentPattern = pattern;
  }

}
