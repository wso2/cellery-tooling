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

    /**
     * validates the image tag
     */
    public static validateImageTag(cellName: string): boolean {
        const regexp = /^([^/:]*)\/([^/:]*):([^/:]*)$/;
        const subMatch = regexp.exec(cellName);
        if (subMatch === null) {
            vscode.window.showErrorMessage(`expects <organization>/<cell-image>:<version> as the tag, received ${cellName}`);
            return false;
        }
        const organization = subMatch[1];
        if (!(Constants.CELLERY_ID_PATTERN.test(organization))) {
            vscode.window.showErrorMessage(`"expects a valid organization name (lower case letters, numbers and dashes "+
			"with only letters and numbers at the begining and end), received ${organization}`);
            return false;
        }
        const imageName = subMatch[2];
        if (!(Constants.CELLERY_ID_PATTERN.test(imageName))) {
            vscode.window.showErrorMessage(`expects a valid image name (lower case letters, numbers and dashes "+
			"with only letters and numbers at the begining and end), received ${imageName}`);
            return false;
        }
        const imageVersion = subMatch[3];
        if (!(Constants.IMAGE_VERSION_PATTERN.test(imageVersion))) {
            vscode.window.showErrorMessage(`expects a valid image version (lower case letters, numbers, dashes and dots "+
			"with only letters and numbers at the begining and end), received ${imageVersion}`);
            return false;
        }
        return true;
    }

    /**
     * validates the instance name
     */
    public static validateInstanceName(instanceName: string): boolean {
        if (!(Constants.CELLERY_ID_PATTERN.test(instanceName))) {
            vscode.window.showErrorMessage(`expects a valid instance name (lower case letters, numbers, dashes and dots "+
			"with only letters and numbers at the begining and end), received ${instanceName}`);
            return false;
        }
        return true;
    }
}

export default CommandsUtils;
