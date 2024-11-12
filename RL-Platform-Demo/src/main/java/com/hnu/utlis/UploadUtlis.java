package com.hnu.utlis;


import com.github.junrar.Archive;
import com.github.junrar.rarfile.FileHeader;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipEntry;
import org.apache.commons.compress.archivers.ArchiveException;
import org.apache.commons.compress.archivers.sevenz.SevenZArchiveEntry;
import org.apache.commons.compress.archivers.sevenz.SevenZFile;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.treewalk.AbstractTreeIterator;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.eclipse.jgit.treewalk.filter.PathFilter;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.zip.ZipOutputStream;

@Component
public class UploadUtlis {
    private static String REPO_PATH = "F:\\Java\\RLplatform\\Algorithm_repos"; // Git仓库的路径

    // 解压zip文件
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

    // 解压rar文件
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
                    }catch (Exception e){
                        System.out.println("rar解压失败");
                    }
                }
            }
        }catch (Exception e){
            System.out.println("rar解压失败");
        }
    }

    // 解压7z文件
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

    //压缩为zip文件
    public void compressDirectory(String name){
        String sourceDirPath = REPO_PATH+'/'+name;
        String zipFilePath = REPO_PATH+'/'+name+".zip";
        System.out.println(sourceDirPath);
        System.out.println(zipFilePath);
        File sourceDir = new File(sourceDirPath);

        if (!sourceDir.exists() || !sourceDir.isDirectory()) {
            throw new IllegalArgumentException("The source directory does not exist or is not a directory.");
        }

        // 创建 Zip 输出流
        try (
                FileOutputStream fileOutputStream = new FileOutputStream(zipFilePath);
                ZipOutputStream zipOutputStream = new ZipOutputStream(fileOutputStream)
        ) {

            // 递归地添加文件
            Path sourcePath = Paths.get(sourceDirPath);
            Files.walk(sourcePath)
                    .filter(path -> !Files.isDirectory(path))  // 只处理文件，跳过目录
                    .forEach(path -> {
                        try {
                            // 创建每个文件的 ZIP 条目
                            String relativePath = sourcePath.relativize(path).toString();
                            zipOutputStream.putNextEntry(new ZipEntry(relativePath));

                            // 将文件内容写入 ZIP 文件
                            Files.copy(path, zipOutputStream);

                            // 关闭当前条目
                            zipOutputStream.closeEntry();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    });
        } catch (IOException e) {  // 捕获并处理异常
            System.out.println("Error occurred during compression: " + e.getMessage());
            e.printStackTrace();
        }
    }

    //导出该压缩包
    public ResponseEntity<byte[]> downloadFile(String name){
        String FILE_PATH = REPO_PATH+'/'+name+".zip";
        File file = new File(FILE_PATH);

        // 确保文件存在
        if (!file.exists()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(null);  // 文件不存在时，返回404
        }

        try {
            // 读取文件为字节数组
            byte[] fileBytes = Files.readAllBytes(file.toPath());

            // 返回文件内容，设置Content-Disposition为附件下载
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + file.getName())
                    .contentLength(file.length())
                    .body(fileBytes);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(null);  // 读取文件错误时，返回500
        }
    }

    //得到没有后缀的文件名
    public String getFileNameWithoutExtension(String zipname) {
        if (zipname != null && zipname.contains(".")) {
            // 找到文件扩展名的最后一个点的位置
            int lastDotIndex = zipname.lastIndexOf(".");

            // 提取扩展名前的文件名
            return zipname.substring(0, lastDotIndex);
        } else {
            // 如果没有扩展名，返回原始文件名
            return zipname;
        }
    }
    //输出历史提交记录
    public String submitInformation(String REPO_PATH) {
        try {
            // 打开Git仓库
            Git git = Git.open(new File(REPO_PATH));
            RevCommit latestCommit = git.log().setMaxCount(1).call().iterator().next();

            // 获取提交 ID
            String latestCommitId = latestCommit.getId().getName();
            // 获取提交记录
            Iterable<RevCommit> commits = git.log().call();
            for (RevCommit commit : commits) {
                //对ID的简化
                String commitInfo = commit.getId().toString();
                String[] parts = commitInfo.split(" ");
                String commitId = parts[1];  // 提取第二部分，即纯提交 ID

                System.out.println("提交哈希: " + commitId);
                System.out.println("作者: " + commit.getAuthorIdent().getName());
                System.out.println("提交时间: " + commit.getAuthorIdent().getWhen());
                System.out.println("提交信息: " + commit.getFullMessage());
                System.out.println("------------");
            }
            // 获取最新一条提交记录
//            RevCommit latestCommit = git.log().setMaxCount(1).call().iterator().next();

            // 获取提交 ID
//            String latestCommitId = latestCommit.getId().getName();
            return latestCommitId;
        } catch (IOException | GitAPIException e) {
            e.printStackTrace();
            return "error";
        }
    }

    //输出版本差异
    public  void versionComparison(String oldCommitId, String newCommitId) throws Exception {
        // 打开 Git 仓库
        File repoDir = new File("F:\\Java\\RLplatform\\Algorithm_repos\\.git");
        try (Repository repository = new FileRepositoryBuilder()
                .setGitDir(repoDir)
                .build()) {

            // 初始化 Git 实例
            try (Git git = new Git(repository)) {
                // 获取需要比较的两个提交 ID（commit ID）
//                String oldCommitId = "commit_id_1";  // 替换为第一个提交的ID
//                String newCommitId = "commit_id_2";  // 替换为第二个提交的ID

                // 获取提交对象
                AbstractTreeIterator oldTreeParser = prepareTreeParser(repository, oldCommitId);
                AbstractTreeIterator newTreeParser = prepareTreeParser(repository, newCommitId);

                // 调用 diff 命令
                List<DiffEntry> diffEntries = git.diff()
                        .setOldTree(oldTreeParser)
                        .setNewTree(newTreeParser)
                        .call();

                // 输出差异
                try (ByteArrayOutputStream out = new ByteArrayOutputStream();
                     DiffFormatter diffFormatter = new DiffFormatter(out)) {

                    diffFormatter.setRepository(repository);
                    for (DiffEntry entry : diffEntries) {
                        diffFormatter.format(entry);
                        System.out.println(out.toString("UTF-8"));  // 打印出差异信息
                        out.reset();
                    }
                }
            }
        }
    }

    // 准备树解析器
    private static AbstractTreeIterator prepareTreeParser(Repository repository, String objectId) throws IOException {
        try (RevWalk walk = new RevWalk(repository)) {
            RevCommit commit = walk.parseCommit(ObjectId.fromString(objectId));
            RevTree treeCommit = commit.getTree();
            CanonicalTreeParser treeParser = new CanonicalTreeParser();
            try (var reader = repository.newObjectReader()) {
                treeParser.reset(reader, treeCommit);
            }
            walk.dispose();
            return treeParser;
        }
    }


    //导出历史版本
    public  void exportVersion(String commitId,String filePath,String outputPath) {
        String repoPath = "F:\\Java\\RLplatform\\Algorithm_repos"; // Git 仓库路径
//        String commitId = "8e5818e46b613842bd01a3ac593195391ee3c432"; // 指定的提交 ID
//        String filePath = "temp/test3.txt"; // 要查看的文件路径

        try (Repository repository = Git.open(new File(repoPath)).getRepository();
             RevWalk revWalk = new RevWalk(repository)) {

            // 获取指定的提交
            RevCommit commit = revWalk.parseCommit(repository.resolve(commitId));

            // 创建 TreeWalk 实例
            try (TreeWalk treeWalk = new TreeWalk(repository)) {
                treeWalk.addTree(commit.getTree());
                treeWalk.setRecursive(true);
                treeWalk.setFilter(PathFilter.create(filePath));

                // 迭代查找文件或文件夹
                while (treeWalk.next()) {
                    // 检查当前路径是否是文件夹
                    String currentPath = treeWalk.getPathString();
                    File outputFile = new File(outputPath, currentPath);
                    if (outputFile.exists()) {
                        outputFile.delete();  // 删除已存在的文件
                    }

                    // 如果是文件夹，创建对应的输出文件夹
                    if (treeWalk.isSubtree()) {
                        outputFile.mkdirs();  // 创建输出目录
                        treeWalk.enterSubtree();  // 进入子树继续遍历
                    } else {
                        // 如果是文件，获取文件内容并导出
                        byte[] fileContent = repository.open(treeWalk.getObjectId(0)).getBytes();

                        // 确保输出目录存在
                        outputFile.getParentFile().mkdirs();
                        Files.write(outputFile.toPath(), fileContent);  // 写入文件
                        System.out.println("文件已导出到: " + outputFile.getAbsolutePath());
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
