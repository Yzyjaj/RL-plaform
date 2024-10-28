package com.hnu.service.impl;

import com.hnu.mapper.TaskMapper;
import com.hnu.pojo.Task;
import com.hnu.service.TaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TaskServiceImpl implements TaskService {
    @Autowired
    private TaskMapper taskMapper;
    @Override
    public List<Task> list(){
        return taskMapper.list();
    }
    @Override
    public void delete(Integer id){
        taskMapper.delete(id);
    }

    @Override
    public void updatetask(Task task){
        taskMapper.updatetask(task);
    }
}
