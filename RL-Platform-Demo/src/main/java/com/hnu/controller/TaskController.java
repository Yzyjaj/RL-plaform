package com.hnu.controller;

import com.hnu.pojo.Task;
import com.hnu.pojo.Result;
import com.hnu.service.TaskService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@CrossOrigin(origins = "http://localhost:5173") // 允许指定的来源
public class TaskController {
    @Autowired
    private TaskService taskService;
    @GetMapping("/gettasks")
    public Result gettasks(){
        log.info("显示所有信息");
        List<Task> taskList = taskService.list();
        return Result.success(taskList);
    }

    @DeleteMapping("/deletetask/{id}")
    public Result deletetask(@PathVariable Integer id){
        log.info("根据id删除任务,{}",id);
        taskService.delete(id);
        return Result.success();

    }
//    updatatasks
    @PutMapping("/updatetask")
    public Result updatatask(@RequestBody Task task){
        log.info("更新任务信息{}");
        taskService.updatetask(task);
        return Result.success();
    }
}
