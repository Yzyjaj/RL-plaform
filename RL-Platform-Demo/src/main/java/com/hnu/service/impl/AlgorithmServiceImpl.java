package com.hnu.service.impl;

import com.hnu.mapper.AlgorithmMapper;
import com.hnu.pojo.Algorithm;
import com.hnu.pojo.Result;
import com.hnu.service.AlgorithmService;
import com.hnu.utlis.UploadUtlis;
import jakarta.annotation.Resource;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Service
public class AlgorithmServiceImpl implements AlgorithmService {
    public static String REPO_PATH = "F:/Java/RLplatform/Algorithm_repos";
    @Autowired
    private AlgorithmMapper algorithmMapper;
    @Autowired
    private UploadUtlis uploadUtlis;
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
            uploadUtlis.versionComparison(commitId1, commitId2);
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
        uploadUtlis.exportVersion(algorithm.getCommitId(),algorithm.getName(),"F:/Java/RLplatform/Algorithm_repos");
        //压缩该文件
        uploadUtlis.compressDirectory(algorithm.getName());
        //导出该文件
        return uploadUtlis.downloadFile(algorithm.getName());
    }



}
