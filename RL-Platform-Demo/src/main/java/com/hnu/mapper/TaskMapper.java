package com.hnu.mapper;

import com.hnu.pojo.Task;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface TaskMapper {
    @Select("select * from task")
    List<Task> list();
    @Delete("delete from task where id=#{id}")
    void delete(Integer id);


}
