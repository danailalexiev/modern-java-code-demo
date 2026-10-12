package com.example.accounts.checks;

import static com.google.errorprone.BugPattern.SeverityLevel.ERROR;

import com.google.auto.service.AutoService;
import com.google.errorprone.BugPattern;
import com.google.errorprone.VisitorState;
import com.google.errorprone.bugpatterns.BugChecker;
import com.google.errorprone.bugpatterns.BugChecker.SwitchExpressionTreeMatcher;
import com.google.errorprone.bugpatterns.BugChecker.SwitchTreeMatcher;
import com.google.errorprone.matchers.Description;
import com.google.errorprone.util.ASTHelpers;
import com.sun.source.tree.CaseTree;
import com.sun.source.tree.DefaultCaseLabelTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.SwitchExpressionTree;
import com.sun.source.tree.SwitchTree;
import com.sun.source.tree.Tree;
import com.sun.tools.javac.code.Flags;
import com.sun.tools.javac.code.Type;
import java.util.List;

@AutoService(BugChecker.class)
@BugPattern(summary = "default on a sealed switch hides new cases", severity = ERROR)
public final class NoDefaultOnSealedSwitch extends BugChecker
        implements SwitchExpressionTreeMatcher, SwitchTreeMatcher {

    @Override
    public Description matchSwitchExpression(SwitchExpressionTree t, VisitorState s) {
        return check(t.getExpression(), t.getCases(), t);
    }

    @Override
    public Description matchSwitch(SwitchTree t, VisitorState s) {
        return check(t.getExpression(), t.getCases(), t);
    }

    private Description check(ExpressionTree selector, List<? extends CaseTree> cases, Tree t) {
        Type type = ASTHelpers.getType(selector);
        boolean sealed = type != null && type.tsym != null && (type.tsym.flags() & Flags.SEALED) != 0;
        boolean hasDefault = cases.stream()
            .flatMap(c -> c.getLabels().stream())
            .anyMatch(l -> l instanceof DefaultCaseLabelTree);
        return sealed && hasDefault ? describeMatch(t) : Description.NO_MATCH;
    }
}
