package com.hnu.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Model 类表示一个机器学习模型的基本信息。
 * 包含算法名称、环境名称、版本号、运行命令和描述。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Model {

    /**
     * id
     */
    private int id; // id
    /**
     * 算法的主键 (外键)
     */
    private int algorithmId;  // 对应的算法表的主键
    /**
     * 算法名称
     */
    private String algorithm;  // 算法名

    /**
     * 环境名称
     */
    private String environment;  // 环境名

    /**
     * 版本号
     */
    private Integer version;  // 版本号

    /**
     * 运行命令
     */
    private String command;  // 运行命令

    /**
     * 描述信息
     */
    private String description;  // 描述
}
