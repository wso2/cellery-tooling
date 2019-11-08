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
    private static readonly imageNames: string[] = ["add image name"];
    private static readonly instanceNames: string[] = ["add instance name"];

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
        const errorMessage: string = "Unable to run cellery build";
        let imageName = await vscode.window.showQuickPick(Commands.imageNames, {
            placeHolder: `${Constants.ORG_NAME}/${Constants.IMAGE_NAME}:${Constants.VERSION}`,
        });
        if (imageName === undefined) {
            vscode.window.showErrorMessage(`${errorMessage}, image name not found`);
            return;
        }
        if (imageName === Commands.imageNames[0]) {
            const newImageName = await Commands.getNewImageName();
            if (newImageName === undefined) {
                vscode.window.showErrorMessage(`${errorMessage}, image name not found`);
                return;
            }
            Commands.imageNames.push(newImageName);
            imageName = newImageName;
        }
        const file = vscode.window.activeTextEditor ? vscode.window.activeTextEditor.document.fileName : null;
        if (file === null) {
            vscode.window.showErrorMessage(`${errorMessage}, source file not found`);
            return;
        }
        const buildCommand = `${Constants.CELLERY_BUILD_COMMAND} ${file} ${imageName}`;
        Commands.runCommandInTerminal(buildCommand, file, Constants.terminals.CELLERY_BUILD);
    }

    /**
     * cellery run command handler used by the function 'registerCommand'
     */
    private static readonly handleRunCommand = async() => {
        const errorMessage: string = "Unable to run cellery run";
        let imageName = await vscode.window.showQuickPick(Commands.imageNames, {
            placeHolder: `Pick an image name or add new image name`,
        });
        let instanceName: string | undefined;
        if (imageName === undefined) {
            vscode.window.showErrorMessage(`${errorMessage}, image name not found`);
            return;
        }
        if (imageName === Commands.imageNames[0]) {
            const newImageName = await Commands.getNewImageName();
            if (newImageName === undefined) {
                vscode.window.showErrorMessage(`${errorMessage}, image name not found`);
                return;
            }
            Commands.imageNames.push(newImageName);
            imageName = newImageName;
            const newInstanceName = await Commands.getNewInstanceName();
            if (newInstanceName === undefined) {
                vscode.window.showErrorMessage(`${errorMessage}, instance name not found`);
                return;
            }
            Commands.instanceNames.push(newInstanceName);
            instanceName = newInstanceName;
        } else {
            instanceName = await vscode.window.showQuickPick(Commands.instanceNames, {
                placeHolder: `Pick an instance-name or add new instance-name`,
            });
            if (instanceName === undefined) {
                vscode.window.showErrorMessage(`${errorMessage}, instance name not found`);
                return;
            }
            if (instanceName === Commands.instanceNames[0]) {
                const newInstanceName = await Commands.getNewInstanceName();
                if (newInstanceName === undefined) {
                    vscode.window.showErrorMessage(`${errorMessage}, instance name not found`);
                    return;
                }
                Commands.instanceNames.push(newInstanceName);
                instanceName = newInstanceName;
            }
        }
        const file = vscode.window.activeTextEditor ? vscode.window.activeTextEditor.document.fileName : null;
        if (file === null) {
            vscode.window.showErrorMessage(`${errorMessage}, source file not found`);
            return;
        }
        const buildCommand = `${Constants.CELLERY_BUILD_COMMAND} ${file} ${imageName}`;
        const runCommand = `${Constants.CELLERY_RUN_COMMAND} ${imageName} -n ${instanceName} -d`;
        const logCommand = `${Constants.CELLERY_LOGS_COMMAND} ${instanceName}`;
        Commands.runCommandInTerminal(`${buildCommand} && ${runCommand} && ${logCommand}`, file,
                                      Constants.terminals.CELLERY_RUN);
    }

    /**
     * cellery test command handler used by the function 'registerCommand'
     */
    private static readonly handleTestCommand = async() => {
        const errorMessage: string = "Unable to run cellery test";
        let imageName = await vscode.window.showQuickPick(Commands.imageNames, {
            placeHolder: `${Constants.ORG_NAME}/${Constants.IMAGE_NAME}:${Constants.VERSION}`,
        });
        if (imageName === undefined) {
            vscode.window.showErrorMessage(`${errorMessage}, image name not found`);
            return;
        }
        if (imageName === Commands.imageNames[0]) {
            const newImageName = await Commands.getNewImageName();
            if (newImageName === undefined) {
                vscode.window.showErrorMessage(`${errorMessage}, image name not found`);
                return;
            }
            Commands.imageNames.push(newImageName);
            imageName = newImageName;
        }
        const file = vscode.window.activeTextEditor ? vscode.window.activeTextEditor.document.fileName : null;
        if (file === null) {
            vscode.window.showErrorMessage(`${errorMessage}, source file not found`);
            return;
        }
        const buildCommand = `${Constants.CELLERY_BUILD_COMMAND} ${file} ${imageName}`;
        const testCommand = `${Constants.CELLERY_TEST_COMMAND} ${imageName}`;
        Commands.runCommandInTerminal(`${buildCommand} && ${testCommand}`, file,
                                      Constants.terminals.CELLERY_TEST);
    }

    /**
     * Get new image name from user
     */
    private static getNewImageName(): Thenable<string | undefined> {
        return vscode.window.showInputBox({
            placeHolder: `${Constants.ORG_NAME}/${Constants.IMAGE_NAME}:${Constants.VERSION}`,
            prompt: `Enter the image name`,
            validateInput: (value) => {
                return CommonUtils.isValidImage(value);
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
                return CommonUtils.isValidInstanceName(value);
            },
        });
    }

    /**
     * Run CLI command in terminal
     */
    private static runCommandInTerminal(command: string, file: string, terminalName: string) {
        let isTerminalAvailable = false;
        vscode.window.terminals.forEach((terminal) => {
            if (terminal.name === terminalName) {
                terminal.show(true);
                terminal.sendText(command);
                isTerminalAvailable = true;
            }
        });
        if (isTerminalAvailable) {
            return;
        }
        const newTerminal = vscode.window.createTerminal({
            name: terminalName,
            cwd: path.dirname(file),
        });
        newTerminal.show(true);
        newTerminal.sendText(command);
    }
}

export default Commands;
