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

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import io.cellery.tooling.ballerina.langserver.plugins.Constants;
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
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Cellery Image Manager for fetching and extracting information from Images.
 */
public class ImageManager {
    private static final Logger logger = LoggerFactory.getLogger(ImageManager.class);
    private static final Executor executor = Executors.newWorkStealingPool();
    private static final ImageManager instance = new ImageManager();

    private final Map<String, Image> images = new HashMap<>();

    public static ImageManager getInstance() {
        return instance;
    }

    /**
     * Get a Cellery image.
     *
     * If the image does not exist in the local repository it will be automatically pulled.
     *
     * @param orgName The name of the organization the image belongs to
     * @param imageName The name of the image
     * @param version The version of the image
     * @return The image from which information had be extracted
     */
    public synchronized Image getImage(String orgName, String imageName, String version) {
        String imageFQN = getImageFQN(orgName, imageName, version);
        Image image = images.get(imageFQN);

        // Ensuring that the actual image and the last image of which information was collected is equal
        if (image == null) {
            File imageFile = new File(Constants.LOCAL_REPO_DIRECTORY + File.separator + orgName
                    + File.separator + imageName + File.separator + version + File.separator
                    + imageName + Constants.CELLERY_IMAGE_EXTENSION);
            if (imageFile.exists()) {
                image = initializeImageFromLocalRepo(orgName, imageName, version);
            } else {
                pullImage(orgName, imageName, version);
            }
        } else if (image.getCurrentDigest() == null) {  // Previous image had been deleted
            images.remove(imageFQN);
            pullImage(orgName, imageName, version);
        } else if (!Arrays.equals(image.getCurrentDigest(), image.getLastKnownDigest())) {
            image.extractInformation();
        }
        return image;
    }

    /**
     * Get the images collections currently kept in memory.
     *
     * @return The images collection.
     */
    public synchronized Collection<Image> getImages() {
        syncWithLocalRepo();
        return images.values();
    }

    /**
     * Start a thread which periodically checks the local repository for changes.
     */
    private void syncWithLocalRepo() {
        for (Map.Entry<String, Image> imageEntry : images.entrySet()) {
            if (imageEntry.getValue().getCurrentDigest() == null) {
                images.remove(imageEntry.getKey());
            }
        }

        File localRepo = new File(Constants.LOCAL_REPO_DIRECTORY);
        File[] orgDirectories = localRepo.listFiles();
        if (orgDirectories != null) {
            // Looping organizations
            for (File orgDirectory : orgDirectories) {
                File[] imageDirectories = orgDirectory.listFiles();
                if (imageDirectories != null) {
                    // Looping images in an organization
                    for (File imageDirectory : imageDirectories) {
                        File[] versionDirectories = imageDirectory.listFiles();
                        if (versionDirectories != null) {
                            // Looping versions in a image
                            for (File versionDirectory : versionDirectories) {
                                if (versionDirectory.exists() && versionDirectory.isDirectory()) {
                                    executor.execute(() -> {
                                        String org = orgDirectory.getName();
                                        String name = imageDirectory.getName();
                                        String version = versionDirectory.getName();

                                        synchronized (this) {
                                            Image image = images.get(getImageFQN(org, name, version));
                                            if (image == null) {
                                                initializeImageFromLocalRepo(org, name, version);
                                            } else if (!Arrays.equals(image.getCurrentDigest(),
                                                    image.getLastKnownDigest())) {
                                                image.extractInformation();
                                            }
                                        }
                                    });
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Pull Image from remote registry.
     *
     * @param orgName The name of the organization the image belongs to
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
                    initializeImageFromLocalRepo(orgName, imageName, version);
                } else {
                    logger.error("Failed to pull Cellery Image with exit code " + exitCode);
                }
            } catch (IOException | InterruptedException e) {
                logger.error("Failed to fetch image " + getImageFQN(orgName, imageName, version), e);
            }
        });
    }

    /**
     * Initialize an image in the local repo.
     *
     * Relevant information will be extracted from the image as well.
     *
     * @param orgName The name of the organization the image belongs to
     * @param imageName The name of the image
     * @param version The version of the image
     * @return The image from from the local repository with information extracted
     */
    private synchronized Image initializeImageFromLocalRepo(String orgName, String imageName, String version) {
        Image image = new Image(orgName, imageName, version);
        image.extractInformation();
        images.put(getImageFQN(orgName, imageName, version), image);
        return image;
    }

    /**
     * Get the fully qualified name of an image
     *
     * @param orgName The name of the organization the image belongs to
     * @param imageName The name of the image
     * @param version The version of the image
     * @return Image fully qualified name
     */
    private static String getImageFQN(String orgName, String imageName, String version) {
        return orgName + "/" + imageName + ":" + version;
    }

    /**
     * Represents a Cellery image.
     */
    public static class Image {
        private static final Logger logger = LoggerFactory.getLogger(Image.class);
        private static final Gson gson = new Gson();
        private static final Type referenceTypeToken = new ReferenceTypeToken().getType();

        private String org;
        private String name;
        private String version;
        private File imageFile;
        private byte[] lastKnownDigest;
        private Map<String, String> referenceKeys;
        private Metadata metadata;

        private Image(String orgName, String imageName, String version) {
            this.org = orgName;
            this.name = imageName;
            this.version = version;
            this.lastKnownDigest = new byte[0];
            this.imageFile = new File(Constants.LOCAL_REPO_DIRECTORY + File.separator + orgName
                    + File.separator + imageName + File.separator + version + File.separator
                    + imageName + Constants.CELLERY_IMAGE_EXTENSION);
        }

        public synchronized Map<String, String> getReferenceKeys() {
            return referenceKeys;
        }

        public synchronized Metadata getMetadata() {
            return metadata;
        }

        public String getOrg() {
            return org;
        }

        public String getName() {
            return name;
        }

        public String getVersion() {
            return version;
        }

        public String getFQN() {
            return ImageManager.getImageFQN(org, name, version);
        }

        private synchronized byte[] getLastKnownDigest() {
            return lastKnownDigest;
        }

        /**
         * Get the digest of an Cell Image file.
         *
         * @return The digest of the Cellery Image
         */
        private byte[] getCurrentDigest() {
            byte[] digest = null;
            if (imageFile.exists()) {
                try {
                    MessageDigest md5 = MessageDigest.getInstance("MD5");
                    digest = md5.digest(IOUtils.toByteArray(new FileInputStream(imageFile)));
                } catch (IOException | NoSuchAlgorithmException e) {
                    digest = null;
                }
            }
            return digest;
        }

        /**
         * Extract information from the image.
         */
        private synchronized void extractInformation() {
            try (ZipFile celleryImageZip = new ZipFile(imageFile)) {
                // Reading reference data
                ZipEntry referenceJsonZipEntry = celleryImageZip.getEntry(Constants.CELLERY_IMAGE_REFERENCE_FILE);
                String referenceJsonString = IOUtils.toString(celleryImageZip.getInputStream(referenceJsonZipEntry),
                        StandardCharsets.UTF_8);
                referenceKeys = gson.fromJson(referenceJsonString, referenceTypeToken);

                // Reading metadata
                ZipEntry metadataJsonZipEntry = celleryImageZip.getEntry(Constants.CELLERY_IMAGE_METADATA_FILE);
                String metadataJsonString = IOUtils.toString(celleryImageZip.getInputStream(metadataJsonZipEntry),
                        StandardCharsets.UTF_8);
                metadata = gson.fromJson(metadataJsonString, Metadata.class);

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
