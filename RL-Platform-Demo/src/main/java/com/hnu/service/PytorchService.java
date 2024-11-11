package com.hnu.service;

public interface PytorchService {
    // 启动训练并返回初始输出
    String startTraining(String scriptPath);

    // 获取训练结果图表路径
    String getTrainingResult(String timestamp);


}
