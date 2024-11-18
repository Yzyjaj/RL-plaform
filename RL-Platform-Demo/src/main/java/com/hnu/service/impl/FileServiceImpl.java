package com.hnu.service.impl;

import com.github.junrar.Archive;
import com.github.junrar.rarfile.FileHeader;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipEntry;

import com.hnu.service.FileService;
import org.apache.commons.compress.archivers.ArchiveException;
import org.apache.commons.compress.archivers.sevenz.SevenZArchiveEntry;
import org.apache.commons.compress.archivers.sevenz.SevenZFile;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.zip.ZipOutputStream;

@Service
public class FileServiceImpl implements FileService {

    private static final String REPO_PATH = "D://Algorithm";

    @Override
    public void unzipFile(File file, File destDir) throws IOException {
        try (ZipInputStream zipIn = new ZipInputStream(Files.newInputStream(file.toPath()))) {
            ZipEntry entry;
            while ((entry = zipIn.getNextEntry()) != null) {
                File outFile = new File(destDir, entry.getName());
                if (entry.isDirectory()) {
                    outFile.mkdirs();
                } else {
                    outFile.getParentFile().mkdirs();
                    try (FileOutputStream fos = new FileOutputStream(outFile)) {
                        fos.write(zipIn.readAllBytes());
                    }
                }
            }
        }
    }

    @Override
    public void unrarFile(File file, File destDir) throws IOException {
        try (Archive archive = new Archive(file)) {
            FileHeader header;
            while ((header = archive.nextFileHeader()) != null) {
                File outFile = new File(destDir, header.getFileNameString().trim());
                if (header.isDirectory()) {
                    outFile.mkdirs();
                } else {
                    outFile.getParentFile().mkdirs();
                    try (FileOutputStream fos = new FileOutputStream(outFile)) {
                        archive.extractFile(header, fos);
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("RAR解压失败");
        }
    }

    @Override
    public void un7zFile(File file, File destDir) throws IOException, ArchiveException {
        try (SevenZFile sevenZFile = new SevenZFile(file)) {
            SevenZArchiveEntry entry;
            byte[] buffer = new byte[8192];
            while ((entry = sevenZFile.getNextEntry()) != null) {
                File outFile = new File(destDir, entry.getName());
                if (entry.isDirectory()) {
                    outFile.mkdirs();
                } else {
                    outFile.getParentFile().mkdirs();
                    try (FileOutputStream fos = new FileOutputStream(outFile)) {
                        int bytesRead;
                        while ((bytesRead = sevenZFile.read(buffer)) != -1) {
                            fos.write(buffer, 0, bytesRead);
                        }
                    }
                }
            }
        }
    }

    @Override
    public void compressDirectory(String name) {
        String sourceDirPath = REPO_PATH + '/' + name;
        String zipFilePath = REPO_PATH + '/' + name + ".zip";
        File sourceDir = new File(sourceDirPath);

        if (!sourceDir.exists() || !sourceDir.isDirectory()) {
            throw new IllegalArgumentException("The source directory does not exist or is not a directory.");
        }

        try (FileOutputStream fileOutputStream = new FileOutputStream(zipFilePath);
             ZipOutputStream zipOutputStream = new ZipOutputStream(fileOutputStream)) {
            Path sourcePath = Paths.get(sourceDirPath);
            Files.walk(sourcePath)
                    .filter(path -> !Files.isDirectory(path))
                    .forEach(path -> {
                        try {
                            String relativePath = sourcePath.relativize(path).toString();
                            zipOutputStream.putNextEntry(new ZipEntry(relativePath));
                            Files.copy(path, zipOutputStream);
                            zipOutputStream.closeEntry();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public ResponseEntity<byte[]> downloadFile(String name) {
        String FILE_PATH = REPO_PATH + '/' + name + ".zip";
        File file = new File(FILE_PATH);

        if (!file.exists()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }

        try {
            byte[] fileBytes = Files.readAllBytes(file.toPath());
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + file.getName())
                    .contentLength(file.length())
                    .body(fileBytes);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @Override
    public String getFileNameWithoutExtension(String zipname) {
        if (zipname != null && zipname.contains(".")) {
            int lastDotIndex = zipname.lastIndexOf(".");
            return zipname.substring(0, lastDotIndex);
        } else {
            return zipname;
        }
    }
}
