package com.hnu.service.impl;

import com.hnu.mapper.AlgorithmMapper;
import com.hnu.mapper.ModelMapper;
import com.hnu.pojo.Algorithm;
import com.hnu.pojo.Model;
import com.hnu.pojo.Result;
import com.hnu.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class ModelServiceImpl implements ModelService {


    @Autowired
    private ModelMapper modelMapper;
    @Autowired
    private AlgorithmMapper algorithmMapper;
    @Autowired
    private GitService gitService;
    @Autowired
    private PytorchService pytorchService;
    @Autowired
    private AlgorithmService algorithmService;
    @Autowired
    private FileService fileService;

    @Override
    public ResponseEntity<String> continueTrain(Integer id, String command, String modelDescription) {
        try {
            // 获取模型信息，通过模型id查找模型
            Model model = modelMapper.getModelById(id);
            if (model == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Model not found.");
            }
            // 获取算法信息，通过模型的algorithmId来查询对应的算法
            Algorithm algorithm = algorithmMapper.getAlgorithmById(model.getAlgorithmId());
            if (algorithm == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Algorithm not found.");
            }
            // 获取算法的commitId（假设模型中存储的算法对应版本commitId）
            String commitId = algorithm.getCommitId();
            String algorithmName = algorithm.getName();
            String initEnv =  model.getEnvironment();

            String changeEnv =  model.getEnvironment();
            String gitHash = model.getGitHash();
            // 调用gitService进行算法版本回滚
            gitService.gitCheckout(commitId, algorithmName);
            // 解析上传的命令，提取--env_name参数的值
            String newEnv = extractEnvName(command);
            if (newEnv != null) {
                changeEnv = newEnv;
            }

            String modelSaveDir = fileService.generateModelSaveDir(algorithmName, changeEnv);
            gitService.dvcCheckout(algorithmName, initEnv, gitHash);
            String initModelPath = fileService.findModelFilePath(algorithmName, initEnv);
            // 执行训练命令
            pytorchService.continueTrain(algorithmName, initModelPath, modelSaveDir, command);

            fileService.deleteModelFile(initModelPath);
            String commitHash = gitService.commitModelToDVC(modelSaveDir, algorithm.getName());
            String deleteModelPath = modelSaveDir + algorithm.getName() + ".pt";

            fileService.deleteModelFile(deleteModelPath);
            // 生成新的版本并保存模型
            Integer version = modelMapper.getNextVersion(model.getAlgorithm(), changeEnv);
            modelMapper.insertModel(model.getAlgorithmId(), model.getAlgorithm(), changeEnv, version, command, modelDescription, commitHash);
            return ResponseEntity.ok("Training continued successfully.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Training failed: " + e.getMessage());
        }
    }

    @Override
    public ResponseEntity<Result> getAllModels() {
        try {
            // 通过模型的数据库仓库获取所有模型
            List<Model> models = modelMapper.getAllModels();

            if (models.isEmpty()) {
                // 如果没有模型，返回 204 No Content
                return ResponseEntity.status(HttpStatus.NO_CONTENT).body(Result.error("没有找到任何模型"));
            }

            // 返回成功的响应，包含模型数据
            return ResponseEntity.ok(Result.success(models));
        } catch (Exception e) {
            // 捕获异常并返回 500 错误响应
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Result.error("获取模型列表失败: " + e.getMessage()));
        }
    }



    @Override
    public ResponseEntity<byte[]> exportModel(Integer id){
        // 查询模型信息
        Model model = modelMapper.getModelById(id);

        String algorithmName = model.getAlgorithm();
        String initEnv = model.getEnvironment();
        Integer initVersion = model.getVersion();
        String gitHash = model.getGitHash();

        gitService.dvcCheckout(algorithmName, initEnv, gitHash);

        // 构建模型文件夹路径
        String sourceDirPath = String.format("D:\\Models\\%s\\%s\\%s.pt", algorithmName, initEnv, algorithmName);

        String outputDirPath = String.format("D:\\Models\\%s\\%s\\version%d.zip", algorithmName, initEnv, initVersion);

        fileService.compressDirectory(sourceDirPath, outputDirPath);
        String initModelPath = fileService.findModelFilePath(algorithmName, initEnv);
        //fileService.deleteModelFile(initModelPath);
        return fileService.downloadFile(outputDirPath);
    }


    /**
     * 解析命令中的--env_name参数，并提取环境名
     * @param command 用户上传的命令
     * @return 提取出的环境名，如果没有--env_name参数则返回null
     */
    private String extractEnvName(String command) {
        // 正则表达式匹配 --env_name 后面的环境名
        Pattern pattern = Pattern.compile("--env_name\\s+(\\S+)");
        Matcher matcher = pattern.matcher(command);

        if (matcher.find()) {
            // 提取环境名并返回
            return matcher.group(1);
        }

        // 如果没有--env_name参数，则返回null
        return null;
    }

}