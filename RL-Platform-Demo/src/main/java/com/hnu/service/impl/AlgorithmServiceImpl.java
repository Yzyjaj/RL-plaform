package com.hnu.service.impl;

import com.hnu.mapper.AlgorithmMapper;
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
    @Override
    public List<Algorithm> getAlgorithm() {
        return algorithmMapper.getAlgorithm();
    }

    @Override
    public void deleteAlgorithm(Integer id){
        algorithmMapper.deleteAlgorithm(id);
    }
    @Override
    public void updateAlgorithm(Algorithm algorithm){
        algorithmMapper.updateAlgorithm(algorithm);
    }
    @Override
    public void uploadAlgorithm(String name,String commitId,String description){
        Integer version = algorithmMapper.getNextVersion(name);
        algorithmMapper.uploadAlgorithm(name,version,commitId,description);
    }
    @Override
    public void compareAlgorithm(Integer id1,Integer id2){
        //旧提交的commitId
        String commitId1 = algorithmMapper.getcommitIdbyId(id1);
        //新提交的commitId
        String commitId2 = algorithmMapper.getcommitIdbyId(id2);
        try{
            gitService.versionComparison(commitId1, commitId2);
            System.out.println("对比完成");
        }catch (Exception e){
            System.out.println("对比失败");
        }
    }
    @Override
    public ResponseEntity<byte[]> exportAlgorithm(Integer id){
        Algorithm algorithm = algorithmMapper.getAllById(id);
        System.out.println(algorithm.getName());
        //导出旧版本
        gitService.exportVersion(algorithm.getCommitId(),algorithm.getName(),"D:\\Algorithm");
        //压缩该文件
        fileService.compressDirectory(algorithm.getName());
        //导出该文件
        return fileService.downloadFile(algorithm.getName());
    }



}
