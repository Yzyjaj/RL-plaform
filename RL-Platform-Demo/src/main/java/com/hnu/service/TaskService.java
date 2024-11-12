package com.hnu.service;

import com.hnu.pojo.Task;

import java.util.List;

public interface TaskService {

    List<Task> list();

    void delete(Integer id);

    void updatetask( Task task);
}
