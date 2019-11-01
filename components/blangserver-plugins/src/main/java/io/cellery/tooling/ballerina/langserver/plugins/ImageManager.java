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

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Cellery Image Manager for fetching and extracting information from Images.
 */
public class ImageManager {
    private static final Executor executor = Executors.newWorkStealingPool();
    private static final Gson gson = new Gson();
    private static final ImageManager instance = new ImageManager();
    private final Map<String, Image> images = new ConcurrentHashMap<>();

    public static ImageManager getInstance() {
        return instance;
    }

    /**
     * Get a Cellery image.
     * If the image does not exist in the local repository it will be automatically pulled.
     *
     * @param orgName The name of the organization the image belongs to
     * @param imageName The name of the image
     * @param version The version of the image
     * @return The image from which information can be extracted
     */
    public Image getImage(String orgName, String imageName, String version) {
        String imageFQN = orgName + "/" + imageName + ":" + version;
        Image image = images.computeIfAbsent(imageFQN, k -> new Image(orgName, imageName, version));

        // Ensuring that the actual image and the last image of which information was collected is equal
        if (!Arrays.equals(image.getLastKnownDigest(), image.getCurrentDigest())) {
            image.extractInformationFromImage();
        }
        return image;
    }

    /**
     * Represents a Cellery image.
     */
    public static class Image {
        private static final Logger logger = LoggerFactory.getLogger(Image.class);
        private static final Type referenceTypeToken = new ReferenceTypeToken().getType();

        private String org;
        private String name;
        private String version;
        private File imageFile;
        private byte[] lastKnownDigest;
        private Map<String, String> referenceKeys;

        private Image(String orgName, String imageName, String version) {
            this.org = orgName;
            this.name = imageName;
            this.version = version;
            this.lastKnownDigest = new byte[0];
            this.imageFile = new File(Constants.LOCAL_REPO_DIRECTORY + File.separator + orgName
                    + File.separator + imageName + File.separator + version + File.separator
                    + imageName + Constants.CELLERY_IMAGE_EXTENSION);

            // Collecting image information (pulling the image from Cellery Hub if necessary)
            if (imageFile.exists()) {
                extractInformationFromImage();
            } else {
                pullImage(orgName, imageName, version);
            }
        }

        public synchronized Map<String, String> getReferenceKeys() {
            return referenceKeys;
        }

        public synchronized byte[] getLastKnownDigest() {
            return lastKnownDigest.clone();
        }

        public String getFQN() {
            return org + "/" + name + ":" + version;
        }

        /**
         * Get the digest of an Cell Image file.
         *
         * @return The digest of the Cellery Image
         */
        private byte[] getCurrentDigest() {
            byte[] digest;
            try {
                MessageDigest md5 = MessageDigest.getInstance("MD5");
                digest = md5.digest(IOUtils.toByteArray(new FileInputStream(imageFile)));
            } catch (IOException | NoSuchAlgorithmException e) {
                digest = new byte[0];
            }
            return digest;
        }

        /**
         * Pull an image from the Registry.
         *
         * @param orgName The name of the organization the image belogns to
         * @param imageName The name of the image
         * @param version The version of the image
         */
        private void pullImage(String orgName, String imageName, String version) {
            executor.execute(() -> {
                try {
                    Process process = Runtime.getRuntime()
                            .exec(String.format(Constants.CELLERY_PULL_COMMAND, orgName, imageName, version));
                    int exitCode = process.waitFor();
                    if (exitCode == 0) {
                        extractInformationFromImage();
                    } else {
                        logger.error("Failed to pull Cellery Image with exit code " + exitCode);
                    }
                } catch (IOException | InterruptedException e) {
                    logger.error("Failed to fetch image " + orgName + "/" + imageName + ":" + version, e);
                }
            });
        }

        /**
         * Extract information from the image.
         */
        private synchronized void extractInformationFromImage() {
            try (ZipFile celleryImageZip = new ZipFile(imageFile)) {
                ZipEntry zipEntry = celleryImageZip.getEntry(Constants.CELLERY_IMAGE_REFERENCE_FILE);
                String referenceJsonString = IOUtils.toString(celleryImageZip.getInputStream(zipEntry),
                        StandardCharsets.UTF_8);
                referenceKeys = gson.fromJson(referenceJsonString, referenceTypeToken);
                lastKnownDigest = getCurrentDigest();
            } catch (IOException e) {
                logger.error("Failed to read Cell Image zip " + imageFile.getAbsolutePath(), e);
            }
        }

        /**
         * Gson Type Token used for de-serializing Reference JSON.
         */
        private static class ReferenceTypeToken extends TypeToken<Map<String, String>> {
        }
    }

    private ImageManager() {    // Prevent initialization
    }
}
