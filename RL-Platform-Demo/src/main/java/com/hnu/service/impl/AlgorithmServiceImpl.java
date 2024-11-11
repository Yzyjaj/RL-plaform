package com.hnu.service.impl;

import com.hnu.mapper.AlgorithmMapper;
import com.hnu.pojo.Algorithm;
import com.hnu.pojo.Result;
import com.hnu.service.AlgorithmService;
import com.hnu.utlis.UploadUtlis;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AlgorithmServiceImpl implements AlgorithmService {
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
    public void exportAlgorithm(Integer id,String version){
        String name = algorithmMapper.getNameById(id);
        Algorithm algorithm = algorithmMapper.getAlgorithmByNameVersion(name,version);
//        System.out.println(algorithm.getId());
//        System.out.println(algorithm.getName());
//        System.out.println(algorithm.getVersion());
//        System.out.println(algorithm.getDescription());
//        System.out.println(algorithm.getDir());
//        System.out.println(algorithm.getCommand());
//        System.out.println(algorithm.getCommitId());
//        System.out.println(algorithm.getDir()+'/'+algorithm.getName());
        uploadUtlis.exportVersion(algorithm.getCommitId(),algorithm.getDir()+'/'+algorithm.getName(),"F:\\Java\\RLplatform\\Algorithm_repos");
    }
}
