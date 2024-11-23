    package com.hnu.service.impl;

    import com.hnu.service.FileService;
    import com.hnu.service.GitService;
    import org.eclipse.jgit.api.Git;
    import org.eclipse.jgit.api.errors.GitAPIException;
    import org.eclipse.jgit.diff.DiffEntry;
    import org.eclipse.jgit.diff.DiffFormatter;
    import org.eclipse.jgit.lib.ObjectId;
    import org.eclipse.jgit.lib.ObjectReader;
    import org.eclipse.jgit.lib.Repository;
    import org.eclipse.jgit.revwalk.RevCommit;
    import org.eclipse.jgit.revwalk.RevTree;
    import org.eclipse.jgit.revwalk.RevWalk;
    import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
    import org.eclipse.jgit.treewalk.AbstractTreeIterator;
    import org.eclipse.jgit.treewalk.CanonicalTreeParser;

    import org.springframework.beans.factory.annotation.Autowired;
    import org.springframework.stereotype.Service;

    import java.io.*;
    import java.nio.charset.StandardCharsets;
    import java.nio.file.Files;
    import java.util.List;

    @Service
    public class GitServiceImpl implements GitService {

        private static final String REPO_PATH = "D:\\Algorithm";
        @Autowired
        private FileService fileService;




        @Override
        public String submitInformation(String REPO_PATH) {
            try {
                Git git = Git.open(new File(REPO_PATH));
                RevCommit latestCommit = git.log().setMaxCount(1).call().iterator().next();
                return latestCommit.getId().getName();
            } catch (IOException | GitAPIException e) {
                e.printStackTrace();
                return "error";
            }
        }

        @Override
        public String versionComparison(String oldCommitId, String newCommitId, String fileName) throws Exception {
            StringBuilder diffContent = new StringBuilder();
            String comparePath = REPO_PATH + "\\" + fileName;

            try (Repository repository = Git.open(new File(comparePath)).getRepository();
                 Git git = new Git(repository)) {

                // 获取旧提交和新提交的 Tree
                AbstractTreeIterator oldTreeParser = prepareTreeParser(repository, oldCommitId);
                AbstractTreeIterator newTreeParser = prepareTreeParser(repository, newCommitId);

                // 执行 diff 比较
                List<DiffEntry> diffEntries = git.diff()
                        .setOldTree(oldTreeParser)
                        .setNewTree(newTreeParser)
                        .call();

                // 输出每个差异内容
                for (DiffEntry entry : diffEntries) {
                    // 将差异格式化并附加到 diffContent
                    try (ByteArrayOutputStream out = new ByteArrayOutputStream();
                         DiffFormatter formatter = new DiffFormatter(out)) {
                        formatter.setRepository(repository);
                        formatter.format(entry);
                        String diff = out.toString(StandardCharsets.UTF_8);
                        diffContent.append(diff).append(System.lineSeparator());
                    }
                }

            } catch (IOException | GitAPIException e) {
                e.printStackTrace();
                throw new Exception("差异内容生成失败", e);
            }

            return diffContent.toString();
        }



        @Override
        public void exportVersion(String commitId, String filePath, String outputPath) {
            try {
                String exportDir = REPO_PATH + "\\" + filePath;
                File repoDir = new File(exportDir);
                FileRepositoryBuilder builder = new FileRepositoryBuilder();
                Repository repository = builder.setGitDir(new File(repoDir, ".git"))
                        .readEnvironment()
                        .findGitDir()
                        .build();

                try (Git git = new Git(repository)) {
                    // 执行 git checkout 到指定 commitId
                    git.checkout().setName(commitId).call();
                    System.out.println("Checked out to commit: " + commitId);

                }
            } catch (IOException | GitAPIException e) {
                e.printStackTrace();
            }
        }




        // Helper method to prepare the tree parser
        private AbstractTreeIterator prepareTreeParser(Repository repository, String commitId) throws IOException {
            ObjectId commitObjectId = repository.resolve(commitId);
            RevWalk walk = new RevWalk(repository);
            RevCommit commit = walk.parseCommit(commitObjectId);
            RevTree tree = commit.getTree();
            ObjectReader reader = repository.newObjectReader();
            CanonicalTreeParser treeParser = new CanonicalTreeParser();
            treeParser.reset(reader, tree);
            return treeParser;
        }




    }
