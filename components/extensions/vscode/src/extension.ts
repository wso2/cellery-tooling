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

import * as os from "os";
import * as vscode from "vscode";
import Constants from "./constants";
import ExtensionUtils from "./utils/ExtensionUtils";

export const activate = (context: vscode.ExtensionContext) => {
    try {
        ExtensionUtils.setupBalLangServerPlugins(context.extensionPath);
        const commandHandler = (command: string) => async() => {
            const cellName = await vscode.window.showInputBox({
                placeHolder: `${Constants.ORG_NAME}/
            ${Constants.IMAGE_NAME}:${Constants.VERSION}`,
            });
            const file = vscode.window.activeTextEditor ? vscode.window.activeTextEditor.document.fileName : null;
            const cwd = vscode.workspace.workspaceFolders ? vscode.workspace.workspaceFolders[0].uri : null;
            if (cellName && file && cwd) {
                const celleryCommand = `${Constants.CELLERY_KEYWORD} ${command} ${file} ${cellName}`;
                const terminal = vscode.window.createTerminal({ name: command, cwd: cwd});
                terminal.show(true);
                if (file !== null) {
                    terminal.sendText(celleryCommand);
                }
            }
        };
        context.subscriptions.push(vscode.commands.registerCommand(Constants.commands.CELLERY_BUILD,
                                                                   commandHandler("build")));
    } catch (error) {
        vscode.window.showErrorMessage("Failed to install Cellery code completion plugins");
        throw error;
    }

};

export const deactivate = () => {
    // Do nothing
};
