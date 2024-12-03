package com.hnu.service;

import com.hnu.pojo.Task;

import java.util.List;

public interface ResultService {

    List<String> loadAllImagesAsBase64(Integer id);
}