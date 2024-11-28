package com.hnu.controller;


import com.hnu.pojo.Model;
import com.hnu.pojo.Result;
import com.hnu.service.ModelService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@Slf4j
@RestController
@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true")

public class ModelController {

    @Autowired
    private ModelService modelService;
    //训练算法的初始模型
    @PostMapping("/continueTrain/{id}")
    public ResponseEntity<String> continueTrain(@PathVariable Integer id, @RequestParam  String command, @RequestParam String modelDescription) {
        return modelService.continueTrain(id, command, modelDescription);
    }

    // 获取全部模型
    @GetMapping("/getAllModels")
    public ResponseEntity<Result> getAllModels() {
        try {
            // 调用服务层的方法来获取模型列表
            return modelService.getAllModels();
        } catch (Exception e) {
            // 捕获异常并返回 500 错误
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Result.error("获取模型列表失败: " + e.getMessage()));
        }
    }



    // 导出模型信息接口
    @GetMapping("/exportModel/{id}")
    public ResponseEntity<byte[]> exportModel(@PathVariable Integer id) {
       return  modelService.exportModel(id);
    }

}
