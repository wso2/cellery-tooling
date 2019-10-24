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

import * as fse from "fs-extra";
import * as path from "path";
import * as vscode from "vscode";
import BalLangServerUtils from "./BalLangServerUtils";
import CommonUtils from "./CommonUtils";
import Constants from "./constants";

/**
 * Extension related utilities.
 */
class ExtensionUtils {

    /**
     * Setup Ballerina language server plugins.
     *
     * @param extensionPath Path containing the extension
     */
    public static setupBalLangServerPlugins(extensionPath: string) {
        const celleryToolingDir = path.resolve(CommonUtils.getCelleryHome(), Constants.CELLERY_TOOLING_DIR);
        if (!BalLangServerUtils.isSymlinkPresent() || !fse.existsSync(celleryToolingDir)) {
            vscode.window.showErrorMessage(
                `Unable to properly configure auto completion due to missing links.
                Please run "bash ${path.resolve(extensionPath, Constants.FIX_SYMLINK_SCRIPT)}"
                and restart VS Code to fix this issue`,
            );
        } else if (!BalLangServerUtils.isLangPluginsAlreadyInstalled(extensionPath)) {
            BalLangServerUtils.installLangPlugins(extensionPath);

            // Prompting user to reload
            const reloadAction = "Reload";
            vscode.window.showInformationMessage(
                "Installed Cellery code completion plugins. Reload required for the changes to take effect.",
                reloadAction,
            ).then((action) => {
                if (action === reloadAction) {
                    vscode.commands.executeCommand(Constants.commands.WORKBENCH_RELOAD);
                }
            });
        } else {
            console.log("Lang Server plugins had been already installed");
        }
    }
}

export default ExtensionUtils;
