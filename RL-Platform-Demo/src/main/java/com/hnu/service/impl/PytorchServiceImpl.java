package com.hnu.service.impl;

import com.hnu.pojo.Algorithm;
import com.hnu.service.PytorchService;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

@Service
public class PytorchServiceImpl implements PytorchService {
    @Async("taskExecutor")
    public boolean algorithmTraining(Algorithm algorithm, String modelSaveDir) {
        // conda activate 命令路径（根据你的 Anaconda 安装目录调整）
        String condaPath = "D:\\anaconda3\\Scripts\\activate";
        // conda 环境名称（这里取算法名作为环境名称）
        String environmentName = algorithm.getName();

        System.out.println("Starting training...");

        // 获取算法的训练命令（从数据库中读取的 initCommand）
        String initCommand = algorithm.getInitCommand();  // 假设命令是类似于 "python main.py --episodes 200"

        // 添加 --save_dir 参数，确保命令传递模型保存路径
        String command = String.format("cmd /c \"cd /d D:\\Algorithm\\%s\\%s && call %s %s && %s --save_dir %s\"",
                algorithm.getName(), algorithm.getName(), condaPath, environmentName, initCommand, modelSaveDir);

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
                        // 判断标准输出中是否有 "success" 关键字
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
                        // 检测到警告信息，可以适当处理
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


            // 等待线程结束
            stdoutReader.join();
            stderrReader.join();
            if (exitCode == 0) {
                System.out.println("Python script executed successfully.");
                return true;  // 训练成功
            } else {
                System.err.println("Python script finished with error code: " + exitCode);
                return false;  // 训练失败
            }

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            return false;  // 如果捕获异常，也认为训练失败
        }
    }


    @Async("taskExecutor")
    public void continueTrain(String algorithmName, String initDir, String modelSaveDir, String command){
        // conda activate 命令路径（根据你的 Anaconda 安装目录调整）
        String condaPath = "D:\\anaconda3\\Scripts\\activate";

        System.out.println("Starting training...");



        // 构造命令，传递 --save_dir 和 --model_dir 参数
        String newCommand = String.format(
                "cmd /c \"cd /d D:\\Algorithm\\%s\\%s && call %s %s && %s --save_dir %s --model_dir %s\"",
                algorithmName, algorithmName, condaPath, algorithmName, command, modelSaveDir, initDir
        );



        // 使用 ProcessBuilder 执行命令
        ProcessBuilder processBuilder = new ProcessBuilder("cmd", "/c", newCommand);

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
