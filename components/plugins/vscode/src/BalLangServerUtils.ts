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

import * as crypto from "crypto";
import * as fse from "fs-extra";
import * as path from "path";
import Constants from "../src/constants";

class BalLangServerUtils {

    /**
     * Check whether the lang plugins Jar is present.
     *
     * @param extensionPath The path in which the extension was installed
     * @return True if lang plugins were already installed
     */
    public static isLangPluginsAlreadyInstalled(extensionPath: string): boolean {
        const targetLangPluginsJar = path.resolve(Constants.CELLERY_BLANG_SERVER_PLUGINS_DIR,
                                                  Constants.LANG_SERVER_PLUGINS_JAR);
        const targetLangPluginsJarExists = fse.existsSync(targetLangPluginsJar);
        if (targetLangPluginsJarExists) {
            const langPluginsJar = path.resolve(extensionPath, "resources", Constants.LANG_SERVER_PLUGINS_JAR);
            const langPluginsJarExists = fse.existsSync(langPluginsJar);
            if (langPluginsJarExists) {
                return BalLangServerUtils.getFileChecksum(langPluginsJar)
                    === BalLangServerUtils.getFileChecksum(targetLangPluginsJar);
            } else {
                throw Error("Unable to validate the installed lang server plugins " +
                    "as extension doesn't contain the plugins");
            }
        } else {
            return false;
        }
    }

    /**
     * Copy the bundled Ballerina lang server plugins Jar to the lang server directory.
     *
     * @param extensionPath The path in which the extension was installed
     */
    public static installLangPlugins(extensionPath: string) {
        const langPluginsJar = path.resolve(extensionPath, "resources", Constants.LANG_SERVER_PLUGINS_JAR);
        const langPluginsJarExists = fse.existsSync(langPluginsJar);

        if (langPluginsJarExists) {
            const targetLangPluginsJar = path.resolve(Constants.CELLERY_BLANG_SERVER_PLUGINS_DIR,
                                                      Constants.LANG_SERVER_PLUGINS_JAR);
            fse.copyFileSync(langPluginsJar, targetLangPluginsJar);
            fse.chmodSync(targetLangPluginsJar, 0o666);
        } else {
            throw Error("Unable to find Cellery Ballerina lang server plugins Jar " +
                "as extension doesn't contain plugins");
        }
    }

    /**
     * Remove the already installed lang server plugins Jar in the Ballerina lang server libs directory.
     */
    public static uninstallLangPlugins() {
        const targetLangPluginsJar = path.resolve(Constants.CELLERY_BLANG_SERVER_PLUGINS_DIR,
                                                  Constants.LANG_SERVER_PLUGINS_JAR);
        const targetLangPluginsJarExists = fse.existsSync(targetLangPluginsJar);
        if (targetLangPluginsJarExists) {
            fse.unlinkSync(targetLangPluginsJar);
        } else {
            console.log("Unable cleanup Ballerina lang server plugins since the Jar was not found");
        }
    }

    /**
     * Get the checksum of a file.
     *
     * @param filePath The path of the file of which the checksum should be fetched
     * @return The checksum of the file
     */
    private static getFileChecksum(filePath: string): string {
        const fileContent = fse.readFileSync(filePath);
        return crypto.createHash("md5")
            .update(fileContent.toString(), "utf8")
            .digest("hex")
            .toString();
    }
}

export default BalLangServerUtils;
