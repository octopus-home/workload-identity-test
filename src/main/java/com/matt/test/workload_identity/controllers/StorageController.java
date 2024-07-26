package com.matt.test.workload_identity.controllers;

import com.azure.core.util.BinaryData;
import com.azure.core.util.Context;
import com.azure.spring.cloud.core.resource.AzureStorageBlobProtocolResolver;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.options.BlockBlobSimpleUploadOptions;
import com.azure.storage.blob.specialized.BlockBlobClient;
import com.matt.test.workload_identity.service.StorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.WritableResource;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.time.Duration;
import java.util.Map;

@RestController
public class StorageController {
    static final String BLOB_RESOURCE_PATTERN = "azure-blob://%s/%s";
    static final String containerName = "ftpcontainer";

    @Autowired
    private ResourceLoader resourceLoader;
    @Autowired
    private AzureStorageBlobProtocolResolver azureStorageBlobProtocolResolver;
    @Autowired
    private BlobServiceClient blobServiceClient;
    @Autowired
    private StorageService service;

    @GetMapping("/hello/{msg}")
    public String hello(@PathVariable("msg") String msg) {
        return "hello " + msg;
    }

    @GetMapping("/{fileName}")
    public String readResource(@PathVariable("fileName") String fileName) throws IOException {
        Resource resource = resourceLoader.getResource(String.format(BLOB_RESOURCE_PATTERN, containerName, fileName));
        return StreamUtils.copyToString(resource.getInputStream(), Charset.defaultCharset());
    }

    @PostMapping("/{fileName}")
    public String writeResource(@PathVariable("fileName") String fileName, @RequestBody String data) throws IOException {

//        Resource resource = resourceLoader.getResource(String.format(BLOB_RESOURCE_PATTERN, this.containerName, fileName));
//        try (OutputStream os = ((WritableResource) resource).getOutputStream()) {
//            os.write(data.getBytes());
//        }
        BlobContainerClient blobContainerClient = blobServiceClient.getBlobContainerClient(containerName);
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
