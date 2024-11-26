package com.hnu.mapper;

import com.hnu.pojo.Model;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface ModelMapper {



    @Select("SELECT COALESCE(MIN(version + 1), 1) " +
            "FROM model_library " +
            "WHERE algorithm = #{algorithm} AND environment = #{environment} " +
            "AND version + 1 NOT IN (SELECT version FROM model_library WHERE algorithm = #{algorithm} AND environment = #{environment})")
    int getNextVersion(@Param("algorithm") String algorithm, @Param("environment") String environment);



    // 插入模型
    @Insert("INSERT INTO model_library (algorithm_id, algorithm, environment, version, command, description) " +
            "VALUES (#{algorithmId}, #{algorithm}, #{environment}, #{version}, #{command}, #{description})")
    void insertModel(@Param("algorithmId") int algorithmId,
                     @Param("algorithm") String  algorithm,
                     @Param("environment") String environment,
                     @Param("version") Integer version,
                     @Param("command") String command,
                     @Param("description") String description);

    // 根据ID获取模型
    @Select("SELECT * FROM model_library WHERE id = #{id}")
    Model getModelById(@Param("id") Integer id);

    // 获取全部
    @Select("SELECT * FROM model_library")
    List<Model> getAllModels();

    // 根据ID删除模型（可以用删除方法直接执行删除）
    @Delete("DELETE FROM model_library WHERE id = #{id}")
    void deleteModelById(@Param("id") Integer id);


}
