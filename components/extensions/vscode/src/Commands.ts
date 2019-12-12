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

import * as path from "path";
import * as vscode from "vscode";
import Constants from "./constants";
import CommonUtils from "./utils/CommonUtils";

/**
 * Cellery commands.
 */
class Commands {
    private static cachedImageNames: string[] = [];
    private static cahchedInstanceNames: string[] = [];
    private static readonly ADD_IMAGE_NAME = "add image name";
    private static readonly ADD_INSTANCE_NAME = "add instance name";

    /**
     * Register Cellery Commands
     */
    public static registerCelleryCommands = (context: vscode.ExtensionContext) => {
        context.subscriptions.push(vscode.commands.registerCommand(Constants.commands.CELLERY_BUILD,
                                                                   Commands.handleBuildCommand));
        context.subscriptions.push(vscode.commands.registerCommand(Constants.commands.CELLERY_RUN,
                                                                   Commands.handleRunCommand));
        context.subscriptions.push(vscode.commands.registerCommand(Constants.commands.CELLERY_TEST,
                                                                   Commands.handleTestCommand));
    }

    /**
     * cellery build command handler used by the function 'registerCommand'
     */
    private static readonly handleBuildCommand = async() => {
        let imageName: string | undefined = "";
        if (Commands.cachedImageNames.length > 0) {
            imageName = await Commands.getCachedImageName();
            if (imageName === undefined) {
                return;
            }
        }
        if (imageName === Commands.ADD_IMAGE_NAME || Commands.cachedImageNames.length === 0) {
            const newImageName = await Commands.getNewImageName();
            if (newImageName === undefined) {
                return;
            }
            imageName = newImageName;
        }
        Commands.cachedImageNames = Commands.cachedImageNames.filter((item) => item !== imageName);
        Commands.cachedImageNames.unshift(imageName);
        const file = vscode.window.activeTextEditor ? vscode.window.activeTextEditor.document.fileName : null;
        if (file === null) {
            vscode.window.showErrorMessage("Failed to run cellery build as no Cell file open. " +
                "Please open a Cell file to perform this action.");
            return;
        }
        const buildCommand = `${Constants.celleryCommands.CELLERY_BUILD} ${file} ${imageName}`;
        Commands.runCommandInCurrentTerminal(buildCommand);
    }

    /**
     * cellery run command handler used by the function 'registerCommand'
     */
    private static readonly handleRunCommand = async() => {
        let imageName: string | undefined = "";
        if (Commands.cachedImageNames.length > 0) {
            imageName = await Commands.getCachedImageName();
            if (imageName === undefined) {
                return;
            }
        }
        let instanceName: string | undefined = "";
        if (imageName === Commands.ADD_IMAGE_NAME || Commands.cachedImageNames.length === 0) {
            const newImageName = await Commands.getNewImageName();
            if (newImageName === undefined) {
                return;
            }
            imageName = newImageName;
            const newInstanceName = await Commands.getNewInstanceName();
            if (newInstanceName === undefined) {
                return;
            }
            instanceName = newInstanceName;
        } else {
            if (Commands.cahchedInstanceNames.length > 0) {
                instanceName = await Commands.getCachedInstanceName();
                if (instanceName === undefined) {
                    return;
                }
            }
            if (instanceName === Commands.ADD_INSTANCE_NAME || Commands.cahchedInstanceNames.length === 0) {
                const newInstanceName = await Commands.getNewInstanceName();
                if (newInstanceName === undefined) {
                    return;
                }
                instanceName = newInstanceName;
            }
        }
        Commands.cachedImageNames = Commands.cachedImageNames.filter((item) => item !== imageName);
        Commands.cachedImageNames.unshift(imageName);
        Commands.cahchedInstanceNames = Commands.cahchedInstanceNames.filter((item) => item !== instanceName);
        Commands.cahchedInstanceNames.unshift(instanceName);
        const file = vscode.window.activeTextEditor ? vscode.window.activeTextEditor.document.fileName : null;
        if (file === null) {
            vscode.window.showErrorMessage("Failed to run cellery run as no Cell file open. " +
                "Please open a Cell file to perform this action.");
            return;
        }
        const buildCommand = `${Constants.celleryCommands.CELLERY_BUILD} ${file} ${imageName}`;
        const runCommand = `${Constants.celleryCommands.CELLERY_RUN} ${imageName} -n ${instanceName} -d`;
        const logCommand = `${Constants.celleryCommands.CELLERY_LOGS} ${instanceName} -f`;
        Commands.runCommandInCurrentTerminal(`${buildCommand} && ${runCommand} && ${logCommand}`);
    }

