package com.hnu.service.impl;

import com.hnu.mapper.ModelMapper;
import com.hnu.pojo.Model;
import com.hnu.service.ResultService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

@Service
public class ResultServiceImpl implements ResultService {

    @Autowired
    private ModelMapper modelMapper;

    private static String REPO_PATH = "D:\\Models";
    @Override
    public List<String> loadAllImagesAsBase64(Integer id) {
        List<String> base64Images = new ArrayList<>();
        try {
            // 获取算法信息
            Model model = modelMapper.getModelById(id);
            String imageDir = REPO_PATH + File.separator + model.getAlgorithm() + File.separator + model.getEnvironment();
            System.out.println("Image directory: " + imageDir);

            // 获取文件目录路径
            Path dirPath = Paths.get(imageDir);

            // 遍历目录中的所有文件，筛选出图片文件（扩展名为 .png, .jpg, .jpeg）
            Files.walk(dirPath)
                    .filter(path -> {
                        String fileName = path.getFileName().toString().toLowerCase();
                        return fileName.endsWith(".png") || fileName.endsWith(".jpg") || fileName.endsWith(".jpeg");
                    })
                    .forEach(path -> {
                        try {
                            byte[] imageBytes = Files.readAllBytes(path);
                            String base64Image = Base64.getEncoder().encodeToString(imageBytes);
                            base64Images.add(base64Image);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    });

            if (base64Images.isEmpty()) {
                throw new RuntimeException("No readable images found in directory: " + imageDir);
            }

        } catch (IOException e) {
            throw new RuntimeException("Could not load images from directory", e);
        }
        return base64Images;
    }
}