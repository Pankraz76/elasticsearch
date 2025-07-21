/*
 * Copyright Elasticsearch B.V. and/or licensed to Elasticsearch B.V. under one
 * or more contributor license agreements. Licensed under the "Elastic License
 * 2.0", the "GNU Affero General Public License v3.0 only", and the "Server Side
 * Public License v 1"; you may not use this file except in compliance with, at
 * your election, the "Elastic License 2.0", the "GNU Affero General Public
 * License v3.0 only", or the "Server Side Public License, v 1".
 */

package org.elasticsearch.gradle.internal.conventions.precommit;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.openrewrite.gradle.RewriteExtension;

import static java.lang.Boolean.parseBoolean;
import static java.lang.System.getenv;

/**
 * This plugin configures formatting for Java source using Spotless
 * for Gradle. Since the act of formatting existing source can interfere
 * with developers' workflows, we don't automatically format all code
 * (yet). Instead, we maintain a list of projects that are excluded from
 * formatting, until we reach a point where we can comfortably format them
 * in one go without too much disruption.
 *
 * <p>Any new sub-projects must not be added to the exclusions list!
 *
 * <p>To perform a reformat, run:
 *
 * <pre>    ./gradlew rewriteRun spotlessApply</pre>
 *
 * <p>To check the current format, run:
 *
 * <pre>    ./gradlew rewriteDryRun spotlessJavaCheck</pre>
 *
 * <p>This is also carried out by the `precommit` task.
 *
 * <p>See also the <a href="https://github.com/diffplug/spotless/tree/master/plugin-gradle"
 * >Spotless project page</a>.
 */
public class CheckConventionsPlugin implements Plugin<Project> {

    private static final boolean IS_CI = parseBoolean(getenv("isCI"));
    private static final boolean CODE_CLEANUP = parseBoolean(getenv("codeCleanup"));

    @SuppressWarnings("checkstyle:DescendantToken")
    @Override
    public void apply(Project project) {
        RewriteExtension rewriteExtension = project.getExtensions().getByType(RewriteExtension.class);
        rewriteExtension.setFailOnDryRunResults(true);
        rewriteExtension.exclusion(
            "**OpenSearchTestCaseTests.java"
        );
        rewriteExtension.activeRecipe(
            "org.openrewrite.java.RemoveUnusedImports",
//            "org.openrewrite.staticanalysis.RemoveUnusedLocalVariables",
//            "org.openrewrite.staticanalysis.RemoveUnusedPrivateFields",
            "org.openrewrite.staticanalysis.RemoveUnusedPrivateMethods"
        );
        project.getTasks().named("check").configure(check -> check.dependsOn("rewriteDryRun"));
        if (!IS_CI && CODE_CLEANUP) {
            project.getTasks().named("assemble").configure(check -> check.dependsOn("rewriteRun"));
        }
    }
}
