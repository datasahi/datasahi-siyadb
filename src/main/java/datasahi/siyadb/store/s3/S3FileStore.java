package datasahi.siyadb.store.s3;

import com.amazonaws.SdkClientException;
import com.amazonaws.services.s3.model.*;
import com.google.gson.annotations.Expose;
import datasahi.siyadb.store.*;

import java.io.File;
import java.io.IOException;
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

    //@Override
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

        FileListResponse response = new FileListResponse();
        ObjectListing objectListing;
        ListObjectsRequest listObjectsRequest = new ListObjectsRequest()
                .withBucketName(bucket).withPrefix(prefix).withMaxKeys(1000);

        do {
            try {
                objectListing = s3ClientManager.getS3Client().listObjects(bucket, prefix);
                String file;
                String filePath;
                for (S3ObjectSummary objectSummary : objectListing.getObjectSummaries()) {
                    if (objectSummary.getSize() != 0) {
                        int lastSlash = objectSummary.getKey().lastIndexOf('/');
                        if (lastSlash > 0) {
                            file = objectSummary.getKey().substring(lastSlash + 1);
                            filePath = objectSummary.getBucketName() + "/" + objectSummary.getKey().substring(0, lastSlash);
                        } else {
                            file = objectSummary.getKey();
                            filePath = objectSummary.getBucketName();
                        }

                        response.addFile(new FileInfo().setPath(filePath).setFilename(file).setSizeInBytes(objectSummary.getSize()));
                    }
                }
                listObjectsRequest.setMarker(objectListing.getNextMarker());
            } catch (SdkClientException e) {
                throw new IllegalStateException("Error in listing files from folder :: " + folder, e);
            }
        } while (objectListing.isTruncated());

        if (objectListing != null) response.setNextMarker(objectListing.getNextMarker());
        return response;
    }

//    @Override
    public void streamFilesList(String bucket, String prefix, String marker, int filecount, FileListProcessor listProcessor) {

        ListObjectsRequest listObjectsRequest = new ListObjectsRequest()
                .withBucketName(bucket).withPrefix(prefix).withMaxKeys(filecount);
        if (marker != null) {
            listObjectsRequest.setMarker(marker);
        }

        boolean continueProcessing = true;
        ObjectListing objectListing;
        do {
            try {
                objectListing = s3ClientManager.getS3Client().listObjects(listObjectsRequest);
                FileListResponse response = new FileListResponse();
                String file;
                String filePath;
                for (S3ObjectSummary objectSummary : objectListing.getObjectSummaries()) {
                    if (objectSummary.getSize() != 0) {
                        int lastSlash = objectSummary.getKey().lastIndexOf('/');
                        if (lastSlash > 0) {
                            file = objectSummary.getKey().substring(lastSlash + 1);
                            filePath = objectSummary.getBucketName() + "/" + objectSummary.getKey().substring(0, lastSlash);
                        } else {
                            file = objectSummary.getKey();
                            filePath = objectSummary.getBucketName();
                        }

                        response.addFile(new FileInfo().setPath(filePath).setFilename(file).setSizeInBytes(objectSummary.getSize()));
                    }
                }

                String nextMarker = objectListing.getNextMarker();
                response.setMoreFiles(nextMarker != null);
                if (nextMarker == null) nextMarker = response.prepareNextMarker(); // s3 returns null in the marker if there are no files to get again
                if (nextMarker == null) nextMarker = objectListing.getMarker(); // use the previous marker if there is no new marker
                response.setNextMarker(nextMarker);
                listObjectsRequest.setMarker(nextMarker);
                continueProcessing = listProcessor.processList(response);
            } catch (SdkClientException e) {
                throw new IllegalStateException("Error in listing files from folder :: " + bucket + "/" + prefix, e);
            }
        } while (continueProcessing);
    }

//    @Override
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
            PutObjectRequest request = new PutObjectRequest(bucket, key, new File(transferRequest.getSourcePath()));

            s3ClientManager.getS3Client().putObject(request);
            FileTransferResponse response = new FileTransferResponse();
            response.setRequest(transferRequest);
            return response;

        } catch (SdkClientException e) {
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
            ObjectMetadata object = s3ClientManager.getS3Client().getObject(new GetObjectRequest(bucket, key), destinationFile);

            FileTransferResponse transferResponse = new FileTransferResponse();
            transferResponse.setFileInfo(new FileInfo().setFilename(destinationFile.getName()).setPath(filePath)
                    .setSizeInBytes(object.getContentLength()));
            transferResponse.setRequest(request);
            transferResponse.setExists(true);
            return transferResponse;
        } catch (AmazonS3Exception e) {
            if (e.getMessage().startsWith("The specified key does not exist")) {
                FileTransferResponse transferResponse = new FileTransferResponse();
                transferResponse.setRequest(request);
                transferResponse.setExists(false);
                return transferResponse;
            }
            throw new IllegalStateException("Error in downloading file :: " + request, e);
        }
    }

//    @Override
    public FileTransferResponse copy(FileTransferRequest request) {

        try {
            int sourceFirstSlash = request.getSourcePath().indexOf('/');
            String sourceBucket = request.getSourcePath().substring(0, sourceFirstSlash);
            String sourceKey = request.getSourcePath().substring(sourceFirstSlash + 1);

            int targetFirstSlash = request.getTargetPath().indexOf('/');
            String targetBucket = request.getTargetPath().substring(0, targetFirstSlash);
            String targetKey = request.getTargetPath().substring(targetFirstSlash + 1);

            CopyObjectRequest copyRequest = new CopyObjectRequest(sourceBucket, sourceKey, targetBucket, targetKey);
            CopyObjectResult copyResult = s3ClientManager.getS3Client().copyObject(copyRequest);

            FileTransferResponse transferResponse = new FileTransferResponse();
            transferResponse.setRequest(request);
            return transferResponse;
        } catch (SdkClientException e) {
            throw new IllegalStateException("Error in copying file :: " + request, e);
        }
    }

//    @Override
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
            throw new RuntimeException("Uanble to download file from :: " + remoteFilepath, e );
        }
    }

    public void moveFile(String sourceBucket, String sourceKey, String destinationBucket, String destinationKey) {

        try {
            CopyObjectResult result = s3ClientManager.getS3Client().copyObject(sourceBucket, sourceKey, destinationBucket, destinationKey);
            s3ClientManager.getS3Client().deleteObject(sourceBucket, sourceKey);
        } catch (SdkClientException e) {
            throw new RuntimeException(e);
        }
    }
}
