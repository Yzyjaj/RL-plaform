package com.hnu.service.impl;

import com.hnu.mapper.AlgorithmMapper;
import com.hnu.mapper.ModelMapper;
import com.hnu.pojo.Algorithm;
import com.hnu.service.AlgorithmService;
import com.hnu.service.FileService;
import com.hnu.service.GitService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AlgorithmServiceImpl implements AlgorithmService {
    public static String REPO_PATH = "D:\\Algorithm";
    @Autowired
    private AlgorithmMapper algorithmMapper;
    @Autowired
    private FileService fileService;
    @Autowired
    private GitService gitService;
    @Autowired
    private ModelMapper modelMapper;

    @Override
    public List<Algorithm> getAlgorithm() {
        return algorithmMapper.getAlgorithm();
    }


    @Override
    public void deleteAlgorithm(Integer id) {
        // 1. 从数据库中删除记录
        algorithmMapper.deleteAlgorithm(id);
    }


    @Override
    public void updateAlgorithm(Algorithm algorithm){
        algorithmMapper.updateAlgorithm(algorithm);
    }

    @Override
    public void uploadAlgorithm(String name,String commitId,String description, String initEnv, String initCommand){
        Integer version = algorithmMapper.getNextVersion(name);
        algorithmMapper.uploadAlgorithm(name,version,commitId,description, initEnv, initCommand);
    }

    @Override
    public String compareAlgorithm(Integer id1, Integer id2) {
        Algorithm algorithm = algorithmMapper.getAllById(id1);
        // 获取两个算法的提交ID
        String commitId1 = algorithmMapper.getcommitIdbyId(id1);
        String commitId2 = algorithmMapper.getcommitIdbyId(id2);
        String fileName = algorithm.getName();
        StringBuilder diffContent = new StringBuilder();  // 用于拼接差异内容

        try {
            // 获取差异内容
            diffContent.append(gitService.versionComparison(commitId1, commitId2, fileName));  // 获取差异内容并拼接

        } catch (Exception e) {
            throw new RuntimeException("生成差异内容失败", e);
        }

        return diffContent.toString();  // 返回差异内容
    }



    @Override
    public ResponseEntity<byte[]> exportAlgorithm(Integer id){
        Algorithm algorithm = algorithmMapper.getAllById(id);
        System.out.println(algorithm.getName());
        //导出旧版本
        gitService.exportVersion(algorithm.getCommitId(),algorithm.getName(),"D:\\Algorithm\\Export");
        //压缩该文件
        String sourceDirPath = REPO_PATH + '/' + algorithm.getName() + '/' + algorithm.getName();
        String outputDirPath = REPO_PATH + '/' + algorithm.getName() + ".zip";
        fileService.compressDirectory(sourceDirPath, outputDirPath);
        //导出该文件
        return fileService.downloadFile(outputDirPath);
    }

    @Override
    public void algorithmSaveModel(int algorithmId, String algorithm, String environment, String command, String modelDescription, String gitHash) {
        Integer version = modelMapper.getNextVersion(algorithm, environment);
        modelMapper.insertModel(algorithmId, algorithm, environment, version, command, modelDescription, gitHash);
    }




}
