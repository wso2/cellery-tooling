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
import Constants from "../constants";

/**
 * Commands related utilities.
 */
class CommandsUtils {

    /**
     * Get new image name from user
     */
    public static getNewImageName(): Thenable<string | undefined> {
        return vscode.window.showInputBox({
            placeHolder: `${Constants.ORG_NAME}/${Constants.IMAGE_NAME}:${Constants.VERSION}`,
            prompt: `Enter the image name`,
        });
    }

    /**
     * Get new instance name from user
     */
    public static getNewInstanceName(): Thenable<string | undefined> {
        return vscode.window.showInputBox({
            value: `${vscode.window.activeTextEditor
                ? path.parse(vscode.window.activeTextEditor.document.fileName).name
                : "my-instance"}`,
            prompt: `Enter the instance name`,
        });
    }
}

export default CommandsUtils;
