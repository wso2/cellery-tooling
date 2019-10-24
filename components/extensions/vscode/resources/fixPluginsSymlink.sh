#!/usr/bin/env bash
# ------------------------------------------------------------------------
#
# Copyright 2019 WSO2, Inc. (http://wso2.com)
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
# http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License
#
# ------------------------------------------------------------------------

#
# This script deals with creating a symlink from the Ballerina Language Server plugins directory to the Cellery
# tooling directory with necessary permissions. This is done to ensure that the VS Code extension is able to copy
# it's Jars accordingly
#

echo
echo "Fixing Cellery Tooling Plugin Links"
echo

BAL_HOME="$(ballerina home | tr -d '[:space:]')"

if [[ "$OSTYPE" == "linux-gnu" ]]; then
    # Linux
    CELLERY_HOME="/usr/share/cellery"
elif [[ "$OSTYPE" == "darwin"* ]]; then
    # MacOS
    CELLERY_HOME="/Library/Cellery"
else
    echo "Failed to fix Tooling links due to unknown OS Type ${OSTYPE}"
    exit 1
fi

PLUGINS_JAR="io.cellery.tooling.ballerina.langserver.plugins.jar"
BAL_LANG_SERVER_PLUGINS_DIR="${BAL_HOME}/lib/tools/lang-server/lib"
BAL_LANG_SERVER_PLUGINS_JAR="${BAL_LANG_SERVER_PLUGINS_DIR}/${PLUGINS_JAR}"
CELLERY_TOOLING_DIR="${CELLERY_HOME}/tooling"
CELLERY_TOOLING_PLUGINS_JAR="${CELLERY_TOOLING_DIR}/${PLUGINS_JAR}"

# Ensuring that the Cellery Tooling directory is present
if [[ ! -d "${CELLERY_TOOLING_DIR}" ]]; then
    echo "Creating missing Cellery tooling directory"
    sudo mkdir -p "${CELLERY_TOOLING_DIR}"
    sudo chmod 777 "${CELLERY_TOOLING_DIR}"
fi

function createSymlink() {
    echo "Creating symlink from Ballerina Language Server plugins directory to Cellery tooling directory"
    sudo ln -s "${CELLERY_TOOLING_PLUGINS_JAR}" "${BAL_LANG_SERVER_PLUGINS_JAR}"
    echo
    echo "Please restart the IDE for changes to take effect"
}

if [[ -L "${BAL_LANG_SERVER_PLUGINS_JAR}" ]]; then
    if [[ "$(readlink -n ${BAL_LANG_SERVER_PLUGINS_JAR})" == "${CELLERY_TOOLING_PLUGINS_JAR}" ]]; then
        echo "The symlink is already in place and points to the correct plugins"
    else
        echo "Removing invalid symlink for plugins"
        sudo rm ${BAL_LANG_SERVER_PLUGINS_JAR}
        createSymlink
    fi
else
    echo "Symlink not detected for Cellery tooling Ballerina plugins"
    createSymlink
fi

echo
