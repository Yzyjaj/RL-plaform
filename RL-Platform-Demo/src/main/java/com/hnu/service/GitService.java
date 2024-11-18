package com.hnu.service;

import java.io.IOException;

public interface GitService {
    String submitInformation(String REPO_PATH);
    void versionComparison(String oldCommitId, String newCommitId) throws Exception;
    void exportVersion(String commitId, String filePath, String outputPath);
}
