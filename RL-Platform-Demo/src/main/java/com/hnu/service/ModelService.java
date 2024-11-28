package com.hnu.service;

import com.hnu.pojo.Model;
import com.hnu.pojo.Result;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

public interface ModelService {
    ResponseEntity<String> continueTrain(Integer id, String command, String modelDescription);
    ResponseEntity<Result>  getAllModels();
    ResponseEntity<byte[]> exportModel(Integer id);
}