    /**
     * cellery test command handler used by the function 'registerCommand'
     */
    private static readonly handleTestCommand = async() => {
        let imageName: string | undefined = "";
        if (Commands.cachedImageNames.length > 0) {
            imageName = await Commands.getCachedImageName();
            if (imageName === undefined) {
                return;
            }
        }
        if (imageName === Commands.ADD_IMAGE_NAME || Commands.cachedImageNames.length === 0) {
            const newImageName = await Commands.getNewImageName();
            if (newImageName === undefined) {
                return;
            }
            imageName = newImageName;
        }
        Commands.cachedImageNames = Commands.cachedImageNames.filter((item) => item !== imageName);
        Commands.cachedImageNames.unshift(imageName);
        const file = vscode.window.activeTextEditor ? vscode.window.activeTextEditor.document.fileName : null;
        if (file === null) {
            vscode.window.showErrorMessage("Failed to run cellery test as no Cell file open. " +
                "Please open a Cell file to perform this action.");
            return;
        }
        const buildCommand = `${Constants.celleryCommands.CELLERY_BUILD} ${file} ${imageName}`;
        const testCommand = `${Constants.celleryCommands.CELLERY_TEST} ${imageName}`;
        Commands.runCommandInCurrentTerminal(`${buildCommand} && ${testCommand}`);
    }

    /**
     * Get cached image name picked by the user
     */
    private static getCachedImageName(): Thenable<string | undefined> {
        return vscode.window.showQuickPick(
            [...Commands.cachedImageNames, Commands.ADD_IMAGE_NAME],
            { placeHolder: "Pick an image name or add new image name",
        });
    }

    /**
     * Get cached instance name picked by the user
     */
    private static getCachedInstanceName(): Thenable<string | undefined> {
        return vscode.window.showQuickPick(
            [...Commands.cahchedInstanceNames, Commands.ADD_INSTANCE_NAME],
            { placeHolder: `Pick an instance-name or add new instance-name`,
        });
    }

    /**
     * Get new image name from user
     */
    private static getNewImageName(): Thenable<string | undefined> {
        return vscode.window.showInputBox({
            placeHolder: `${Constants.ORG_NAME}/${Constants.IMAGE_NAME}:${Constants.VERSION}`,
            prompt: `Enter the image name`,
            validateInput: (value) => {
                return CommonUtils.validateImageName(value);
            },
        });
    }

    /**
     * Get new instance name from user
     */
    private static getNewInstanceName(): Thenable<string | undefined> {
        return vscode.window.showInputBox({
            value: `${vscode.window.activeTextEditor
                ? path.parse(vscode.window.activeTextEditor.document.fileName).name
                : "my-instance"}`,
            prompt: `Enter the instance name`,
            validateInput: (value) => {
                return CommonUtils.validateInstanceName(value);
            },
        });
    }

    /**
     * Run CLI command in current terminal
     */
    private static runCommandInCurrentTerminal(command: string) {
        let terminal = vscode.window.activeTerminal;
        if (terminal === undefined) {
            terminal = vscode.window.createTerminal({
                name: "cellery",
            });
        }
        terminal.show();
        terminal.sendText(command);
    }
}

export default Commands;
