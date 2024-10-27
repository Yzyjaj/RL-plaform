package com.hnu.mapper;

import com.hnu.pojo.DataManage;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface DataManageMapper {
    @Select("select * from data_management")
    List<DataManage> list();
}
