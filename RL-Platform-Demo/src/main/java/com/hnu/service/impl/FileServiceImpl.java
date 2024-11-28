package com.hnu.service.impl;

import com.github.junrar.Archive;
import com.github.junrar.rarfile.FileHeader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipEntry;

import com.hnu.pojo.Algorithm;
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
    public void compressDirectory(String sourceDirPath, String outputDirPath) {
        System.out.println("sourceDirPath: " + sourceDirPath);
        System.out.println("outputDirPath: " + outputDirPath);
        File sourceDir = new File(sourceDirPath);

        if (!sourceDir.exists()) {
            throw new IllegalArgumentException("The source file or directory does not exist.");
        }

        try (FileOutputStream fileOutputStream = new FileOutputStream(outputDirPath);
             ZipOutputStream zipOutputStream = new ZipOutputStream(fileOutputStream)) {

            if (sourceDir.isDirectory()) {
                // 处理目录
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
            } else if (sourceDir.isFile()) {
                // 处理单个文件
                zipOutputStream.putNextEntry(new ZipEntry(sourceDir.getName()));
                Files.copy(sourceDir.toPath(), zipOutputStream);
                zipOutputStream.closeEntry();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /**
     * 返回文件给前端，并在传输后删除临时文件
     * @param outputPath 压缩文件
     * @return ResponseEntity 包含文件的响应体
     */
    @Override
    public ResponseEntity<byte[]> downloadFile(String outputPath) {
        // 创建临时文件路径
        File file = new File(outputPath); // 确保路径正确

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

    /**
     * 删除文件夹及其内容
     *
     * @param dir 文件夹
     */
    public void deleteDirectory(File dir) {
        File[] files = dir.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    deleteDirectory(file); // 递归删除子文件夹
                } else {
                    file.delete(); // 删除文件
                }
            }
        }
        dir.delete(); // 删除空目录
    }

    // 删除 .pt 文件
    public void deleteModelFile(String modelPath) {
        File modelFile = new File(modelPath);
        if (modelFile.exists()) {
            if (modelFile.delete()) {
                System.out.println("模型文件删除成功: " + modelPath);
            } else {
                System.out.println("删除模型文件失败: " + modelPath);
            }
        } else {
            System.out.println("模型文件不存在: " + modelPath);
        }
    }

    /**
     * 根据算法信息生成模型保存路径。
     * 如果是初始训练，则生成 version1 文件夹；
     * 如果已经存在其他版本，则递增版本号并创建新的文件夹。
     *
     * @param algorithmName 算法名称和。
     * @param environment 算法环境。
     * @return 返回生成的模型保存路径。
     */
    public String generateModelSaveDir(String algorithmName, String environment) {
        // 基础路径
        String baseDir = "D:\\Models\\";

        // 拼接初始路径：D:\Models\{algorithmName}\{environment}
        String modelBaseDir = baseDir + algorithmName + "\\" + environment + "\\";

        // 创建基础文件夹（D:\Models\{algorithmName}\{initEnv}），如果不存在
        File modelDir = new File(modelBaseDir);
        if (!modelDir.exists()) {
            modelDir.mkdirs();
            // 进入 Conda 环境并执行 git init 和 dvc init
            try {
                // 进入 Conda 环境并初始化 Git 和 DVC 仓库
                String condaActivateCommand = "conda activate " + algorithmName;
                String gitInitCommand = "git init";
                String dvcInitCommand = "dvc init";

                // 使用cmd命令执行 Conda激活、Git和DVC初始化
                String[] commands = {
                        "cmd.exe", "/c",
                        condaActivateCommand + " && cd " + modelBaseDir + " && " + gitInitCommand + " && " + dvcInitCommand
                };

                Process process = Runtime.getRuntime().exec(commands);
                process.waitFor(); // 等待命令执行完成

            } catch (IOException | InterruptedException e) {
                e.printStackTrace(); // 捕获异常
            }
        }
        return modelBaseDir;
    }

    @Override
    public String findModelFilePath(String algorithmName, String initEnv) {
        // 构建目标路径
        String directoryPath = String.format("D:\\Models\\%s\\%s", algorithmName, initEnv);

        // 创建目录对象
        File directory = new File(directoryPath);

        // 如果目录不存在，返回空或抛出异常
        if (!directory.exists() || !directory.isDirectory()) {
            throw new IllegalArgumentException("The directory does not exist: " + directoryPath);
        }

        // 查找所有文件
        File[] files = directory.listFiles((dir, name) -> name.endsWith(".pt"));

        // 如果没有找到 .pt 文件，返回空或抛出异常
        if (files == null || files.length == 0) {
            throw new IllegalArgumentException("No .pt file found in directory: " + directoryPath);
        }

        // 假设只会找到一个 .pt 文件，取第一个文件的名字
        File modelFile = files[0];

        // 返回完整路径
        return modelFile.getAbsolutePath();
    }

}
