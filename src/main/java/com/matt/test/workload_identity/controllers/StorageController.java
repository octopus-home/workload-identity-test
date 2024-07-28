package com.matt.test.workload_identity.controllers;

import com.azure.identity.ClientSecretCredential;
import com.azure.identity.ClientSecretCredentialBuilder;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobContainerClientBuilder;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
public class StorageController {
    static final String containerName = "ftpcontainer";
    private static final String CLIENT_ID = "";
    private static final String CLIENT_SECRET = "";
    private static final String TENANT_ID = "";



    @GetMapping("/hello/{msg}")
    public String hello(@PathVariable("msg") String msg) {
        return "hello " + msg;
    }

    @GetMapping("/{fileName}")
    public String readResource(@PathVariable("fileName") String fileName) throws IOException {
        ClientSecretCredential clientSecretCredential = new ClientSecretCredentialBuilder()
                .clientId(CLIENT_ID)
                .clientSecret(CLIENT_SECRET)
                .tenantId(TENANT_ID)
                .build();
        BlobContainerClient blobContainerClient = new BlobContainerClientBuilder()
                .endpoint("<your-storage-account-url>")
                .credential(clientSecretCredential)
                .containerName(containerName)
                .buildClient();
        System.out.println(blobContainerClient);
        return null;
    }

    @PostMapping("/{fileName}")
    public String writeResource(@PathVariable("fileName") String fileName, @RequestBody String data) throws IOException {


        return "blob was uploaded";
    }
}
