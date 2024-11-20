package com.hnu.service.impl;

import com.github.junrar.Archive;
import com.github.junrar.rarfile.FileHeader;

import java.io.ByteArrayOutputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipEntry;

import com.hnu.service.FileService;
import org.apache.commons.compress.archivers.ArchiveException;
import org.apache.commons.compress.archivers.sevenz.SevenZArchiveEntry;
import org.apache.commons.compress.archivers.sevenz.SevenZFile;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffFormatter;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Repository;
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

    /**
     * 返回文件给前端，并在传输后删除临时文件
     * @param file 临时文件
     * @return ResponseEntity 包含文件的响应体
     */
    @Override
    public ResponseEntity<byte[]> downloadFile(String fileName) {
        // 创建临时文件路径
        File file = new File(REPO_PATH + "/" + fileName); // 确保路径正确

        if (!file.exists()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }

        try {
            // 读取文件内容为字节数组
            byte[] fileBytes = Files.readAllBytes(file.toPath());

            // 创建 ResponseEntity 返回文件内容
            ResponseEntity<byte[]> response = ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)  // 设置文件类型
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + file.getName())  // 设置文件下载头
                    .contentLength(file.length())  // 设置文件大小
                    .body(fileBytes);  // 设置文件内容

            // 删除临时文件
            Files.delete(file.toPath()); // 删除文件

            return response;
        } catch (IOException e) {
            // 处理文件读取或删除错误
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
