package datasahi.siyadb.store.s3;

import com.google.gson.annotations.Expose;
import datasahi.siyadb.store.*;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.exception.SdkException;
import software.amazon.awssdk.services.s3.model.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

public class S3FileStore implements FileStore {

    @Expose(serialize = false, deserialize = false)
    private final S3ClientManager s3ClientManager;
    private final S3Config s3Config;

    public S3FileStore(S3Config s3Config) {
        this.s3Config = s3Config;
        if (this.s3Config.getSecurityMode() == S3Config.SecurityMode.ROLE_ARN) {
            this.s3ClientManager = new S3ClientManagerWithRole(s3Config);
        } else {
            this.s3ClientManager = new S3ClientManagerWithKeys(s3Config);
        }
    }

    @Override
    public StoreConfig getConfig() {
        return s3Config;
    }

    public S3Config getS3Config() {
        return s3Config;
    }

    @Override
    public FileListResponse listFiles(String folder) {
        String bucket;
        String prefix;

        int firstSlash = folder.indexOf('/');
        if (firstSlash > 0) {
            bucket = folder.substring(0, firstSlash);
            prefix = folder.substring(firstSlash + 1);
        } else {
            bucket = folder;
            prefix = "";
        }

        FileListResponse fileListResponse = new FileListResponse();

        try {
            ListObjectsV2Request listObjectsRequest = ListObjectsV2Request.builder()
                    .bucket(bucket)
                    .prefix(prefix)
                    .maxKeys(1000)
                    .build();

            ListObjectsV2Response listObjectsResponse;
            String continuationToken = null;

            do {
                if (continuationToken != null) {
                    listObjectsRequest = listObjectsRequest.toBuilder()
                            .continuationToken(continuationToken)
                            .build();
                }

                listObjectsResponse = s3ClientManager.getS3Client().listObjectsV2(listObjectsRequest);
                String file;
                String filePath;

                for (S3Object s3Object : listObjectsResponse.contents()) {
                    if (s3Object.size() != 0) {
                        int lastSlash = s3Object.key().lastIndexOf('/');
                        if (lastSlash > 0) {
                            file = s3Object.key().substring(lastSlash + 1);
                            filePath = bucket + "/" + s3Object.key().substring(0, lastSlash);
                        } else {
                            file = s3Object.key();
                            filePath = bucket;
                        }

                        fileListResponse.addFile(new FileInfo().setPath(filePath).setFilename(file).setSizeInBytes(s3Object.size()));
                    }
                }

                continuationToken = listObjectsResponse.nextContinuationToken();
            } while (listObjectsResponse.isTruncated());

            fileListResponse.setNextMarker(continuationToken);
            return fileListResponse;

        } catch (SdkException e) {
            throw new IllegalStateException("Error in listing files from folder :: " + folder, e);
        }
    }

    //@Override
    public void streamFilesList(String bucket, String prefix, String continuationToken, int filecount, FileListProcessor listProcessor) {
        try {
            ListObjectsV2Request.Builder requestBuilder = ListObjectsV2Request.builder()
                    .bucket(bucket)
                    .prefix(prefix)
                    .maxKeys(filecount);

            if (continuationToken != null) {
                requestBuilder.continuationToken(continuationToken);
            }

            boolean continueProcessing = true;
            String nextToken = continuationToken;

            while (continueProcessing) {
                ListObjectsV2Request request = requestBuilder.build();
                if (nextToken != null) {
                    request = request.toBuilder().continuationToken(nextToken).build();
                }

                ListObjectsV2Response listObjectsResponse = s3ClientManager.getS3Client().listObjectsV2(request);
                FileListResponse fileListResponse = new FileListResponse();
                String file;
                String filePath;

                for (S3Object s3Object : listObjectsResponse.contents()) {
                    if (s3Object.size() != 0) {
                        int lastSlash = s3Object.key().lastIndexOf('/');
                        if (lastSlash > 0) {
                            file = s3Object.key().substring(lastSlash + 1);
                            filePath = bucket + "/" + s3Object.key().substring(0, lastSlash);
                        } else {
                            file = s3Object.key();
                            filePath = bucket;
                        }

                        fileListResponse.addFile(new FileInfo().setPath(filePath).setFilename(file).setSizeInBytes(s3Object.size()));
                    }
                }

                nextToken = listObjectsResponse.nextContinuationToken();
                fileListResponse.setMoreFiles(nextToken != null);
                if (nextToken == null)
                    nextToken = fileListResponse.prepareNextMarker(); // s3 returns null in the token if there are no files to get again
                if (nextToken == null && request.continuationToken() != null)
                    nextToken = request.continuationToken(); // use the previous token if there is no new token
                fileListResponse.setNextMarker(nextToken);

                continueProcessing = listProcessor.processList(fileListResponse);

                if (!continueProcessing || nextToken == null) {
                    break;
                }
            }
        } catch (SdkException e) {
            throw new IllegalStateException("Error in listing files from folder :: " + bucket + "/" + prefix, e);
        }
    }

