package com.hnu.controller;

import com.hnu.pojo.Result;
import com.hnu.service.AlgorithmService;
import com.hnu.utlis.UploadUtlis;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jgit.api.Git;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.File;
@Slf4j
@RestController
public class UploadController {
    private static final String REPO_PATH = "F:\\Java\\RLplatform\\Algorithm_repos"; // Git仓库的路径
    @Autowired
    UploadUtlis uploadUtlis;

    @Autowired
    private AlgorithmService algorithmService;

    @PostMapping("/upload")
    public Result uploadFile(MultipartFile file, String dir) {
        System.out.println("接收文件: " + file.getOriginalFilename());
        try {
            // 1. 创建名为 name 的文件夹
            File repoDir = new File(REPO_PATH, dir);
            if (!repoDir.exists()) {
                repoDir.mkdirs(); // 创建文件夹
            }

            // 2. 将上传的文件保存到指定目录
            File uploadedFile = new File(repoDir, file.getOriginalFilename());
            try (FileOutputStream fos = new FileOutputStream(uploadedFile)) {
                fos.write(file.getBytes());
            }

            // 3. 根据文件类型进行解压
            String fileName = file.getOriginalFilename();
            if (fileName.endsWith(".zip")) {
                uploadUtlis.unzipFile(uploadedFile, repoDir);
            } else if (fileName.endsWith(".rar")) {
                uploadUtlis.unrarFile(uploadedFile, repoDir);
            } else if (fileName.endsWith(".7z")) {
                uploadUtlis.un7zFile(uploadedFile, repoDir);
            } else {
                System.out.println("非压缩文件，不需解压");
            }

            // 4. 初始化或打开Git仓库
            Git git;
            if (!repoDir.getParentFile().exists()) {
                git = Git.init().setDirectory(repoDir.getParentFile()).call();
            } else {
                try {
                    git = Git.open(repoDir.getParentFile());
                } catch (IOException e) {
                    git = Git.init().setDirectory(repoDir.getParentFile()).call();
                }
            }

            // 5. 将文件添加到Git索引并提交
            git.add().addFilepattern(dir).call();
            git.commit().setMessage("Upload and extract file to folder: " + dir+':'+file.getOriginalFilename()).call();
            String name = uploadUtlis.getFileNameWithoutExtension(file.getOriginalFilename());
            String commitID = uploadUtlis.submitInformation(REPO_PATH);
            algorithmService.uploadAlgorithm(name,dir,commitID);
            System.out.println("文件已成功上传并提交到Git仓库");

            // 6. 删除压缩文件
            if (uploadedFile.exists()) {
                boolean deleted = uploadedFile.delete();
                if (deleted) {
                    System.out.println("压缩包文件已删除: " + uploadedFile.getAbsolutePath());
                } else {
                    System.out.println("删除压缩包文件失败: " + uploadedFile.getAbsolutePath());
                }
            }

            return Result.success(repoDir.getAbsolutePath());

        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("文件上传或解压失败");
        }
    }
}
