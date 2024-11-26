package com.hnu.controller;


import com.hnu.mapper.AlgorithmMapper;
import com.hnu.pojo.Algorithm;
import com.hnu.pojo.Result;
import com.hnu.service.AlgorithmService;
import com.hnu.service.FileService;
import com.hnu.service.PytorchService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


import java.util.List;

@Slf4j
@RestController
@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true")
public class AlgorithmController {
    @Autowired
    private AlgorithmService algorithmService;
    @Autowired
    private AlgorithmMapper algorithmMapper;
    @Autowired
    private FileService fileService;
    @Autowired
    private PytorchService pytorchService;


    @GetMapping("/getAlgorithm")
    public Result getAlgorithm(){
        log.info("显示算法信息");
        List<Algorithm> algorithmList = algorithmService.getAlgorithm();
        return Result.success(algorithmList);
    }

    @DeleteMapping("/deleteAlgorithm/{id}")
    public Result deleteAlgorithm(@PathVariable Integer id){
        log.info("根据id删除算法{}",id);
        algorithmService.deleteAlgorithm(id);
        return Result.success();
    }

    @PutMapping("/updateAlgorithm")
    public Result updateAlgorithm(@RequestBody Algorithm algorithm){
        log.info("更新算法描述");
        algorithmService.updateAlgorithm(algorithm);
        return Result.success();
    }
    @GetMapping("/compareAlgorithm/{id1}/{id2}")
    public ResponseEntity<String> compareAlgorithm(@PathVariable Integer id1, @PathVariable Integer id2) {
        log.info("进行算法比对{},{}", id1, id2);

        try {
            // 获取差异内容
            String diffContent = algorithmService.compareAlgorithm(id1, id2);

            if (diffContent != null) {
                return ResponseEntity.ok(diffContent);  // 返回差异内容给前端
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("生成差异内容失败");  // 返回错误
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("生成差异内容失败");
        }
    }

    //导出旧版本代码
    @GetMapping("/exportAlgorithm/{id}")
    public ResponseEntity<byte[]> exportAlgorithm(@PathVariable Integer id){
        log.info("将算法文件压缩包上传给前端,{}",id);
        return algorithmService.exportAlgorithm(id);
    }

    //训练算法的初始模型
    @PostMapping("/trainAlgorithm/{id}")
    public ResponseEntity<Result> trainAlgorithm(@PathVariable Integer id, @RequestParam  String modelDescription) {
        try {
            // 获取算法信息
            Algorithm algorithm = algorithmMapper.getAlgorithmById(id);
            if (algorithm == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Result.error("Algorithm not found."));
            }

            // 生成模型保存路径
            String modelSaveDir = fileService.generateModelSaveDir(algorithm.getName(), algorithm.getInitEnv());
            System.out.println("algorithm" + algorithm);
            System.out.println("modelSaveDir" + modelSaveDir);

            // 执行训练命令
            boolean trainingSuccess = pytorchService.algorithmTraining(algorithm, modelSaveDir);

            if (!trainingSuccess) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Result.error("Training failed."));
            }
            // 保存模型信息到数据库
            algorithmService.algorithmSaveModel(algorithm.getId(), algorithm.getName(), algorithm.getInitEnv(), algorithm.getInitCommand(), modelDescription);

            // 返回特定成功代码
            return ResponseEntity.ok(Result.success());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Result.error("Training failed: " + e.getMessage()));
        }
    }


}
