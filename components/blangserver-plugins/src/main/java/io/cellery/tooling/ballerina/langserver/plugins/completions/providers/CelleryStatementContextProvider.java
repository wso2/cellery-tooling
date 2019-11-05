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

import io.cellery.tooling.ballerina.langserver.plugins.Constants;
import io.cellery.tooling.ballerina.langserver.plugins.Utils;
import io.cellery.tooling.ballerina.langserver.plugins.completions.CompletionUtils;
import io.cellery.tooling.ballerina.langserver.plugins.completions.SnippetGenerator;
import io.cellery.tooling.ballerina.langserver.plugins.images.ImageManager.Image;
import io.cellery.tooling.ballerina.langserver.plugins.visitor.CelleryKeys;
import org.antlr.v4.runtime.CommonToken;
import org.ballerinalang.annotation.JavaSPIService;
import org.ballerinalang.langserver.common.CommonKeys;
import org.ballerinalang.langserver.common.utils.FilterUtils;
import org.ballerinalang.langserver.compiler.LSContext;
import org.ballerinalang.langserver.completions.CompletionKeys;
import org.ballerinalang.langserver.completions.SymbolInfo;
import org.ballerinalang.langserver.completions.providers.contextproviders.StatementContextProvider;
import org.eclipse.lsp4j.CompletionItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.ballerinalang.compiler.parser.antlr4.BallerinaParser;

import java.util.ArrayList;
import java.util.List;

/**
 * Statement Context Cellery Completions Provider.
 *
 * This deals with injecting Cellery specific completions in Statement Context.
 * The completions are injected only if cellery had been imported.
 */
@JavaSPIService("org.ballerinalang.langserver.completions.spi.LSCompletionProvider")
public class CelleryStatementContextProvider extends StatementContextProvider {
    private static final Logger logger = LoggerFactory.getLogger(CelleryStatementContextProvider.class);

    @Override
    public Precedence getPrecedence() {
        return Precedence.HIGH;
    }

    @Override
    public List<CompletionItem> getCompletions(LSContext context) {
        List<CompletionItem> completions = new ArrayList<>();

        try {
            if (Utils.hasCelleryImport(context)) {
                Boolean forceRemovedStmt = context.get(CompletionKeys.FORCE_REMOVED_STATEMENT_WITH_PARENTHESIS_KEY);
                if (!this.isAnnotationAccessExpression(context) && !this.isAnnotationAttachmentContext(context)
                        && !this.inFunctionReturnParameterContext(context)
                        && (forceRemovedStmt == null || !forceRemovedStmt)) {
                    CompletionUtils.addCelleryInfoToContext(context);
                    int invocationOrDelimiterTokenType = context.get(CompletionKeys.INVOCATION_TOKEN_TYPE_KEY);
                    if (invocationOrDelimiterTokenType == -1) {
                        completions.addAll(this.getCellerySnippetCompletions(context));
                    } else if (invocationOrDelimiterTokenType > -1) {
                        completions.addAll(this.getCelleryFieldAccessOrInvocationCompletions(context));
                    }
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
        List<CompletionItem> completions = new ArrayList<>(3);
        completions.add(SnippetGenerator.getComponentSnippet().build(context));
        completions.add(SnippetGenerator.getCellImageSnippet(context.get(CelleryKeys.COMPONENTS)).build(context));
        completions.add(SnippetGenerator.getCompositeImageSnippet(context.get(CelleryKeys.COMPONENTS)).build(context));
        return completions;
    }

    /**
     * Get Cellery specific invocation completions.
     *
     * @param context Language Server Context
     * @return {@link List<CompletionItem>} List of calculated Completion Items
     */
    private List<CompletionItem> getCelleryFieldAccessOrInvocationCompletions(LSContext context) {
        List<CommonToken> defaultTokens = context.get(CompletionKeys.LHS_DEFAULT_TOKENS_KEY);
        List<Integer> defaultTokenTypes = context.get(CompletionKeys.LHS_DEFAULT_TOKEN_TYPES_KEY);
        int delimiter = context.get(CompletionKeys.INVOCATION_TOKEN_TYPE_KEY);
        int lastDelimiterIndex = defaultTokenTypes.lastIndexOf(delimiter);

        List<CompletionItem> completions = null;
        if (lastDelimiterIndex >= 8
                && BallerinaParser.Identifier == defaultTokenTypes.get(lastDelimiterIndex - 8)
                && BallerinaParser.COLON == defaultTokenTypes.get(lastDelimiterIndex - 7)
                && BallerinaParser.Identifier == defaultTokenTypes.get(lastDelimiterIndex - 6)
                && BallerinaParser.LEFT_PARENTHESIS == defaultTokenTypes.get(lastDelimiterIndex - 5)
                && BallerinaParser.Identifier == defaultTokenTypes.get(lastDelimiterIndex - 4)
                && BallerinaParser.COMMA == defaultTokenTypes.get(lastDelimiterIndex - 3)
                && BallerinaParser.QuotedStringLiteral == defaultTokenTypes.get(lastDelimiterIndex - 2)
                && BallerinaParser.RIGHT_PARENTHESIS == defaultTokenTypes.get(lastDelimiterIndex - 1)
                && BallerinaParser.DOT == defaultTokenTypes.get(lastDelimiterIndex)
                && Constants.CELLERY_PACKAGE_NAME.equals(defaultTokens.get(lastDelimiterIndex - 8).getText())
                && Constants.CELLERY_GET_REFERENCE_METHOD.equals(defaultTokens.get(lastDelimiterIndex - 6).getText())) {
            // Completions for direct invocations on cellery:getReference(componentVar, "alias")
            String componentVariable = defaultTokens.get(lastDelimiterIndex - 4).getText();
            String aliasQuotedLiteral = defaultTokens.get(lastDelimiterIndex - 2).getText();
            String alias = aliasQuotedLiteral.substring(1, aliasQuotedLiteral.length() - 1);

            Image image = context.get(CelleryKeys.COMPONENTS).get(componentVariable).getDependencies().get(alias);
            completions = CompletionUtils.generateReferenceKeysCompletions(image);
        } else {
            List<SymbolInfo> visibleSymbols = new ArrayList<>(context.get(CommonKeys.VISIBLE_SYMBOLS_KEY));
            String symbolToken = defaultTokens.get(lastDelimiterIndex - 1).getText().replace("'", "");
            SymbolInfo symbol = FilterUtils.getVariableByName(symbolToken, visibleSymbols);

            if (symbol != null
                    && Utils.checkType(symbol.getScopeEntry().symbol.type, Constants.CelleryTypes.REFERENCE)) {
                // Completions on variables of type cellery:Reference
                Image image = context.get(CelleryKeys.IMAGE_REFERENCES).get(symbol.getSymbolName());
                completions = CompletionUtils.generateReferenceKeysCompletions(image);
            }
        }
        if (completions == null) {
            completions = new ArrayList<>(0);
        }
        return completions;
    }
}
