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

import io.cellery.tooling.ballerina.langserver.plugins.Constants;
import io.cellery.tooling.ballerina.langserver.plugins.images.ComponentMetadata;
import io.cellery.tooling.ballerina.langserver.plugins.images.ImageManager.Image;
import io.cellery.tooling.ballerina.langserver.plugins.visitor.CelleryKeys;
import io.cellery.tooling.ballerina.langserver.plugins.visitor.CelleryTreeVisitor;
import org.ballerinalang.langserver.compiler.DocumentServiceKeys;
import org.ballerinalang.langserver.compiler.LSContext;
import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.CompletionItemKind;
import org.eclipse.lsp4j.MarkupContent;
import org.wso2.ballerinalang.compiler.tree.BLangNode;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

/**
 * Cellery Lang Server plugin Completions related utilities.
 */
public class CompletionUtils {

    /**
     * Add Cellery specific information to the language server context.
     */
    public static synchronized void addCelleryInfoToContext(LSContext context) {
        if (context.get(CelleryKeys.COMPONENTS) == null) {  // Ensuring that Cellery visitor is used only once
            BLangNode packageNode = context.get(DocumentServiceKeys.CURRENT_BLANG_PACKAGE_CONTEXT_KEY);
            CelleryTreeVisitor celleryTreeVisitor = new CelleryTreeVisitor(context);
            packageNode.accept(celleryTreeVisitor);
        }
    }

    /**
     * Generate ingress keys completion items list.
     *
     * @param image The image for which the ingress key completions should be generated
     * @return {@link List<CompletionItem>} List of calculated Completion Items
     */
    public static List<CompletionItem> generateReferenceKeysCompletions(Image image) {
        List<CompletionItem> completions = new ArrayList<>(image.getReferenceKeys().size());
        String imageFQN = image.getFQN();
        for (Map.Entry<String, String> reference : image.getReferenceKeys().entrySet()) {
            CompletionItem completionItem = new CompletionItem();
            completionItem.setInsertText("get(\"" + reference.getKey() + "\")");
            completionItem.setLabel("get(\"" + reference.getKey() + "\")");
            MarkupContent documentation = new MarkupContent();
            documentation.setKind("markdown");
            documentation.setValue("**Cellery Image Reference Key**"
                    + "\n\n**Image:** " + imageFQN
                    + "\n\n**Key:** " + reference.getKey()
                    + "\n\n**Value:** " + reference.getValue());
            completionItem.setDocumentation(documentation);
            completionItem.setDetail(Constants.CompletionType.CELLERY_REFERENCE_KEY);
            completionItem.setKind(CompletionItemKind.Function);
            completions.add(completionItem);
        }
        return completions;
    }

    /**
     * Generate image completions.
     *
     * @param images The images list for which the ingress key completions should be generated
     * @param insertTextMapFunction Function to map insert text from image
     * @return {@link List<CompletionItem>} List of calculated Completion Items
     */
    public static List<CompletionItem> generateImageStringCompletions(Collection<Image> images,
                                                                      Function<Image, String> insertTextMapFunction) {
        List<CompletionItem> completions = new ArrayList<>(images.size());
        for (Image image : images) {
            String autoScalingStatus = null;
            if (image.getMetadata().isZeroScalingRequired()) {
                autoScalingStatus = "Zero Scaling";
            }
            if (image.getMetadata().isAutoScalingRequired()) {
                autoScalingStatus = (autoScalingStatus == null ? "HPA" : autoScalingStatus + " & HPA");
            }
            if (autoScalingStatus == null) {
                autoScalingStatus = "Disabled";
            }

            Set<String> ingressTypes = new HashSet<>();
            for (ComponentMetadata componentMetadata : image.getMetadata().getComponents().values()) {
                ingressTypes.addAll(componentMetadata.getIngressTypes());
            }

            CompletionItem completionItem = new CompletionItem();
            completionItem.setInsertText(insertTextMapFunction.apply(image));
            completionItem.setLabel(image.getFQN());
            MarkupContent documentation = new MarkupContent();
            documentation.setKind("markdown");
            documentation.setValue("**Cellery Image**"
                    + "\n\n**Image:** " + image.getFQN()
                    + "\n\n**Ingress Types:** " + String.join(", ", ingressTypes)
                    + "\n\n**Kind:** " + image.getMetadata().getKind()
                    + "\n\n**Auto-Scaling:** " + autoScalingStatus);
            completionItem.setDocumentation(documentation);
            completionItem.setDetail(Constants.CompletionType.CELLERY_IMAGE);
            completionItem.setKind(CompletionItemKind.Text);
            completions.add(completionItem);
        }
        return completions;
    }

    private CompletionUtils() {     // Prevent initialization
    }
}
