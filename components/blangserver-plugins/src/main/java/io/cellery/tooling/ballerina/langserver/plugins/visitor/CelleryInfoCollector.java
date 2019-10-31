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

import org.ballerinalang.langserver.common.CommonKeys;
import org.ballerinalang.langserver.compiler.LSContext;
import org.ballerinalang.langserver.completions.SymbolInfo;
import org.ballerinalang.langserver.completions.TreeVisitor;
import org.wso2.ballerinalang.compiler.semantics.model.types.BRecordType;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangRecordLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangSimpleVarRef;
import org.wso2.ballerinalang.compiler.tree.statements.BLangSimpleVariableDef;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Ballerina Lang Node visitor for collecting Cellery related information.
 */
public class CelleryInfoCollector extends TreeVisitor {
    private Map<String, Component> components;
    private Set<String> visibleVariables;

    public CelleryInfoCollector(LSContext lsContext) {
        super(lsContext);
        components = new HashMap<>();
        visibleVariables = lsContext.get(CommonKeys.VISIBLE_SYMBOLS_KEY).stream()
                .filter(symbolInfo -> symbolInfo.getScopeEntry().symbol.type instanceof BRecordType)
                .map(SymbolInfo::getSymbolName)
                .collect(Collectors.toSet());
    }

    @Override
    public void visit(BLangSimpleVariableDef simpleVariableDef) {
        BLangExpression initialExpression = simpleVariableDef.getVariable().getInitialExpression();
        String variableName = simpleVariableDef.getVariable().getName().getValue();
        if (isVisible(variableName) && initialExpression instanceof BLangRecordLiteral) {
            Component component = components.computeIfAbsent(variableName, k -> new Component());

            // Extracting information about the useful fields in the component
            BLangRecordLiteral recordLiteral = (BLangRecordLiteral) initialExpression;
            for (BLangRecordLiteral.BLangRecordKeyValue keyValuePair : recordLiteral.getKeyValuePairs()) {
                if (keyValuePair.getKey() instanceof BLangSimpleVarRef) {
                    BLangSimpleVarRef fieldNameRef = (BLangSimpleVarRef) keyValuePair.getKey();
                    if (Component.NAME_FIELD_NAME.equals(fieldNameRef.getVariableName().getValue())
                            && keyValuePair.getValue() instanceof BLangLiteral) {
                        Object fieldValue = ((BLangLiteral) keyValuePair.getValue()).getValue();
                        component.setName(fieldValue.toString());
                    }
                }
            }
        }
        super.visit(simpleVariableDef);
    }

    /**
     * Check if a variable is visible in the current context.
     *
     * @param variableName The name of the variable
     * @return True if the variable is visible
     */
    private boolean isVisible(String variableName) {
        return visibleVariables.contains(variableName);
    }

    public Map<String, Component> getComponents() {
        return components;
    }
}
