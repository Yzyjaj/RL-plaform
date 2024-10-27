package com.hnu.controller;

import com.hnu.pojo.DataManage;
import com.hnu.pojo.Result;
import com.hnu.service.DataManageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
public class DataManageController {
    @Autowired
    private DataManageService dataManageService;
    @GetMapping("/datamanage")
    public Result dataManage(){
        log.info("显示数据管理的所有信息");
        List<DataManage> dataManageList = dataManageService.list();
        return Result.success(dataManageList);
    }
}
