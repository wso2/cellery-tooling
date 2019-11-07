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
        const errorMessage: string = "Unable to run cellery build";
        let cellName = await vscode.window.showQuickPick(Commands.imageNames, {
            placeHolder: `${Constants.ORG_NAME}/${Constants.IMAGE_NAME}:${Constants.VERSION}`,
        });
        if (cellName === Commands.imageNames[0]) {
            const newCellName = await CommandsUtils.getNewImageName();
            if (newCellName === undefined || !CommandsUtils.validateImageTag(newCellName)) {
                if (newCellName === undefined) {
                    vscode.window.showErrorMessage(`${errorMessage}, image name not found`);
                }
                return;
            }
            Commands.imageNames.push(newCellName);
            cellName = newCellName;
        }
        const file = vscode.window.activeTextEditor ? vscode.window.activeTextEditor.document.fileName : null;
        if (file === null) {
            vscode.window.showErrorMessage(`${errorMessage}, source file not found`);
            return;
        }
        const buildCommand = `${Constants.CELLERY_BUILD_COMMAND} ${file} ${cellName}`;
        if (!(Commands.terminals.build)) {
            Commands.terminals.build = vscode.window.createTerminal({
                name: "Cellery Build",
                cwd: path.dirname(file),
            });
        }
        Commands.terminals.build.show(true);
        Commands.terminals.build.sendText(buildCommand);
    }

    /**
     * cellery run command handler used by the function 'registerCommand'
     */
    private static readonly handleRunCommand = async() => {
        const errorMessage: string = "Unable to run cellery run";
        let cellName = await vscode.window.showQuickPick(Commands.imageNames, {
            placeHolder: `${Constants.ORG_NAME}/${Constants.IMAGE_NAME}:${Constants.VERSION}`,
        });
        let instanceName: string | undefined;
        if (cellName === Commands.imageNames[0]) {
            const newCellName = await CommandsUtils.getNewImageName();
            if (newCellName === undefined || !CommandsUtils.validateImageTag(newCellName)) {
                if (newCellName === undefined) {
                    vscode.window.showErrorMessage(`${errorMessage}, image name not found`);
                }
                return;
            }
            Commands.imageNames.push(newCellName);
            cellName = newCellName;
            const newInstanceName = await CommandsUtils.getNewInstanceName();
            if (newInstanceName === undefined || !CommandsUtils.validateInstanceName(newInstanceName)) {
                if (newInstanceName === undefined) {
                    vscode.window.showErrorMessage(`${errorMessage}, instance name not found`);
                }
                return;
            }
            Commands.instanceNames.push(newInstanceName);
            instanceName = newInstanceName;
        } else {
            instanceName = await vscode.window.showQuickPick(Commands.instanceNames, {
                placeHolder: `instance-name`,
            });
            if (instanceName === Commands.instanceNames[0]) {
                const newInstanceName = await CommandsUtils.getNewInstanceName();
                if (newInstanceName === undefined || !CommandsUtils.validateInstanceName(newInstanceName)) {
                    if (newInstanceName === undefined) {
                        vscode.window.showErrorMessage(`${errorMessage}, instance name not found`);
                    }
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
    }

    /**
     * cellery test command handler used by the function 'registerCommand'
     */
    private static readonly handleTestCommand = async() => {
        const errorMessage: string = "Unable to run cellery test";
        let cellName = await vscode.window.showQuickPick(Commands.imageNames, {
            placeHolder: `${Constants.ORG_NAME}/${Constants.IMAGE_NAME}:${Constants.VERSION}`,
        });
        if (cellName === Commands.imageNames[0]) {
            const newCellName = await CommandsUtils.getNewImageName();
            if (newCellName === undefined || !CommandsUtils.validateImageTag(newCellName)) {
                if (newCellName === undefined) {
                    vscode.window.showErrorMessage(`${errorMessage}, image name not found`);
                }
                return;
            }
            Commands.imageNames.push(newCellName);
            cellName = newCellName;
        }
        const file = vscode.window.activeTextEditor ? vscode.window.activeTextEditor.document.fileName : null;
        if (file === null) {
            vscode.window.showErrorMessage(`${errorMessage}, source file not found`);
            return;
        }
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
    }
}

export default Commands;
