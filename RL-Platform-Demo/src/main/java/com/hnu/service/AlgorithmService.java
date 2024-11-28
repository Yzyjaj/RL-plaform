package com.hnu.service;

import com.hnu.pojo.Algorithm;
import org.springframework.http.ResponseEntity;
import java.util.List;

public interface AlgorithmService {
    List<Algorithm> getAlgorithm();

    void deleteAlgorithm(Integer id);
    void updateAlgorithm(Algorithm algorithm);
    void uploadAlgorithm(String name,String commitId,String description, String initEnv, String initCommand);
    void algorithmSaveModel(int algorithmId, String algorithm, String environment, String command, String modelDescription, String gitHash);
    String compareAlgorithm(Integer id1, Integer id2);
    ResponseEntity<byte[]> exportAlgorithm(Integer id);

}
