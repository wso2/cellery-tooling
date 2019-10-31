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
import commands from "./Commands";
import Constants from "./constants";
import ExtensionUtils from "./utils/ExtensionUtils";

export const activate = (context: vscode.ExtensionContext) => {
    try {
        ExtensionUtils.setupBalLangServerPlugins(context.extensionPath);
        context.subscriptions.push(vscode.commands.registerCommand(Constants.commands.CELLERY_BUILD,
                                                                   commands.buildCommandHandler));
        context.subscriptions.push(vscode.commands.registerCommand(Constants.commands.CELLERY_RUN,
                                                                   commands.runCommandHandler));
    } catch (error) {
        vscode.window.showErrorMessage("Failed to install Cellery code completion plugins");
        throw error;
    }

};

export const deactivate = () => {
    // Do nothing
};
