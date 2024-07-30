package com.matt.test.workload_identity.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.Map;

@RestController
public class StorageController {
    static final String CONTAINER_NAME = "container1";
    private static final String CLIENT_ID = "580c9d1d-a8d1-4ffa-8d75-610807bc6240";
    private static final String CLIENT_SECRET = "xxx";
    private static final String TENANT_ID = "186a3027-ecbc-40e9-8bd6-2ccdcbc15e61";
    private static final String STORAGE_ENDPOINT = "https://m03storageaccount.blob.core.windows.net";

    private ObjectMapper mapper = new ObjectMapper();


    @GetMapping("/hello/{msg}")
    public String hello(@PathVariable("msg") String msg) {
        return "hello " + msg;
    }

    @GetMapping("/credential/{fileName}")
    public String readResource(@PathVariable("fileName") String fileName) throws IOException, InterruptedException, NoSuchAlgorithmException, KeyManagementException {
        String access_token = getAccessToken();

        SSLContext sslContext = ignoreSSLContext();
        HttpClient blobClient = HttpClient.newBuilder().sslContext(sslContext).build();

        HttpRequest getRequest = HttpRequest.newBuilder().uri(URI.create(STORAGE_ENDPOINT + "/" + CONTAINER_NAME + "/" + fileName))
                .header("x-ms-version", "2024-08-04")
                .header("Authorization", "Bearer " + access_token)
                .GET()
                .build();
        String blob_content = blobClient.send(getRequest, HttpResponse.BodyHandlers.ofString()).body();

        return blob_content;
    }

