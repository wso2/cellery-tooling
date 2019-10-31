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

package io.cellery.tooling.ballerina.langserver.plugins;

import java.io.File;

/**
 * Cellery Ballerina Language Server related constants.
 */
public class Constants {
    public static final String CELLERY_PACKAGE_ORG_NAME = "celleryio";
    public static final String CELLERY_PACKAGE_NAME = "cellery";
    public static final String CELLERY_COMPONENT_TYPE = "Component";
    public static final String CELLERY_DEPENDENCIES_TYPE = "Dependencies";

    public static final String CELLERY_IMAGE_EXTENSION = ".zip";
    public static final String LOCAL_REPO_DIRECTORY = System.getProperty("user.home") + File.separator + ".cellery"
            + File.separator + "repo";
    public static final String CELLERY_IMAGE_REFERENCE_FILE = "artifacts/ref/reference.json";
    public static final String CELLERY_PULL_COMMAND = "cellery pull %s/%s:%s";

    /**
     * Completion Providers related constants.
     */
    public static class CompletionProvider {
        private static final String CELLERY_PREFIX = "cellery:";
        public static final String COMPONENT_SNIPPET_LABEL = CELLERY_PREFIX + "component";
        public static final String CELL_SNIPPET_LABEL = CELLERY_PREFIX + "cell";
        public static final String COMPOSITE_SNIPPET_LABEL = CELLERY_PREFIX + "composite";
    }
}
