package com.hnu.service.impl;

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
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

@Service
public class GitServiceImpl implements GitService {

    private static final String REPO_PATH = "D://Algorithm";

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
    public void versionComparison(String oldCommitId, String newCommitId) throws Exception {
        File repoDir = new File(REPO_PATH + "/.git");
        try (Repository repository = new FileRepositoryBuilder().setGitDir(repoDir).build()) {
            try (Git git = new Git(repository)) {
                AbstractTreeIterator oldTreeParser = prepareTreeParser(repository, oldCommitId);
                AbstractTreeIterator newTreeParser = prepareTreeParser(repository, newCommitId);

                List<DiffEntry> diffEntries = git.diff()
                        .setOldTree(oldTreeParser)
                        .setNewTree(newTreeParser)
                        .call();

                try (ByteArrayOutputStream out = new ByteArrayOutputStream();
                     DiffFormatter diffFormatter = new DiffFormatter(out)) {

                    diffFormatter.setRepository(repository);
                    for (DiffEntry entry : diffEntries) {
                        diffFormatter.format(entry);
                        System.out.println(out.toString("UTF-8"));
                        out.reset();
                    }
                }
            }
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
