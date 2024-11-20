package com.hnu.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Algorithm {
    Integer id;
    String name;
    Integer version;
    String description;
    String dir;
    String command;
    @Getter
    String commitId;

}
