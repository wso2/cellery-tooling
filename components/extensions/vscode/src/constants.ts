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

/**
 * VSCode extension related constants.
 */
class Constants {
    public static readonly LANG_SERVER_PLUGINS_JAR = "io.cellery.tooling.ballerina.langserver.plugins.jar";
    public static readonly CELLERY_TOOLING_DIR = "tooling";
    public static readonly BAL_LANG_SERVER_PLUGINS_DIR = "lib/tools/lang-server/lib";
    public static readonly FIX_SYMLINK_SCRIPT = "resources/fixPluginsSymlink.sh";

    /**
     * VSCode commands related constants.
     * This includes all commands which are used by this extension for performing different actions.
     */
    public static readonly commands = class Commands {
        public static readonly WORKBENCH_RELOAD = "workbench.action.reloadWindow";
    };
}

export default Constants;
