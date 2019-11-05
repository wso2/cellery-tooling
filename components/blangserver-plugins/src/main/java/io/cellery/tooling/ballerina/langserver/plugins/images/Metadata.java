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

package io.cellery.tooling.ballerina.langserver.plugins.images;

import java.util.Map;

/**
 * Metadata model.
 */
public class Metadata {
    private String kind;
    private Map<String, ComponentMetadata> components;
    private boolean zeroScalingRequired;
    private boolean autoScalingRequired;

    public String getKind() {
        return kind;
    }

    public void setKind(String kind) {
        this.kind = kind;
    }

    public Map<String, ComponentMetadata> getComponents() {
        return components;
    }

    public void setComponents(Map<String, ComponentMetadata> components) {
        this.components = components;
    }

    public boolean isZeroScalingRequired() {
        return zeroScalingRequired;
    }

    public void setZeroScalingRequired(boolean zeroScalingRequired) {
        this.zeroScalingRequired = zeroScalingRequired;
    }

    public boolean isAutoScalingRequired() {
        return autoScalingRequired;
    }

    public void setAutoScalingRequired(boolean autoScalingRequired) {
        this.autoScalingRequired = autoScalingRequired;
    }
}
