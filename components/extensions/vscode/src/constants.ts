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
    public static readonly LANG_PLUGINS_INFO_FILE = "lang-plugins-info.json";
    public static readonly CELLERY_TOOLING_DIR = "tooling";
    public static readonly BAL_LANG_SERVER_PLUGINS_DIR = "lib/tools/lang-server/lib";
    public static readonly FIX_SYMLINK_SCRIPT = "resources/fixPluginsSymlink.sh";

    // VERSION is replaced at build time
    public static readonly CELLERY_TOOLING_VERSION = "{{VERSION}}";

    /**
     * VSCode commands related constants.
     * This includes all commands which are used by this extension for performing different actions.
     */
    public static readonly commands = class Commands {
        public static readonly WORKBENCH_RELOAD = "workbench.action.reloadWindow";
        public static readonly CELLERY_BUILD = "cellery.build";
        public static readonly CELLERY_RUN = "cellery.run";
        public static readonly CELLERY_TEST = "cellery.test";
    };

    // Cellery commands
    public static readonly CELLERY_BUILD_COMMAND = "cellery build";
    public static readonly CELLERY_RUN_COMMAND = "cellery run";
    public static readonly CELLERY_LOGS_COMMAND = "cellery logs";
    public static readonly CELLERY_TEST_COMMAND = "cellery test";

    public static readonly ORG_NAME = "org-name";
    public static readonly IMAGE_NAME = "image-name";
    public static readonly VERSION = "version";

    public static readonly CELLERY_ID_PATTERN = /^[a-z0-9]+(-[a-z0-9]+)*$/;
    public static readonly IMAGE_VERSION_PATTERN = /^[a-z0-9]+((?:-|.)[a-z0-9]+)*$/;

    /**
     * Includes the names of terminals used for running CLI commands
     */
    public static readonly terminals = class Commands {
        public static readonly CELLERY_BUILD = "Cellery Build";
        public static readonly CELLERY_RUN = "Cellery Run";
        public static readonly CELLERY_TEST = "Cellery Test";
    };
}

export default Constants;
