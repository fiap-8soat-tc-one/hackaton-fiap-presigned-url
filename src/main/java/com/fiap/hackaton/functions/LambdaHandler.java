package com.fiap.hackaton.functions;

import com.amazonaws.HttpMethod;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

public class LambdaHandler implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

    private final AmazonS3 s3Client = AmazonS3ClientBuilder.standard().build();
    private static final String BUCKET_NAME = "bucket-fiap-hackaton";
    private static final int EXPIRATION_TIME_IN_MINUTES = 10;
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB

    @Override
    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent request, Context context) {

        try {
            String fileKey = "e-mail" + "/" + UUID.randomUUID();
            URL presignedUrl = s3Client.generatePresignedUrl(
                    BUCKET_NAME, fileKey,
                    new java.util.Date(System.currentTimeMillis() + EXPIRATION_TIME_IN_MINUTES * 60 * 1000),
                    HttpMethod.PUT
            );

            AtomicReference<Map<String, String>> responseBody = new AtomicReference<>(new HashMap<>());
            responseBody.get().put("url", presignedUrl.toString());
            responseBody.get().put("fileKey", fileKey);
            responseBody.get().put("maxFileSize", String.valueOf(MAX_FILE_SIZE));

            return new APIGatewayProxyResponseEvent()
                    .withStatusCode(200)
                    .withBody("{\"url\":\"" + presignedUrl + "\", \"fileKey\":\"" + fileKey + "\", \"maxFileSize\":" + MAX_FILE_SIZE + "}")
                    .withHeaders(Map.of("Content-Type", "application/json"));
        } catch (Exception e) {
            return new APIGatewayProxyResponseEvent()
                    .withStatusCode(500)
                    .withBody("{\"error\":\"" + e.getMessage() + "\"}")
                    .withHeaders(Map.of("Content-Type", "application/json"));
        }
    }
}
