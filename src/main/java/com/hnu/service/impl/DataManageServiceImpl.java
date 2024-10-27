package com.hnu.service.impl;

import com.hnu.mapper.DataManageMapper;
import com.hnu.pojo.DataManage;
import com.hnu.service.DataManageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DataManageServiceImpl implements DataManageService {
    @Autowired
    private DataManageMapper dataManageMapper;
    @Override
    public List<DataManage> list(){
        return dataManageMapper.list();
    }
}
