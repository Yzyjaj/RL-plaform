package com.hnu.service;




public interface GitService {
    String submitInformation(String REPO_PATH);
    String versionComparison(String oldCommitId, String newCommitId, String fileName) throws Exception;
    void exportVersion(String commitId, String filePath, String outputPath);
    String commitModelToDVC(String repoPath, String algorithmName);
    void gitCheckout(String commitId ,String algorithmName);
    String dvcCheckout(String algorithmName, String initEnv, String gitHash);
}
