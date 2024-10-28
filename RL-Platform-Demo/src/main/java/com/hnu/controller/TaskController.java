package com.hnu.controller;

import com.hnu.pojo.Task;
import com.hnu.pojo.Result;
import com.hnu.service.TaskService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.CrossOrigin;

import java.util.List;

@Slf4j
@RestController
@CrossOrigin(origins = "http://localhost:5173") // 允许指定的来源
public class TaskController {
    @Autowired
    private TaskService taskService;
    @GetMapping("/gettasks")
    public Result gettasks(){
        log.info("显示数据管理的所有信息");
        List<Task> taskList = taskService.list();
        return Result.success(taskList);
    }
}
