package com.hnu.service;

import com.hnu.pojo.Algorithm;
import jakarta.annotation.Resource;
import org.springframework.http.ResponseEntity;

import java.util.List;

public interface AlgorithmService {
    List<Algorithm> getAlgorithm();

    void deleteAlgorithm(Integer id);
    void updateAlgorithm(Algorithm algorithm);
    void uploadAlgorithm(String name,String commitId,String description);
    void compareAlgorithm(Integer id1,Integer id2);
    ResponseEntity<byte[]> exportAlgorithm(Integer id);

}
