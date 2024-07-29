package com.matt.test.workload_identity.controllers;

import com.azure.core.util.Context;
import com.azure.identity.ClientSecretCredential;
import com.azure.identity.ClientSecretCredentialBuilder;
import com.azure.identity.ManagedIdentityCredential;
import com.azure.identity.ManagedIdentityCredentialBuilder;
import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobContainerClientBuilder;
import com.azure.storage.blob.options.BlockBlobSimpleUploadOptions;
import com.azure.storage.blob.specialized.BlockBlobClient;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.time.Duration;
import java.util.Map;

@RestController
public class StorageController {
    static final String containerName = "container1";
    private static final String CLIENT_ID = "580c9d1d-a8d1-4ffa-8d75-610807bc6240";
    private static final String CLIENT_SECRET = "xxx";
    private static final String TENANT_ID = "186a3027-ecbc-40e9-8bd6-2ccdcbc15e61";
    private static final String STORAGE_ENDPOINT = "https://m03storageaccount.blob.core.windows.net";

    private static BlobContainerClient blobContainerClientBuild() {
        //clientSecretCrdential build
//        ClientSecretCredential identityCredential = new ClientSecretCredentialBuilder()
//                .clientId(CLIENT_ID)
//                .clientSecret(CLIENT_SECRET)
//                .tenantId(TENANT_ID)
//                .build();

        ManagedIdentityCredential identityCredential = new ManagedIdentityCredentialBuilder()
                .clientId(CLIENT_ID)
                .build();


        BlobContainerClient blobContainerClient = new BlobContainerClientBuilder()
                .endpoint(STORAGE_ENDPOINT)
                .credential(identityCredential)
                .containerName(containerName)
                .buildClient();
        return blobContainerClient;
    }

    @GetMapping("/hello/{msg}")
    public String hello(@PathVariable("msg") String msg) {
        return "hello " + msg;
    }

    @GetMapping("/{fileName}")
    public String readResource(@PathVariable("fileName") String fileName) throws IOException {
        BlobContainerClient blobContainerClient = blobContainerClientBuild();
        BlobClient blobClient = blobContainerClient.getBlobClient(fileName);
        String fileContent = null;
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            blobClient.downloadStream(outputStream);
            fileContent = StreamUtils.copyToString(outputStream, Charset.defaultCharset());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return fileContent;
    }

    @PostMapping("/{fileName}")
    public String writeResource(@PathVariable("fileName") String fileName, @RequestBody String data) throws IOException {
        BlobContainerClient blobContainerClient = blobContainerClientBuild();

        BlockBlobClient blockBlobClient = blobContainerClient.getBlobClient(fileName).getBlockBlobClient();

        String content = "hello world! " + data;

        try (ByteArrayInputStream dataStream = new ByteArrayInputStream(content.getBytes())) {
            BlockBlobSimpleUploadOptions options = new BlockBlobSimpleUploadOptions(dataStream,
                    content.length()).setMetadata(Map.of("application_id", "123456")).setTags(Map.of("user_id", "hk_user"));
            blockBlobClient.uploadWithResponse(options, Duration.ofSeconds(30L), Context.NONE);
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        return "blob was uploaded";
    }
}
