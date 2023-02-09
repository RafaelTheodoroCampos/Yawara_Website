package br.com.yawarasolution.service;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;

@Service
public class FirebaseFileService {

    private Storage storage;

    /**
     * When the application is ready, load the firebase.json file and use it to
     * create a storage object.
     * 
     * @param event The event that triggered the listener.
     */
    @EventListener
    public void init(ApplicationReadyEvent event) {
        try {
            ClassPathResource serviceAccount = new ClassPathResource("firebase.json");
            storage = StorageOptions.newBuilder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount.getInputStream()))
                    .setProjectId("yamara-db-image").build().getService();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * It takes a file, generates a name for it, creates a blobId and blobInfo, and
     * then creates the blob
     * in the storage bucket
     * 
     * @param file The file to upload.
     * @return The name of the file that was uploaded.
     */
    public String saveFile(MultipartFile file) throws IOException {
        String imageName = generateFileName(file.getOriginalFilename());
        Map<String, String> map = new HashMap<>();
        map.put("firebaseStorageDownloadTokens", imageName);
        BlobId blobId = BlobId.of("yamara-db-image.appspot.com", imageName);
        BlobInfo blobInfo = BlobInfo.newBuilder(blobId)
                .setMetadata(map)
                .setContentType(file.getContentType())
                .build();
        storage.create(blobInfo, file.getInputStream());
        return imageName;
    }

    /**
     * It generates a random UUID and appends the file extension to it
     * 
     * @param originalFileName The name of the file that was uploaded.
     * @return The file name is being returned.
     */
    private String generateFileName(String originalFileName) {
        return UUID.randomUUID().toString() + "." + getExtension(originalFileName);
    }

    /**
     * It returns the extension of the file name
     * 
     * @param originalFileName The original file name of the file being uploaded.
     * @return The extension of the file name.
     */
    private String getExtension(String originalFileName) {
        return StringUtils.getFilenameExtension(originalFileName);
    }

    /**
     * It takes a file name, extracts the image name from it, and then deletes the
     * image from the storage
     * bucket
     * 
     * @param fileName The full URL of the image.
     * @return The return value is a boolean value.
     */
    public Boolean deletFile(String fileName) {
        int startIndex = fileName.lastIndexOf("/") + 1;
        int endIndex = fileName.lastIndexOf("?");
        String imageName = fileName.substring(startIndex, endIndex);
        return storage.delete("yamara-db-image.appspot.com", imageName);
    }
}
