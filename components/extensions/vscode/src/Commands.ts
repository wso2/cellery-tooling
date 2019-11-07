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
import CommandsUtils from "./utils/CommandsUtils";

/**
 * Cellery commands.
 */
class Commands {
    private static readonly terminals: { [name: string]: vscode.Terminal } = { };
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
        let cellName = await vscode.window.showQuickPick(Commands.imageNames, {
            placeHolder: `${Constants.ORG_NAME}/${Constants.IMAGE_NAME}:${Constants.VERSION}`,
        });
        if (cellName === Commands.imageNames[0]) {
            const newCellName = await CommandsUtils.getNewImageName();
            if (newCellName) {
                Commands.imageNames.push(newCellName);
                cellName = newCellName;
            }
        }
        const file = vscode.window.activeTextEditor ? vscode.window.activeTextEditor.document.fileName : null;
        if (cellName && cellName !== Commands.imageNames[0] && file) {
            const buildCommand = `${Constants.CELLERY_BUILD_COMMAND} ${file} ${cellName}`;
            if (!(Commands.terminals.build)) {
                Commands.terminals.build = vscode.window.createTerminal({
                    name: "Cellery Build",
                    cwd: path.dirname(file),
                });
            }
            Commands.terminals.build.show(true);
            Commands.terminals.build.sendText(buildCommand);
        } else {
            const errorMessage: string = "Unable to run cellery build";
            if (!file) {
                vscode.window.showErrorMessage(`${errorMessage}, source file not found`);
            } else if (!cellName) {
                vscode.window.showErrorMessage(`${errorMessage}, image name not found`);
            }
        }
    }

    /**
     * cellery run command handler used by the function 'registerCommand'
     */
    private static readonly handleRunCommand = async() => {
        let cellName = await vscode.window.showQuickPick(Commands.imageNames, {
            placeHolder: `${Constants.ORG_NAME}/${Constants.IMAGE_NAME}:${Constants.VERSION}`,
        });
        let instanceName: string | undefined;
        if (cellName === Commands.imageNames[0]) {
            const newCellName = await CommandsUtils.getNewImageName();
            const newInstanceName = await CommandsUtils.getNewInstanceName();
            if (newCellName && newInstanceName) {
                Commands.imageNames.push(newCellName);
                Commands.instanceNames.push(newInstanceName);
                cellName = newCellName;
                instanceName = newInstanceName;
            }
        } else {
            instanceName = await vscode.window.showQuickPick(Commands.instanceNames, {
                placeHolder: `instance-name`,
            });
            if (instanceName === Commands.instanceNames[0]) {
                const newInstanceName = await CommandsUtils.getNewInstanceName();
                if (newInstanceName) {
                    Commands.instanceNames.push(newInstanceName);
                    instanceName = newInstanceName;
                }
            }
        }
        const file = vscode.window.activeTextEditor ? vscode.window.activeTextEditor.document.fileName : null;
        if (cellName && instanceName && cellName !== Commands.imageNames[0] &&
            instanceName !== Commands.instanceNames[0] && file) {
            const buildCommand = `${Constants.CELLERY_BUILD_COMMAND} ${file} ${cellName}`;
            const runCommand = `${Constants.CELLERY_RUN_COMMAND} ${cellName} -n ${instanceName} -d`;
            const logCommand = `${Constants.CELLERY_LOGS_COMMAND} ${instanceName}`;
            if (!(Commands.terminals.run)) {
                Commands.terminals.run = vscode.window.createTerminal({
                    name: "Cellery Run",
                    cwd: path.dirname(file),
                });
            }
            Commands.terminals.run.show(true);
            Commands.terminals.run.sendText(`${buildCommand} && ${runCommand} && ${logCommand}`);
        } else {
            const errorMessage: string = "Unable to run cellery run";
            if (!file) {
                vscode.window.showErrorMessage(`${errorMessage}, source file not found`);
            } else if (!cellName) {
                vscode.window.showErrorMessage(`${errorMessage}, image name not found`);
            } else if (!instanceName) {
                vscode.window.showErrorMessage(`${errorMessage}, instance name not found`);
            }
        }
    }

    /**
     * cellery test command handler used by the function 'registerCommand'
     */
    private static readonly handleTestCommand = async() => {
        let cellName = await vscode.window.showQuickPick(Commands.imageNames, {
            placeHolder: `${Constants.ORG_NAME}/${Constants.IMAGE_NAME}:${Constants.VERSION}`,
        });
        if (cellName === Commands.imageNames[0]) {
            const newCellName = await CommandsUtils.getNewImageName();
            if (newCellName) {
                Commands.imageNames.push(newCellName);
                cellName = newCellName;
            }
        }
        const file = vscode.window.activeTextEditor ? vscode.window.activeTextEditor.document.fileName : null;
        if (cellName && cellName !== Commands.imageNames[0] && file) {
            const buildCommand = `${Constants.CELLERY_BUILD_COMMAND} ${file} ${cellName}`;
            const testCommand = `${Constants.CELLERY_TEST_COMMAND} ${cellName}`;
            if (!(Commands.terminals.run)) {
                Commands.terminals.run = vscode.window.createTerminal({
                    name: "Cellery Test",
                    cwd: path.dirname(file),
                });
            }
            Commands.terminals.run.show(true);
            Commands.terminals.run.sendText(`${buildCommand} && ${testCommand}`);
        } else {
            const errorMessage: string = "Unable to run cellery test";
            if (!file) {
                vscode.window.showErrorMessage(`${errorMessage}, source file not found`);
            } else if (!cellName) {
                vscode.window.showErrorMessage(`${errorMessage}, image name not found`);
            }
        }
    }
}

export default Commands;