    private String getAccessToken() throws IOException, InterruptedException {
        HttpClient authClient = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create("https://login.microsoftonline.com/" + TENANT_ID
                        + "/oauth2/v2.0/token")).header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(
                        "client_id=" + CLIENT_ID +
                                "&client_secret=" + CLIENT_SECRET +
                                "&grant_type=client_credentials" +
                                "&scope=https://storage.azure.com/.default"
                )).build();
        String tokenResult = authClient.send(request, HttpResponse.BodyHandlers.ofString()).body();
        HashMap mapResult = mapper.readValue(tokenResult, HashMap.class);
        String access_token = (String) mapResult.get("access_token");
        return access_token;
    }

    private static SSLContext ignoreSSLContext() throws NoSuchAlgorithmException, KeyManagementException {
        TrustManager[] trustAllCerts = new TrustManager[]{
                new X509TrustManager() {
                    public X509Certificate[] getAcceptedIssuers() {
                        return new X509Certificate[0];
                    }
                    public void checkClientTrusted(X509Certificate[] certs, String authType) {
                    }
                    public void checkServerTrusted(X509Certificate[] certs, String authType) {
                    }
                }
        };

        SSLContext sslContext = SSLContext.getInstance("SSL");
        sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
        return sslContext;
    }

    @PostMapping("/credential/{fileName}")
    public Integer writeResource(@PathVariable("fileName") String fileName, @RequestBody String data) throws IOException, InterruptedException, NoSuchAlgorithmException, KeyManagementException {

        String content = "hello world! " + data;

        String access_token = getAccessToken();

        SSLContext sslContext = ignoreSSLContext();
        HttpClient blobClient = HttpClient.newBuilder().sslContext(sslContext).build();

        HttpRequest putRequest = HttpRequest.newBuilder().uri(URI.create(STORAGE_ENDPOINT + "/" + CONTAINER_NAME + "/" + fileName))
                .header("x-ms-version", "2024-08-04")
                .header("Authorization", "Bearer " + access_token)
                .header("x-ms-blob-type", "BlockBlob" )
                .header("x-ms-tags", "user_id=hk_user")
                .header("x-ms-meta-doc_index", "application_id:123456")
                .PUT(HttpRequest.BodyPublishers.ofString(content))
                .build();

        HttpResponse<String> response = blobClient.send(putRequest, HttpResponse.BodyHandlers.ofString());

        return response.statusCode();
    }


    @GetMapping("/workload/{fileName}")
    public String download(@PathVariable("fileName") String fileName) throws IOException, InterruptedException {

        Map<String, String> env = System.getenv();
        String clientAssertion;
        try {
            clientAssertion = new String(Files.readAllBytes(Paths.get(env.get("AZURE_FEDERATED_TOKEN_FILE"))),
                    StandardCharsets.UTF_8);
        } catch (Exception e) {
            System.out.printf("Error creating client application: %s", e.getMessage());
            throw new RuntimeException(e);
        }
        HttpClient authClient = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create("https://login.microsoftonline.com/" + TENANT_ID
                        + "/oauth2/v2.0/token")).header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(
                        "client_id=" + CLIENT_ID +
                                "&client_assertion_type=" + "urn:ietf:params:oauth:client-assertion-type:jwt-bearer" +
                                "&grant_type=client_credentials" +
                                "&scope=https://storage.azure.com/.default"+
                                "&client_assertion=" + clientAssertion
                )).build();
        String tokenResult = authClient.send(request, HttpResponse.BodyHandlers.ofString()).body();
        HashMap mapResult = mapper.readValue(tokenResult, HashMap.class);
        String access_token = (String) mapResult.get("access_token");

        HttpClient blobClient = HttpClient.newHttpClient();
        HttpRequest getRequest = HttpRequest.newBuilder().uri(URI.create(STORAGE_ENDPOINT + "/" + CONTAINER_NAME + "/" + fileName))
                .header("x-ms-version", "2024-08-04")
                .header("Authorization", "Bearer " + access_token)
                .GET()
                .build();
        String blob_content = blobClient.send(getRequest, HttpResponse.BodyHandlers.ofString()).body();

        return blob_content;
    }

    @PostMapping("/workload/{fileName}")
    public Integer upload(@PathVariable("fileName") String fileName, @RequestBody String data) throws IOException, InterruptedException {

        String content = "hello world! " + data;

        Map<String, String> env = System.getenv();
        String clientAssertion;
        try {
            clientAssertion = new String(Files.readAllBytes(Paths.get(env.get("AZURE_FEDERATED_TOKEN_FILE"))),
                    StandardCharsets.UTF_8);
        } catch (Exception e) {
            System.out.printf("Error creating client application: %s", e.getMessage());
            throw new RuntimeException(e);
        }

        HttpClient authClient = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create("https://login.microsoftonline.com/" + TENANT_ID
                        + "/oauth2/v2.0/token")).header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(
                        "client_id=" + CLIENT_ID +
                                "&client_assertion_type=" + "urn:ietf:params:oauth:client-assertion-type:jwt-bearer" +
                                "&grant_type=client_credentials" +
                                "&scope=https://storage.azure.com/.default"+
                                "&client_assertion=" + clientAssertion
                )).build();
        String tokenResult = authClient.send(request, HttpResponse.BodyHandlers.ofString()).body();
        HashMap mapResult = mapper.readValue(tokenResult, HashMap.class);
        String access_token = (String) mapResult.get("access_token");

        HttpClient blobClient = HttpClient.newHttpClient();

        HttpRequest putRequest = HttpRequest.newBuilder().uri(URI.create(STORAGE_ENDPOINT + "/" + CONTAINER_NAME + "/" + fileName))
                .header("x-ms-version", "2024-08-04")
                .header("Authorization", "Bearer " + access_token)
                .header("x-ms-blob-type", "BlockBlob" )
                .header("x-ms-tags", "user_id=hk_user")
                .header("x-ms-meta-doc_index", "application_id:123456")
                .PUT(HttpRequest.BodyPublishers.ofString(content))
                .build();

        HttpResponse<String> response = blobClient.send(putRequest, HttpResponse.BodyHandlers.ofString());

        return response.statusCode();
    }
}
