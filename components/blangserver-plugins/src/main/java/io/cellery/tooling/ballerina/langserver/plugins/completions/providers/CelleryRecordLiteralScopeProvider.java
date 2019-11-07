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
import io.cellery.tooling.ballerina.langserver.plugins.images.ImageManager;
import org.ballerinalang.annotation.JavaSPIService;
import org.ballerinalang.langserver.compiler.LSContext;
import org.ballerinalang.langserver.completions.CompletionKeys;
import org.ballerinalang.langserver.completions.providers.scopeproviders.RecordLiteralScopeProvider;
import org.eclipse.lsp4j.CompletionItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.ballerinalang.compiler.parser.antlr4.BallerinaParser;
import org.wso2.ballerinalang.compiler.tree.BLangNode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

/**
 * Record Literal Scope Cellery Completions Provider.
 *
 * This deals with injecting Cellery specific completions in Record Literal Scope.
 * The completions are injected only if cellery had been imported.
 */
@JavaSPIService("org.ballerinalang.langserver.completions.spi.LSCompletionProvider")
public class CelleryRecordLiteralScopeProvider extends RecordLiteralScopeProvider {
    private static final Logger logger = LoggerFactory.getLogger(CelleryRecordLiteralScopeProvider.class);

    @Override
    public Precedence getPrecedence() {
        return Precedence.HIGH;
    }

    @Override
    public List<CompletionItem> getCompletions(LSContext context) {
        List<CompletionItem> completions = new ArrayList<>();
        try {
            if (Utils.hasCelleryImport(context)) {
                BLangNode scopeNode = context.get(CompletionKeys.SCOPE_NODE_KEY);
                List<Integer> defaultTokenTypes = context.get(CompletionKeys.LHS_DEFAULT_TOKEN_TYPES_KEY);
                Integer invocationToken = context.get(CompletionKeys.INVOCATION_TOKEN_TYPE_KEY);

                int invocationTokenTypeIndex = defaultTokenTypes.lastIndexOf(invocationToken);
                int firstColonIndex = defaultTokenTypes.indexOf(BallerinaParser.COLON);
                if (Utils.checkMapType(scopeNode.type, Constants.CelleryTypes.IMAGE_NAME)) {
                    if (firstColonIndex == -1) {
                        completions.addAll(getCelleryImageCompletions(true));
                    } else if (firstColonIndex == invocationTokenTypeIndex) {
                        completions.addAll(getCelleryImageCompletions(false));
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
     * Get Cellery specific completions for Images to be added as dependencies.
     *
     * @return {@link List<CompletionItem>} List of calculated Completion Items
     */
    private List<CompletionItem> getCelleryImageCompletions(boolean includeAlias) {
        Collection<ImageManager.Image> images = ImageManager.getInstance().getImages();
        List<CompletionItem> completions;
        if (includeAlias) {
            completions = CompletionUtils.generateImageStringCompletions(images, (image) -> {
                String[] imageNameSplit = image.getName().split("-");
                String alias = imageNameSplit[0]
                        + Arrays.stream(Arrays.copyOfRange(imageNameSplit, 1, imageNameSplit.length))
                        .map((imageNameSplitItem) -> imageNameSplitItem.substring(0, 1).toUpperCase(Locale.ENGLISH)
                                + imageNameSplitItem.substring(1))
                        .collect(Collectors.joining(""));
                return alias + ": \"" + image.getFQN() + "\"";
            });
        } else {
            completions = CompletionUtils.generateImageStringCompletions(images,
                    (image) -> "\"" + image.getFQN() + "\"");
        }
        return completions;
    }
}
