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

package io.cellery.tooling.ballerina.langserver.plugins.visitor;

import io.cellery.tooling.ballerina.langserver.plugins.Constants;
import io.cellery.tooling.ballerina.langserver.plugins.ImageManager;
import io.cellery.tooling.ballerina.langserver.plugins.ImageManager.Image;
import io.cellery.tooling.ballerina.langserver.plugins.Utils;
import org.ballerinalang.langserver.common.CommonKeys;
import org.ballerinalang.langserver.compiler.LSContext;
import org.ballerinalang.langserver.completions.SymbolInfo;
import org.ballerinalang.langserver.completions.TreeVisitor;
import org.ballerinalang.model.tree.expressions.ExpressionNode;
import org.wso2.ballerinalang.compiler.semantics.model.types.BRecordType;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangInvocation;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangRecordLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangRecordLiteral.BLangRecordKeyValue;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangSimpleVarRef;
import org.wso2.ballerinalang.compiler.tree.statements.BLangSimpleVariableDef;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Ballerina Lang Node visitor for collecting Cellery related information.
 */
public class CelleryInfoCollector extends TreeVisitor {
    private final Map<String, Component> components;
    private final Map<String, Image> references;
    private final Set<String> visibleVariables;

    public CelleryInfoCollector(LSContext lsContext) {
        super(lsContext);
        components = new HashMap<>();
        references = new HashMap<>();
        visibleVariables = lsContext.get(CommonKeys.VISIBLE_SYMBOLS_KEY).stream()
                .filter(symbolInfo -> symbolInfo.getScopeEntry().symbol.type instanceof BRecordType)
                .map(SymbolInfo::getSymbolName)
                .collect(Collectors.toSet());
    }

    @Override
    public void visit(BLangSimpleVariableDef simpleVariableDef) {
        BLangExpression assignedExpression = simpleVariableDef.getVariable().getInitialExpression();
        String variableName = simpleVariableDef.getVariable().getName().getValue();
        if (visibleVariables.contains(variableName)) {
            if (Utils.isRecordType(assignedExpression, Constants.CELLERY_COMPONENT_TYPE)) {
                BLangRecordLiteral recordLiteral = (BLangRecordLiteral) assignedExpression;
                Component component = components.computeIfAbsent(variableName, k -> new Component());

                // Extracting component name
                BLangExpression name = Utils.getFieldValue(recordLiteral, Component.NAME_FIELD_NAME);
                if (name instanceof BLangLiteral) {
                    component.setName(((BLangLiteral) name).getValue().toString());
                }

                // Extracting dependencies information
                BLangExpression dependencies = Utils.getFieldValue(recordLiteral, Component.DEPENDENCIES_FIELD_NAME);
                if (Utils.isRecordType(dependencies, Constants.CELLERY_DEPENDENCIES_TYPE)) {
                    Map<String, Image> componentDependencies = new HashMap<>();
                    // Extracting cell dependencies from Component.dependencies.cells
                    BLangExpression cellDependencies = Utils.getFieldValue((BLangRecordLiteral) dependencies,
                            Component.DEPENDENCIES_CELLS_FIELD_NAME);
                    if (cellDependencies instanceof BLangRecordLiteral) {
                        componentDependencies.putAll(
                                extractDependencyInformation((BLangRecordLiteral) cellDependencies));
                    }
                    // Extracting composite dependencies from Component.dependencies.composites
                    BLangExpression compositeDependencies = Utils.getFieldValue((BLangRecordLiteral) dependencies,
                            Component.DEPENDENCIES_COMPOSITES_FIELD_NAME);
                    if (compositeDependencies instanceof BLangRecordLiteral) {
                        componentDependencies.putAll(
                                extractDependencyInformation((BLangRecordLiteral) compositeDependencies));
                    }
                    component.setDependencies(componentDependencies);
                }
            } else if (Utils.isInvocationReturnType(assignedExpression, Constants.CELLERY_REFERENCE_TYPE)) {
                // Resolving references at definition
                BLangInvocation invocation = (BLangInvocation) assignedExpression;
                List<? extends ExpressionNode> argumentExpressions = invocation.getArgumentExpressions();
                ExpressionNode firstArgument = argumentExpressions.get(0);
                ExpressionNode secondArgument = argumentExpressions.get(1);
                if (firstArgument instanceof BLangSimpleVarRef && secondArgument instanceof BLangLiteral) {
                    String componentVar = ((BLangSimpleVarRef) firstArgument).getVariableName().getValue();
                    String alias = ((BLangLiteral) secondArgument).getValue().toString();
                    Component component = components.get(componentVar);
                    if (component != null) {
                        references.put(variableName, component.getDependencies().get(alias));
                    }
                }
            }
        }
        super.visit(simpleVariableDef);
    }

    /**
     * Extract the component dependencies from the cell/composite dependencies map.
     *
     * @param recordLiteral The record (map)
     * @return The component dependencies map
     */
    private Map<String, Image> extractDependencyInformation(BLangRecordLiteral recordLiteral) {
        List<BLangRecordKeyValue> recordEntries = recordLiteral.getKeyValuePairs();
        Map<String, Image> componentDependencies = new HashMap<>();
        for (BLangRecordKeyValue recordKeyValue : recordEntries) {
            if (recordKeyValue.getKey() instanceof BLangSimpleVarRef) {
                BLangSimpleVarRef recordKey = (BLangSimpleVarRef) recordKeyValue.getKey();
                Image image = null;
                if (recordKeyValue.getValue() instanceof BLangRecordLiteral) {
                    // Extracting dependency specified as record {org: string, name: string, ver: string}
                    BLangRecordLiteral imageRecordLiteral = (BLangRecordLiteral) recordKeyValue.getValue();
                    BLangExpression orgNameExpression = Utils.getFieldValue(imageRecordLiteral,
                            Component.DEPENDENCIES_IMAGE_ORG_FIELD_NAME);
                    BLangExpression imageNameExpression = Utils.getFieldValue(imageRecordLiteral,
                            Component.DEPENDENCIES_IMAGE_NAME_FIELD_NAME);
                    BLangExpression versionExpression = Utils.getFieldValue(imageRecordLiteral,
                            Component.DEPENDENCIES_IMAGE_VERSION_FIELD_NAME);
                    if (orgNameExpression instanceof BLangLiteral
                            && imageNameExpression instanceof BLangLiteral
                            && versionExpression instanceof BLangLiteral) {
                        image = ImageManager.getInstance().getImage(
                                ((BLangLiteral) orgNameExpression).getValue().toString(),
                                ((BLangLiteral) imageNameExpression).getValue().toString(),
                                ((BLangLiteral) versionExpression).getValue().toString());
                    }
                } else if (recordKeyValue.getValue() instanceof BLangLiteral) {
                    // Extracting dependency specified as string image FQN (org/name:ver)
                    String imageFQN = ((BLangLiteral) recordKeyValue.getValue()).getValue().toString();
                    String[] versionSplit = imageFQN.split(":");
                    if (versionSplit.length == 2) {
                        String[] imageSplit = versionSplit[0].split("/");
                        if (imageSplit.length == 2) {
                            image = ImageManager.getInstance()
                                    .getImage(imageSplit[0], imageSplit[1], versionSplit[1]);
                        }
                    }
                }
                if (image != null) {
                    componentDependencies.put(recordKey.getVariableName().getValue(), image);
                }
            }
        }
        return componentDependencies;
    }

    public Map<String, Component> getComponents() {
        return components;
    }

    public Map<String, Image> getReferences() {
        return references;
    }
}
