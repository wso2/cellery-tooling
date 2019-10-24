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

import * as fse from "fs-extra";
import * as path from "path";
import Constants from "../src/constants";
import CommonUtils from "./CommonUtils";

class BalLangServerUtils {

    /**
     * Copy the bundled Ballerina lang server plugins Jar to the lang server directory.
     *
     * @param extensionPath The path in which the extension was installed
     */
    public static installLangPlugins(extensionPath: string) {
        const langPluginsJar = path.resolve(extensionPath, "resources", Constants.LANG_SERVER_PLUGINS_JAR);
        const langPluginsJarExists = fse.existsSync(langPluginsJar);

        if (langPluginsJarExists) {
            const targetLangPluginsJar = path.resolve(CommonUtils.getCelleryHome(), Constants.CELLERY_TOOLING_DIR,
                                                      Constants.LANG_SERVER_PLUGINS_JAR);
            console.log("Installing Lang Server plugins into the Cellery Home tooling directory");
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
        const targetLangPluginsJar = path.resolve(CommonUtils.getCelleryHome(), Constants.CELLERY_TOOLING_DIR,
                                                  Constants.LANG_SERVER_PLUGINS_JAR);
        const targetLangPluginsJarExists = fse.existsSync(targetLangPluginsJar);
        if (targetLangPluginsJarExists) {
            console.log("Removing Lang Server plugins from Cellery Home tooling directory");
            fse.unlinkSync(targetLangPluginsJar);
        } else {
            console.log("Unable cleanup Ballerina lang server plugins since the Jar was not found");
        }
    }

    /**
     * Check whether the lang plugins Jar is present.
     *
     * @param extensionPath The path in which the extension was installed
     * @return True if lang plugins were already installed
     */
    public static isLangPluginsAlreadyInstalled(extensionPath: string): boolean {
        const targetLangPluginsJar = path.resolve(CommonUtils.getCelleryHome(), Constants.CELLERY_TOOLING_DIR,
                                                  Constants.LANG_SERVER_PLUGINS_JAR);
        const targetLangPluginsJarExists = fse.existsSync(targetLangPluginsJar);
        if (targetLangPluginsJarExists) {
            const langPluginsJar = path.resolve(extensionPath, "resources", Constants.LANG_SERVER_PLUGINS_JAR);
            const langPluginsJarExists = fse.existsSync(langPluginsJar);
            if (langPluginsJarExists) {
                return CommonUtils.getFileChecksum(langPluginsJar)
                    === CommonUtils.getFileChecksum(targetLangPluginsJar);
            } else {
                throw Error("Unable to validate the installed lang server plugins " +
                    "as extension doesn't contain the plugins");
            }
        } else {
            return false;
        }
    }

    /**
     * Check if the symlink from the ballerina lang server plugins directory to the Cellery plugins
     * directory is present.
     */
    public static isSymlinkPresent() {
        const symlink = path.resolve(CommonUtils.getBallerinaHome(), Constants.BAL_LANG_SERVER_PLUGINS_DIR,
                                     Constants.LANG_SERVER_PLUGINS_JAR);
        const expectedSymlinkTarget = path.resolve(CommonUtils.getCelleryHome(), Constants.CELLERY_TOOLING_DIR,
            Constants.LANG_SERVER_PLUGINS_JAR);
        let symlinkExists;
        try {
            const symlinkStats = fse.lstatSync(symlink);
            symlinkExists = symlinkStats.isSymbolicLink() && fse.readlinkSync(symlink) === expectedSymlinkTarget;
        } catch (err) {
            if (err.code === "ENOENT") {
                symlinkExists = false;
            } else {
                throw err;
            }
        }
        return symlinkExists;
    }
}

export default BalLangServerUtils;
