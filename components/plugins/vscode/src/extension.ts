/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import * as vscode from "vscode";
import BalLangServerUtils from "./BalLangServerUtils";
import Constants from "./constants";

export const activate = (context: vscode.ExtensionContext) => {
    try {
        if (!BalLangServerUtils.isLangPluginsAlreadyInstalled(context.extensionPath)) {
            BalLangServerUtils.installLangPlugins(context.extensionPath);
            const reloadAction = "Reload";
            vscode.window.showInformationMessage(
                "Installed Cellery code completion plugins.\nReload required for the changes to take effect.",
                reloadAction,
            ).then((action) => {
                if (action === reloadAction) {
                    vscode.commands.executeCommand(Constants.Commands.WORKBENCH_RELOAD);
                }
            });
        }
    } catch (error) {
        vscode.window.showErrorMessage("Failed to install Cellery code completion plugins automatically.");
        throw error;
    }
};

export const deactivate = () => {
    // Do nothing
};
