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
import org.wso2.ballerinalang.compiler.tree.BLangImportPackage;

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
                if (Constants.CELLERY_PACKAGE_ORG_NAME.equals(anImport.getOrgName().getValue()) &&
                        Constants.CELLERY_PACKAGE_NAME.equals(packageName)) {
                    hasCelleryImport = true;
                }
            }
        }
        return hasCelleryImport;
    }
}
