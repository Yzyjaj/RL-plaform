package com.hnu.service.impl;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

@Service
public class PytorchTrainingServiceImpl {

    // 异步执行 Python 训练任务
    @Async("taskExecutor")
    public void startTrainingAsync(String scriptPath) {
        // 设置固定路径
        String scriptPathFixed = "F:/Java/RLplatform/code/DNQN_SimHash/main.py";
        System.out.println("Starting training...");

        // 执行 Python 脚本并获取输出
        ProcessBuilder processBuilder = new ProcessBuilder("python", scriptPathFixed);

        try {
            // 启动进程
            Process process = processBuilder.start();

            // 创建标准输出流的读取器
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));

            String line;

            // 逐行读取标准输出
            while ((line = reader.readLine()) != null) {
                // 打印标准输出内容
                System.out.println("STDOUT: " + line);

                // 如果输出中包含 "success" 或者其他成功标志
                if (line.contains("success")) {
                    System.out.println("Training completed successfully!");
                    break; // 如果已经完成，跳出循环
                }
            }

            // 逐行读取标准错误流（警告和错误信息）
            while ((line = errorReader.readLine()) != null) {
                // 打印标准错误内容
                System.err.println("STDERR: " + line);

                // 如果遇到 DeprecationWarning 或其他警告，捕获并处理
                if (line.contains("DeprecationWarning")) {
                    System.err.println("Warning detected: " + line);
                }
            }

            // 等待进程执行完毕
            int exitCode = process.waitFor();
            if (exitCode == 0) {
                System.out.println("Python script executed successfully.");
            } else {
                System.err.println("Python script finished with error code: " + exitCode);
            }

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}
