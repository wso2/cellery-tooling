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

import { execSync } from "child_process";
import * as crypto from "crypto";
import * as fse from "fs-extra";

/**
 * Common utilities.
 */
class CommonUtils {

    /**
     * Get the Ballerina Home.
     */
    public static getBallerinaHome(): string {
        return execSync("ballerina home").toString().trim();
    }

    /**
     * Get the Cellery Home.
     */
    public static getCelleryHome(): string {
        let celleryHome;
        if (process.platform === "darwin") {
            celleryHome = "/Library/Cellery";
        } else if (process.platform === "linux") {
            celleryHome = "/usr/share/cellery";
        } else {
            throw Error("Failed to detect Cellery Home due to unknown operating system " + process.platform);
        }
        return celleryHome;
    }

    /**
     * Get the checksum of a file.
     *
     * @param filePath The path of the file of which the checksum should be fetched
     * @return The checksum of the file
     */
    public static getFileChecksum(filePath: string): string {
        const fileContent = fse.readFileSync(filePath);
        return crypto.createHash("md5")
            .update(fileContent.toString(), "utf8")
            .digest("hex")
            .toString();
    }
}

export default CommonUtils;
