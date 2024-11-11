package com.hnu.service;

import com.hnu.pojo.Algorithm;

import java.util.List;

public interface AlgorithmService {
    List<Algorithm> getAlgorithm();

    void deleteAlgorithm(Integer id);
    void updateAlgorithm(Algorithm algorithm);
    void uploadAlgorithm(String name,String commitId,String description);
    void compareAlgorithm(Integer id1,Integer id2);
    void exportAlgorithm(Integer id,String version);
}
