package org.eclipse.sparkplug.tck.message;

import com.hivemq.extension.sdk.api.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class TestResult {

    private boolean isCorrect = true;
    private final @NotNull List<String> issues = new ArrayList<>();


    public void addIssue(final @NotNull String issue) {
        isCorrect = false;
        issues.add(issue);
    }

    public void addSubResult(final @NotNull TestResult subResult) {
        isCorrect = isCorrect && subResult.isCorrect;
        issues.addAll(subResult.getIssues());
    }


    public boolean isCorrect() {
        return isCorrect;
    }

    public @NotNull List<String> getIssues() {
        return issues;
    }
}
