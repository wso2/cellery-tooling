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
    public static final String CELLERY_GET_REFERENCE_METHOD = "getReference";

    public static final String CELLERY_IMAGE_EXTENSION = ".zip";
    public static final String LOCAL_REPO_DIRECTORY = System.getProperty("user.home") + File.separator + ".cellery"
            + File.separator + "repo";
    public static final String CELLERY_IMAGE_REFERENCE_ZIP_ENTRY = "artifacts/ref/reference.json";
    public static final String CELLERY_IMAGE_METADATA_ZIP_ENTRY = "artifacts/cellery/metadata.json";
    public static final String CELLERY_PULL_COMMAND = "cellery pull %s/%s:%s";

    /**
     * Ballerina types defined by Cellery.
     */
    public static class CelleryTypes {
        public static final String COMPONENT = "Component";
        public static final String DEPENDENCIES = "Dependencies";
        public static final String REFERENCE = "Reference";
        public static final String IMAGE_NAME = "ImageName";
    }

    /**
     * Completion Providers related constants.
     */
    public static class CompletionProvider {
        private static final String CELLERY_PREFIX = "cellery: ";
        public static final String COMPONENT_SNIPPET_LABEL = CELLERY_PREFIX + "component";
        public static final String CELL_SNIPPET_LABEL = CELLERY_PREFIX + "cell";
        public static final String COMPOSITE_SNIPPET_LABEL = CELLERY_PREFIX + "composite";
        public static final String CELL_BUILD_FUNCTION_LABE = CELLERY_PREFIX + "cell build function";
        public static final String COMPOSITE_BUILD_FUNCTION_LABEL = CELLERY_PREFIX + "composite build function";
        public static final String RUN_FUNCTION_LABEL = CELLERY_PREFIX + "run function";
    }

    /**
     * Completion Type constants.
     */
    public static class CompletionType {
        public static final String CELLERY_REFERENCE_KEY = "Cellery Reference Key";
        public static final String CELLERY_IMAGE = "Cellery Image";
    }

    /**
     * Enum for Image kinds supported by Cellery.
     */
    public enum ImageKind {
        Cell, Composite
    }
}
