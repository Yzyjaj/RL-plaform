package com.hnu.service;

import com.hnu.pojo.Algorithm;
import org.apache.commons.compress.archivers.ArchiveException;
import org.springframework.http.ResponseEntity;
import java.io.File;
import java.io.IOException;

public interface FileService {
    void unzipFile(File file, File destDir) throws IOException;
    void unrarFile(File file, File destDir) throws IOException;
    void un7zFile(File file, File destDir) throws IOException, ArchiveException;
    void compressDirectory(String sourceDirPath, String outputDirPath);
    void deleteDirectory(File dir);
    ResponseEntity<byte[]> downloadFile(String name);
    String getFileNameWithoutExtension(String zipname);
    String generateModelSaveDir(String algorithmName, String environment);
    String findModelFilePath(String algorithmName, String initEnv, Integer initVersion);
}
