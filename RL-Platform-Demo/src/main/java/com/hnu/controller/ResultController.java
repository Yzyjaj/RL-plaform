package com.hnu.controller;

import com.hnu.pojo.Result;
import com.hnu.service.ResultService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true")
public class ResultController {
    @Autowired
    ResultService resultService;
    @GetMapping("/getAllImages/{id}")
    public Result getImages(@PathVariable Integer id) {

        List<String> images = resultService.loadAllImagesAsBase64(id);
        if (images.isEmpty()) {
            return Result.error("No images found");  // 如果没有图片，返回404
        }

        return Result.success(images);
    }
}
