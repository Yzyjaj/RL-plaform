    package com.hnu.service.impl;

    import com.hnu.service.FileService;
    import com.hnu.service.GitService;
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
    import org.springframework.beans.factory.annotation.Autowired;
    import org.springframework.stereotype.Service;

    import java.io.*;
    import java.nio.charset.StandardCharsets;
    import java.nio.file.Files;
    import java.util.List;

    @Service
    public class GitServiceImpl implements GitService {

        private static final String REPO_PATH = "D://Algorithm";
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
        public String versionComparison(String oldCommitId, String newCommitId) throws Exception {
            StringBuilder diffContent = new StringBuilder();

            try {
                try (Repository repository = Git.open(new File(REPO_PATH)).getRepository();
                     Git git = new Git(repository);
                     ByteArrayOutputStream out = new ByteArrayOutputStream();
                     DiffFormatter diffFormatter = new DiffFormatter(out)) {

                    AbstractTreeIterator oldTreeParser = prepareTreeParser(repository, oldCommitId);
                    AbstractTreeIterator newTreeParser = prepareTreeParser(repository, newCommitId);

                    // 设置仓库和启用重命名检测
                    diffFormatter.setRepository(repository);
                    diffFormatter.setDetectRenames(true);


                    List<DiffEntry> diffEntries = git.diff()
                            .setOldTree(oldTreeParser)
                            .setNewTree(newTreeParser)
                            .call();

                    for (DiffEntry entry : diffEntries) {
                        System.out.println("Change Type: " + entry.getChangeType());
                        System.out.println("Old Path: " + entry.getOldPath());
                        System.out.println("New Path: " + entry.getNewPath());

                        diffFormatter.format(entry);
                        String diff = out.toString(StandardCharsets.UTF_8);
                        diffContent.append(diff).append(System.lineSeparator());
                        out.reset();
                    }
                }

                return diffContent.toString();
            } catch (IOException | GitAPIException e) {
                e.printStackTrace();
                throw new Exception("差异内容生成失败", e);
            }
        }








        @Override
        public void exportVersion(String commitId, String filePath, String outputPath) {
            try (Repository repository = Git.open(new File(REPO_PATH)).getRepository();
                 RevWalk revWalk = new RevWalk(repository)) {

                RevCommit commit = revWalk.parseCommit(repository.resolve(commitId));
                try (TreeWalk treeWalk = new TreeWalk(repository)) {
                    treeWalk.addTree(commit.getTree());
                    treeWalk.setRecursive(true);
                    treeWalk.setFilter(PathFilter.create(filePath));

                    while (treeWalk.next()) {
                        String currentPath = treeWalk.getPathString();
                        File outputFile = new File(outputPath, currentPath);
                        if (outputFile.exists()) {
                            outputFile.delete();
                        }

                        if (treeWalk.isSubtree()) {
                            outputFile.mkdirs();
                            treeWalk.enterSubtree();
                        } else {
                            byte[] fileContent = repository.open(treeWalk.getObjectId(0)).getBytes();
                            outputFile.getParentFile().mkdirs();
                            Files.write(outputFile.toPath(), fileContent);
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        private AbstractTreeIterator prepareTreeParser(Repository repository, String objectId) throws IOException {
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




    }