    @Override
    public FileTransferResponse upload(FileTransferRequest transferRequest) {
        String bucket;
        String key;

        try {
            int firstSlash = transferRequest.getTargetPath().indexOf('/');
            int lastSlash = transferRequest.getSourcePath().lastIndexOf('/');

            if (firstSlash > 0) {
                bucket = transferRequest.getTargetPath().substring(0, firstSlash);
                key = transferRequest.getTargetPath().substring(firstSlash + 1) + "/" + transferRequest.getSourcePath().substring(lastSlash + 1);
            } else {
                bucket = transferRequest.getTargetPath();
                key = transferRequest.getSourcePath().substring(lastSlash + 1);
            }

            PutObjectRequest putObjectRequest = PutObjectRequest.builder().bucket(bucket).key(key).build();

            s3ClientManager.getS3Client().putObject(putObjectRequest,
                    software.amazon.awssdk.core.sync.RequestBody.fromFile(new File(transferRequest.getSourcePath())));

            FileTransferResponse response = new FileTransferResponse();
            response.setRequest(transferRequest);
            return response;

        } catch (SdkException e) {
            throw new IllegalStateException("Error in uploading file :: " + transferRequest, e);
        }
    }

    @Override
    public FileTransferResponse download(FileTransferRequest request) {
        try {
            int firstSlash = request.getSourcePath().indexOf('/');
            int lastSlashLocalFilePath = request.getTargetPath().lastIndexOf('/');
            String bucket = request.getSourcePath().substring(0, firstSlash);
            String key = request.getSourcePath().substring(firstSlash + 1);
            String filePath = request.getTargetPath().substring(0, lastSlashLocalFilePath); //include localDirectory
            File destinationFile = new File(request.getTargetPath());
            try {
                Files.deleteIfExists(destinationFile.toPath());
            } catch (IOException e) {
                // Nothing to do
            }

            // Create parent directories if they don't exist
            if (!destinationFile.getParentFile().exists()) {
                destinationFile.getParentFile().mkdirs();
            }

            GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .build();

            ResponseInputStream<GetObjectResponse> s3ObjectResponse =
                    s3ClientManager.getS3Client().getObject(getObjectRequest);

            long contentLength = 0;
            try (OutputStream outputStream = new FileOutputStream(destinationFile)) {
                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = s3ObjectResponse.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                    contentLength += bytesRead;
                }
            }

            FileTransferResponse transferResponse = new FileTransferResponse();
            transferResponse.setFileInfo(new FileInfo()
                    .setFilename(destinationFile.getName())
                    .setPath(filePath)
                    .setSizeInBytes(contentLength));
            transferResponse.setRequest(request);
            transferResponse.setExists(true);
            return transferResponse;

        } catch (NoSuchKeyException e) {
            FileTransferResponse transferResponse = new FileTransferResponse();
            transferResponse.setRequest(request);
            transferResponse.setExists(false);
            return transferResponse;
        } catch (SdkException | IOException e) {
            throw new IllegalStateException("Error in downloading file :: " + request, e);
        }
    }

    //@Override
    public FileTransferResponse copy(FileTransferRequest request) {
        try {
            int sourceFirstSlash = request.getSourcePath().indexOf('/');
            String sourceBucket = request.getSourcePath().substring(0, sourceFirstSlash);
            String sourceKey = request.getSourcePath().substring(sourceFirstSlash + 1);

            int targetFirstSlash = request.getTargetPath().indexOf('/');
            String targetBucket = request.getTargetPath().substring(0, targetFirstSlash);
            String targetKey = request.getTargetPath().substring(targetFirstSlash + 1);

            CopyObjectRequest copyRequest = CopyObjectRequest.builder()
                    .sourceBucket(sourceBucket)
                    .sourceKey(sourceKey)
                    .destinationBucket(targetBucket)
                    .destinationKey(targetKey)
                    .build();

            CopyObjectResponse copyResult = s3ClientManager.getS3Client().copyObject(copyRequest);

            FileTransferResponse transferResponse = new FileTransferResponse();
            transferResponse.setRequest(request);
            return transferResponse;

        } catch (SdkException e) {
            throw new IllegalStateException("Error in copying file :: " + request, e);
        }
    }

    //@Override
    public boolean createFolder(String path) {
        // Nothing to do for S3
        return true;
    }

    public String getS3FileContent(String remoteFilepath) {
        try {
            String localFile = s3Config.getWorkFolder() + "/" + UUID.randomUUID();
            FileTransferResponse fileDownloadResponse = download(new FileTransferRequest().setSourcePath(remoteFilepath).setTargetPath(localFile));
            if (fileDownloadResponse.isExists()) {
                File file = new File(fileDownloadResponse.getFileInfo().getPath() + "/" + fileDownloadResponse.getFileInfo().getFilename());
                if (file.exists() || Files.exists(Path.of(file.getAbsolutePath()))) {
                    String text = Files.readString(Paths.get(file.getAbsolutePath()));
                    Files.deleteIfExists(Paths.get(localFile));
                    return text;
                }
            }
            return null;
        } catch (IOException | IllegalStateException e) {
            throw new RuntimeException(e);
        }
    }

    public File getS3File(String remoteFilepath) {
        try {
            String localFile = s3Config.getWorkFolder() + "/" + UUID.randomUUID();
            FileTransferResponse fileDownloadResponse = download(new FileTransferRequest().setSourcePath(remoteFilepath).setTargetPath(localFile));
            if (fileDownloadResponse.isExists()) {
                File file = new File(fileDownloadResponse.getFileInfo().getPath() + "/" + fileDownloadResponse.getFileInfo().getFilename());
                if (file.exists() || Files.exists(Path.of(file.getAbsolutePath()))) {
                    return file;
                }
            }
            return null;
        } catch (Exception e) {
            throw new RuntimeException("Unable to download file from :: " + remoteFilepath, e);
        }
    }

    public void moveFile(String sourceBucket, String sourceKey, String destinationBucket, String destinationKey) {
        try {
            // First copy the object
            CopyObjectRequest copyRequest = CopyObjectRequest.builder()
                    .sourceBucket(sourceBucket)
                    .sourceKey(sourceKey)
                    .destinationBucket(destinationBucket)
                    .destinationKey(destinationKey)
                    .build();

            s3ClientManager.getS3Client().copyObject(copyRequest);

            // Then delete the original
            DeleteObjectRequest deleteRequest = DeleteObjectRequest.builder()
                    .bucket(sourceBucket)
                    .key(sourceKey)
                    .build();

            s3ClientManager.getS3Client().deleteObject(deleteRequest);

        } catch (SdkException e) {
            throw new RuntimeException("Error moving file from " + sourceBucket + "/" + sourceKey +
                    " to " + destinationBucket + "/" + destinationKey, e);
        }
    }
}
