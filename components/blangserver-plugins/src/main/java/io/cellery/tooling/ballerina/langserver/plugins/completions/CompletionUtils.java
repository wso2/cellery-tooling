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

package io.cellery.tooling.ballerina.langserver.plugins.completions;

import io.cellery.tooling.ballerina.langserver.plugins.ImageManager.Image;
import org.ballerinalang.langserver.completions.util.ItemResolverConstants;
import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.CompletionItemKind;
import org.eclipse.lsp4j.MarkupContent;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Cellery Lang Server plugin Completions related utilities.
 */
public class CompletionUtils {

    /**
     * Generate ingress keys completion items list.
     *
     * @param image The image for which the ingress key completions should be generated
     * @return {@link List<CompletionItem>} List of calculated Completion Items
     */
    public static List<CompletionItem> generateReferenceKeysCompletions(Image image) {
        List<CompletionItem> completions = new ArrayList<>();
        String imageFQN = image.getFQN();
        for (Map.Entry<String, String> reference : image.getReferenceKeys().entrySet()) {
            CompletionItem completionItem = new CompletionItem();
            completionItem.setInsertText(reference.getKey());
            completionItem.setLabel(reference.getKey());
            MarkupContent documentation = new MarkupContent();
            documentation.setKind("markdown");
            documentation.setValue("**Cellery Image Reference Key**"
                    + "\n\n**Image:** " + imageFQN
                    + "\n\n**Key:** " + reference.getKey()
                    + "\n\n**Value:** " + reference.getValue());
            completionItem.setDetail(ItemResolverConstants.FIELD_TYPE);
            completionItem.setDocumentation(documentation);
            completionItem.setKind(CompletionItemKind.Field);
            completions.add(completionItem);
        }
        return completions;
    }

    private CompletionUtils() {     // Prevent initialization
    }
}
