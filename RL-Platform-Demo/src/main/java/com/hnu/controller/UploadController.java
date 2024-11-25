package com.hnu.controller;

import com.hnu.pojo.Result;
import com.hnu.service.AlgorithmService;
import com.hnu.service.FileService;
import com.hnu.service.GitService;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jgit.api.Git;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileOutputStream;
import java.io.File;
@Slf4j
@RestController
@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true")
public class UploadController {
    private static final String REPO_PATH = "D:\\Algorithm"; // Git仓库的路径
    @Autowired
    private FileService fileService;

    @Autowired
    private GitService gitService;

    @Autowired
    private AlgorithmService algorithmService;

    @PostMapping("/upload")
    public Result uploadFile(MultipartFile file, String description, String initEnv, String initCommand) {
        System.out.println("接收文件: " + file.getOriginalFilename());
        try {
            // 1. 创建名为 algorithmName 的文件夹
            String folderName = fileService.getFileNameWithoutExtension(file.getOriginalFilename());
            String fileName = file.getOriginalFilename();
            File repoDir = new File(REPO_PATH, folderName);
            if (!repoDir.exists()) {
                repoDir.mkdirs(); // 创建文件夹
            }

            // 2. 将上传的文件保存到该文件内
            File uploadedFile = new File(repoDir, fileName);
            try (FileOutputStream fos = new FileOutputStream(uploadedFile)) {
                fos.write(file.getBytes());
            }

            // 3. 根据文件类型进行解压（如果是压缩文件）
            if (fileName.endsWith(".zip")) {
                fileService.unzipFile(uploadedFile, repoDir);
            } else if (fileName.endsWith(".rar")) {
                fileService.unrarFile(uploadedFile, repoDir);
            } else if (fileName.endsWith(".7z")) {
                fileService.un7zFile(uploadedFile, repoDir);
            } else {
                System.out.println("非压缩文件，不需解压");
            }

            // 4. 删除压缩文件（如果上传的是压缩包）
            if (uploadedFile.exists()) {
                boolean deleted = uploadedFile.delete();
                if (deleted) {
                    System.out.println("压缩包文件已删除: " + uploadedFile.getAbsolutePath());
                } else {
                    System.out.println("删除压缩包文件失败: " + uploadedFile.getAbsolutePath());
                }
            }

            // 5. 初始化或打开Git仓库
            Git git;
            File gitDir = new File(repoDir, ".git"); // .git 文件夹放在 D:\\Algorithm\\DNQN 路径下
            if (!gitDir.exists()) {
                git = Git.init().setDirectory(repoDir).call(); // 在该目录下初始化 Git 仓库
                System.out.println("Git repository initialized at: " + repoDir.getAbsolutePath());
            } else {
                git = Git.open(repoDir);
                System.out.println("Git repository opened at: " + repoDir.getAbsolutePath());
            }

            // 6. 将文件添加到Git索引并提交
            String name = fileService.getFileNameWithoutExtension(fileName);
            git.add().addFilepattern(name).call(); // 添加当前上传的文件
            git.commit().setMessage("Upload and extract file: " + name).call(); // 提交文件
            String commitID = gitService.submitInformation(repoDir.getAbsolutePath());
            algorithmService.uploadAlgorithm(name, commitID, description, initEnv, initCommand);
            System.out.println("文件已成功上传并提交到Git仓库");


            return Result.success(repoDir.getAbsolutePath());

        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("文件上传或解压失败");
        }
    }
}
