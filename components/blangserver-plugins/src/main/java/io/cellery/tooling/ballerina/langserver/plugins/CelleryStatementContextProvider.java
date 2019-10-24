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

package io.cellery.tooling.ballerina.langserver.plugins;

import org.ballerinalang.annotation.JavaSPIService;
import org.ballerinalang.langserver.compiler.LSContext;
import org.ballerinalang.langserver.completions.CompletionKeys;
import org.ballerinalang.langserver.completions.providers.contextproviders.StatementContextProvider;
import org.eclipse.lsp4j.CompletionItem;

import java.util.ArrayList;
import java.util.List;

/**
 * Statement Context Cellery Completions Provider.
 *
 * This deals with injecting Cellery specific completions in Statement Context.
 */
@JavaSPIService("org.ballerinalang.langserver.completions.spi.LSCompletionProvider")
public class CelleryStatementContextProvider extends StatementContextProvider {

    @Override
    public Precedence getPrecedence() {
        return Precedence.HIGH;
    }

    @Override
    public List<CompletionItem> getCompletions(LSContext context) {
        List<CompletionItem> completions = new ArrayList<>();

        if (Utils.hasCelleryImport(context)) {
            int invocationOrDelimiterTokenType = context.get(CompletionKeys.INVOCATION_TOKEN_TYPE_KEY);
            Boolean forceRemovedStmt = context.get(CompletionKeys.FORCE_REMOVED_STATEMENT_WITH_PARENTHESIS_KEY);
            if (!this.isAnnotationAccessExpression(context) && !this.isAnnotationAttachmentContext(context)
                    && !this.inFunctionReturnParameterContext(context)
                    && (forceRemovedStmt == null || !forceRemovedStmt) && invocationOrDelimiterTokenType == -1) {
                completions.addAll(this.getCelleryCompletions(context));
            }
        }

        // Get statement context completions
        completions.addAll(super.getCompletions(context));
        return completions;
    }

    /**
     * Get cellery specific completion items.
     *
     * @param context Language Server Context
     * @return {@link List<CompletionItem>} List of calculated Completion Items
     */
    private List<CompletionItem> getCelleryCompletions(LSContext context) {
        List<CompletionItem> completions = new ArrayList<>();
        completions.add(SnippetGenerator.getComponentSnippet().build(context));
        return completions;
    }
}
