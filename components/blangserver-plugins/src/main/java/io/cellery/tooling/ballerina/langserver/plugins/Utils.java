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

import org.ballerinalang.langserver.common.utils.CommonUtil;
import org.ballerinalang.langserver.compiler.LSContext;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BTypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.types.BMapType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BUnionType;
import org.wso2.ballerinalang.compiler.tree.BLangImportPackage;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangInvocation;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangRecordLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangSimpleVarRef;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangTypeConversionExpr;

import java.util.Objects;

/**
 * Cellery Lang Server plugin utilities.
 */
public class Utils {

    /**
     * Check whether cellery import was added.
     *
     * @param context Language Server Context
     * @return True if the cellery import is present
     */
    public static boolean hasCelleryImport(LSContext context) {
        boolean hasCelleryImport = false;
        for (BLangImportPackage anImport : CommonUtil.getCurrentFileImports(context)) {
            if (anImport.getPackageName().size() == 1) {
                String packageName = anImport.getPackageName().get(0).getValue();
                if (Constants.CELLERY_PACKAGE_ORG_NAME.equals(anImport.getOrgName().getValue())
                        && Constants.CELLERY_PACKAGE_NAME.equals(packageName)) {
                    hasCelleryImport = true;
                }
            }
        }
        return hasCelleryImport;
    }

    /**
     * Check if the ballerina type is equal to the Cellery record type name.
     *
     * @param bLangExpression The ballerina expression of which the type should be checked
     * @param typeName The name of the Cellery type
     * @return True if the type if equal
     */
    public static boolean checkRecordType(BLangExpression bLangExpression, String typeName) {
        boolean isCorrectType = false;
        if (bLangExpression instanceof BLangRecordLiteral) {
            isCorrectType = checkType(((BLangRecordLiteral) bLangExpression).type, typeName);
        }
        return isCorrectType;
    }

    /**
     * Check if the ballerina type is equal to the Cellery invocation type name.
     *
     * @param bLangExpression The ballerina expression of which the type should be checked
     * @param typeName The name of the Cellery type
     * @return True if the type if equal
     */
    public static boolean checkInvocationReturnType(BLangExpression bLangExpression, String typeName) {
        boolean isCorrectType = false;
        if (bLangExpression instanceof BLangInvocation) {
            isCorrectType = checkType(((BLangInvocation) bLangExpression).type, typeName);
        }
        return isCorrectType;
    }

    /**
     * Check if the ballerina type is equal to the Cellery Map type.
     *
     * @param bType The ballerina type of which the type should be checked
     * @param constraintTypeName The name of the Cellery type of the map constraint
     * @return True if the type if equal
     */
    public static boolean checkMapType(BType bType, String constraintTypeName) {
        boolean isCorrectType = false;
        if (bType instanceof BMapType) {
            BMapType bMapType = (BMapType) bType;
            if (bMapType.constraint instanceof BUnionType) {
                BUnionType bUnionType = (BUnionType) bMapType.constraint;
                for (BType memberType : bUnionType.getMemberTypes()) {
                    if (checkType(memberType, constraintTypeName)) {
                        isCorrectType = true;
                        break;
                    }
                }
            } else {
                isCorrectType = checkType(bMapType.constraint, constraintTypeName);
            }
        }
        return isCorrectType;
    }

    /**
     * Check if the ballerina type is equal to the Cellery type name.
     *
     * @param bType The ballerina type of which the type should be checked
     * @param typeName The name of the Cellery type
     * @return True if the type if equal
     */
    public static boolean checkType(BType bType, String typeName) {
        BTypeSymbol tSymbol = bType.tsymbol;
        return Constants.CELLERY_PACKAGE_ORG_NAME.equals(tSymbol.pkgID.getOrgName().getValue())
                && Constants.CELLERY_PACKAGE_NAME.equals(tSymbol.pkgID.getName().getValue())
                && Objects.equals(typeName, tSymbol.name.getValue());
    }

    /**
     * Get the field value of a Ballerina record literal.
     *
     * @param recordLiteral The record literal from which the value should be extracted
     * @param fieldName The name of the field
     * @return The ballerina literal value extracted
     */
    public static BLangExpression getFieldValue(BLangRecordLiteral recordLiteral, String fieldName) {
        BLangExpression fieldValue = null;
        for (BLangRecordLiteral.BLangRecordKeyValue keyValuePair : recordLiteral.getKeyValuePairs()) {
            if (keyValuePair.getKey() instanceof BLangSimpleVarRef) {
                BLangSimpleVarRef fieldNameRef = (BLangSimpleVarRef) keyValuePair.getKey();
                if (Objects.equals(fieldNameRef.getVariableName().getValue(), fieldName)) {
                    fieldValue = keyValuePair.getValue();
                    break;
                }
            }
        }
        return fieldValue;
    }

    /**
     * Get the actual actual ballerina expression from casted ballerina expression
     *
     * @param bLangExpression A pure ballerina expression or casted ballerina expression
     * @return The ballerina expression originally passed or retrieved from ballerina type conversion expression
     */
    public static BLangExpression getActualExpression(BLangExpression bLangExpression) {
        BLangExpression actualExpression = null;
        if (bLangExpression instanceof BLangTypeConversionExpr) {
            actualExpression = ((BLangTypeConversionExpr) bLangExpression).expr;
        }
        if (actualExpression == null) {
            actualExpression = bLangExpression;
        }
        return actualExpression;
    }
}
