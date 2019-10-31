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

package io.cellery.tooling.ballerina.langserver.plugins.visitor;

import io.cellery.tooling.ballerina.langserver.plugins.ImageManager;

import java.util.Map;

/**
 * Model class for holding information collected by the Cellery visitors.
 */
public class Component {
    // Component.name
    public static final String NAME_FIELD_NAME = "name";
    // Component.dependencies
    public static final String DEPENDENCIES_FIELD_NAME = "dependencies";
    // Component.dependencies.cells
    public static final String DEPENDENCIES_CELLS_FIELD_NAME = "cells";
    // Component.dependencies.composites
    public static final String DEPENDENCIES_COMPOSITES_FIELD_NAME = "composites";
    // Component.dependencies.(cells|composites)[alias].org
    public static final String DEPENDENCIES_IMAGE_ORG_FIELD_NAME = "org";
    // Component.dependencies.(cells|composites)[alias].name
    public static final String DEPENDENCIES_IMAGE_NAME_FIELD_NAME = "name";
    // Component.dependencies.(cells|composites)[alias].ver
    public static final String DEPENDENCIES_IMAGE_VERSION_FIELD_NAME = "ver";

    private String name;
    private Map<String, ImageManager.Image> dependencies;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Map<String, ImageManager.Image> getDependencies() {
        return dependencies;
    }

    public void setDependencies(Map<String, ImageManager.Image> dependencies) {
        this.dependencies = dependencies;
    }
}
