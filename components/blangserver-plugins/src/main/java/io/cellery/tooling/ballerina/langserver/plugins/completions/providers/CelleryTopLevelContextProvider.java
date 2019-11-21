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
import io.cellery.tooling.ballerina.langserver.plugins.completions.CompletionUtils;
import io.cellery.tooling.ballerina.langserver.plugins.completions.SnippetGenerator;
import org.ballerinalang.annotation.JavaSPIService;
import org.ballerinalang.langserver.compiler.LSContext;
import org.ballerinalang.langserver.completions.CompletionKeys;
import org.ballerinalang.langserver.completions.providers.scopeproviders.TopLevelScopeProvider;
import org.eclipse.lsp4j.CompletionItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Top level Cellery Completions Provider.
 *
 * This deals with injecting Cellery specific completions in top level.
 * The completions are injected only if cellery had been imported.
 */
@JavaSPIService("org.ballerinalang.langserver.completions.spi.LSCompletionProvider")
public class CelleryTopLevelContextProvider extends TopLevelScopeProvider {
    private static final Logger logger = LoggerFactory.getLogger(CelleryTopLevelContextProvider.class);

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
                if (forcedRemoved == null || !forcedRemoved) {
                    CompletionUtils.addCelleryInfoToContext(context);
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
        completions.add(SnippetGenerator.getCelleryBuildMethodSnippet().build(context));
        completions.add(SnippetGenerator.getCelleryRunMethodSnippet().build(context));
        return completions;
    }
}
