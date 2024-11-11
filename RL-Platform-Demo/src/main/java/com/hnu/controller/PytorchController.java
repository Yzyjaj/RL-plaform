package com.hnu.controller;

import com.hnu.service.PytorchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PytorchController {

    @Autowired
    private PytorchService pytorchService;

    /**
     * 启动 Python 训练脚本
     * @return 训练开始的状态信息
     */
    @GetMapping("/run-pytorch")
    public String runPyTorch() {
        System.out.println("Starting training...");  // 添加调试日志
        pytorchService.startTraining(""); // 异步启动训练
        return "Training started successfully.";  // 立即返回给客户端
    }

    /**
     * 获取训练结果（图表）路径
     * @param timestamp 训练时间戳，用于标识训练结果文件
     * @return 训练结果文件的路径或错误信息
     */
    @GetMapping("/get-training-result")
    public String getTrainingResult(String timestamp) {
        return pytorchService.getTrainingResult(timestamp);
    }
}
