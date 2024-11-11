package com.hnu.service.impl;

import com.hnu.service.PytorchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;

@Service
public class PytorchServiceImpl implements PytorchService {

    @Autowired
    private PytorchTrainingServiceImpl pytorchTrainingServiceImpl;  // 注入重命名后的服务类

    @Override
    public String startTraining(String scriptPath) {
        // 调用异步方法启动训练
        pytorchTrainingServiceImpl.startTrainingAsync(scriptPath);  // 异步启动训练

        return "Training started successfully.";  // 立即返回给用户
    }

    @Override
    public String getTrainingResult(String timestamp) {
        // 返回训练结果图表的路径
        String resultPath = "figs/" + timestamp + "/training_result.png";
        File resultFile = new File(resultPath);

        // 如果文件存在，返回路径，否则返回文件未找到
        return resultFile.exists() ? resultPath : "Training result not found.";
    }
}
