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
        String scriptPathFixed = "C:\\Users\\chang\\Desktop\\DNQN_SimHash\\main.py";
        // conda activate 命令路径（根据你的 Anaconda 安装目录调整）
        String condaPath = "D:\\anaconda3\\Scripts\\activate";
        // conda 环境名称
        String environmentName = "RLonHand";

        System.out.println("Starting training...");

        // 组装命令：通过 cmd 执行多个命令来激活 conda 环境并执行 Python 脚本
        String command = String.format("cmd /c \"cd /d C:\\Users\\chang\\Desktop\\DNQN_SimHash && call %s %s && python %s\"",
                condaPath, environmentName, scriptPathFixed);


        // 使用 ProcessBuilder 执行命令
        ProcessBuilder processBuilder = new ProcessBuilder("cmd", "/c", command);

        try {
            // 启动进程
            Process process = processBuilder.start();

            // 创建标准输出流的读取器
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));

            // 使用独立线程读取输出，防止主线程阻塞
            Thread stdoutReader = new Thread(() -> {
                try {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        System.out.println("STDOUT: " + line);
                        if (line.contains("success")) {
                            System.out.println("Training completed successfully!");
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });

            // 使用独立线程读取错误输出，防止主线程阻塞
            Thread stderrReader = new Thread(() -> {
                try {
                    String line;
                    while ((line = errorReader.readLine()) != null) {
                        System.err.println("STDERR: " + line);
                        if (line.contains("DeprecationWarning")) {
                            System.err.println("Warning detected: " + line);
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });

            // 启动读取输出的线程
            stdoutReader.start();
            stderrReader.start();

            // 等待进程执行完毕
            int exitCode = process.waitFor();
            if (exitCode == 0) {
                System.out.println("Python script executed successfully.");
            } else {
                System.err.println("Python script finished with error code: " + exitCode);
            }

            // 等待线程结束
            stdoutReader.join();
            stderrReader.join();

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}
