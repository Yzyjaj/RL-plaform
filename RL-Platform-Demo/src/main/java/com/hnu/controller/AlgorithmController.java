package com.hnu.controller;


import com.hnu.pojo.Algorithm;
import com.hnu.pojo.Result;
import com.hnu.service.AlgorithmService;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.annotations.Delete;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
public class AlgorithmController {
    @Autowired
    private AlgorithmService algorithmService;

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
    //对比代码功能
    @GetMapping("/compareAlgorithm/{id1}/{id2}")
    public Result compareAlgorithm(@PathVariable Integer id1,@PathVariable Integer id2){
        log.info("进行算法比对{},{}",id1,id2);
        algorithmService.compareAlgorithm(id1,id2);
        return Result.success();
    }
    //导出旧版本代码
    @GetMapping("/exportAlgorithm/{id}/{version}")
    public Result exportAlgorithm(@PathVariable Integer id,@PathVariable String version){
        log.info("导出历史版本{},{}",id,version);
        algorithmService.exportAlgorithm(id,version);
        return Result.success();
    }
}
