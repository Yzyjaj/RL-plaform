package com.hnu.mapper;

import com.hnu.pojo.Algorithm;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface AlgorithmMapper {
    @Select("select * from algorithm_library")
    List<Algorithm> getAlgorithm();

    @Delete("delete from algorithm_library where id=#{id}")
    void deleteAlgorithm(Integer id);

    @Update("update algorithm_library set description=#{description} where id=#{id}")
    void updateAlgorithm(Algorithm algorithm);

    //获取下一个版本号
    @Select("select COALESCE(MAX(version), 0) + 1 from algorithm_library where name = #{name}")
    int getNextVersion(String name);

    //插入算法
    @Insert("insert into algorithm_library (name,version,commitId,description,initEnv,initCommand) values (#{name},#{version},#{commitId},#{description},#{initEnv},#{initCommand})")
    void uploadAlgorithm(String name,Integer version,String commitId,String description, String initEnv, String initCommand);

    @Select("select * from algorithm_library where id=#{id}")
    Algorithm getAllById(Integer id);

    @Select("select commitId from algorithm_library where id=#{id}")
    String getcommitIdbyId(Integer id);

    // 通过ID查询算法
    @Select("SELECT * FROM algorithm_library WHERE id = #{id}")
    Algorithm getAlgorithmById(Integer id);

    @Select("select name from algorithm_library where id=#{id}")
    String getNameById(Integer id);

    @Select("select * from algorithm_library where name=#{name} and version=#{version}")
    Algorithm getAlgorithmByNameVersion(String name,String version);


}
