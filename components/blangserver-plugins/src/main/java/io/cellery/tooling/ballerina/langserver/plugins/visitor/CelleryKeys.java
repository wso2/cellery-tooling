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

import io.cellery.tooling.ballerina.langserver.plugins.images.ImageManager.Image;
import org.ballerinalang.langserver.compiler.LSContext;

import java.util.Map;

/**
 * Cellery specific LS context keys.
 */
public class CelleryKeys {

    /*
     * Used for storing information about the the components defined by the user. The key is the variable name while
     * the value contains all the information about the Component. (The information may not be accurate when there
     * are complex control flows present)
     */
    public static final LSContext.Key<Map<String, Component>> COMPONENTS = new LSContext.Key<>();

    /*
     * Used for storing information about the references defined by the user. The keys is the variable name while
     * the value contains information about the Image the reference refers to. (information may not be present when
     * the image is not yet in the local repository because it is still being pulled or it is in a private
     * repository to which we do not have access)
     */
    public static final LSContext.Key<Map<String, Image>> IMAGE_REFERENCES = new LSContext.Key<>();

    private CelleryKeys() {     // Prevent initialization
    }
}
