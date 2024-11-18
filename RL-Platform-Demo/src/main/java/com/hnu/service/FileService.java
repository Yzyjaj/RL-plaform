package com.hnu.service;

import org.apache.commons.compress.archivers.ArchiveException;
import org.springframework.http.ResponseEntity;

import java.io.File;
import java.io.IOException;

public interface FileService {
    void unzipFile(File file, File destDir) throws IOException;
    void unrarFile(File file, File destDir) throws IOException;
    void un7zFile(File file, File destDir) throws IOException, ArchiveException;
    void compressDirectory(String name);
    ResponseEntity<byte[]> downloadFile(String name);
    String getFileNameWithoutExtension(String zipname);
}
