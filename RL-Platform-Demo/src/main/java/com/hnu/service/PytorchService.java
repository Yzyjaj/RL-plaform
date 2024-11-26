package com.hnu.service;

import com.hnu.pojo.Algorithm;

public interface PytorchService {
    // 启动训练并返回初始输出
    boolean algorithmTraining(Algorithm algorithm, String modelSaveDir);
    void continueTrain(String algorithmName, String initDir, String modelSaveDir ,String command);
}
