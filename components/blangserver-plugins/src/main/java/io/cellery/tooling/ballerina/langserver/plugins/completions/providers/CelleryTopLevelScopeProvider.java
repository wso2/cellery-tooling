/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.cellery.tooling.ballerina.langserver.plugins.completions.providers;

import io.cellery.tooling.ballerina.langserver.plugins.Utils;
import io.cellery.tooling.ballerina.langserver.plugins.completions.SnippetGenerator;
import org.ballerinalang.annotation.JavaSPIService;
import org.ballerinalang.langserver.compiler.LSContext;
import org.ballerinalang.langserver.completions.CompletionKeys;
import org.ballerinalang.langserver.completions.providers.scopeproviders.TopLevelScopeProvider;
import org.eclipse.lsp4j.CompletionItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.ballerinalang.compiler.tree.BLangFunction;
import org.wso2.ballerinalang.compiler.tree.BLangNode;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;

import java.util.ArrayList;
import java.util.List;

/**
 * Top level Cellery Completions Provider.
 *
 * This deals with injecting Cellery specific completions in top level.
 * The completions are injected only if cellery had been imported.
 */
@JavaSPIService("org.ballerinalang.langserver.completions.spi.LSCompletionProvider")
public class CelleryTopLevelScopeProvider extends TopLevelScopeProvider {
    private static final Logger logger = LoggerFactory.getLogger(CelleryTopLevelScopeProvider.class);

    @Override
    public Precedence getPrecedence() {
        return Precedence.HIGH;
    }

    @Override
    public List<CompletionItem> getCompletions(LSContext context) {
        List<CompletionItem> completions = new ArrayList<>();

        try {
            if (Utils.hasCelleryImport(context)) {
                Boolean forcedRemoved = context.get(CompletionKeys.FORCE_REMOVED_STATEMENT_WITH_PARENTHESIS_KEY);
                // TODO: Have to check other relevant conditions
                if (forcedRemoved == null || !forcedRemoved) {
                    completions.addAll(this.getCellerySnippetCompletions(context));
                }
            }
        } catch (Exception e) {
            logger.error("Failed to add Statement context Cellery completions", e);
        }

        // Get statement context completions
        try {
            completions.addAll(super.getCompletions(context));
        } catch (Exception e) {
            logger.error("Failed to add Statement context Ballerina lang completions", e);
        }
        return completions;
    }

    /**
     * Get cellery specific snippet completion items.
     *
     * @param context Language Server Context
     * @return {@link List<CompletionItem>} List of calculated Completion Items
     */
    private List<CompletionItem> getCellerySnippetCompletions(LSContext context) {
        List<CompletionItem> completions = new ArrayList<>();
        BLangNode scopeNode = context.get(CompletionKeys.SCOPE_NODE_KEY);
        if (scopeNode instanceof BLangPackage) {
            boolean buildFunctionAvailable = false;
            boolean runFunctionAvailable = false;
            BLangPackage bLangPackage = (BLangPackage) scopeNode;
            List<BLangFunction> bLangFunctions = bLangPackage.getFunctions();

            for (BLangFunction bLangFunction : bLangFunctions) {
                if (bLangFunction.name.value.equalsIgnoreCase("build")) {
                    buildFunctionAvailable = true;
                } else if (bLangFunction.name.value.equalsIgnoreCase("run")) {
                    runFunctionAvailable = true;
                }
            }
            if (!buildFunctionAvailable) {
                completions.add(SnippetGenerator.getCelleryBuildMethodSnippet().build(context));
            }
            if (!runFunctionAvailable) {
                completions.add(SnippetGenerator.getCelleryRunMethodSnippet().build(context));
            }
        }
        return completions;
    }
}
