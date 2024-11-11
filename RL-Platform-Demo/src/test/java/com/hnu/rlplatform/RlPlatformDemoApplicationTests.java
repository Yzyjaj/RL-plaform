package com.hnu.rlplatform;

import com.hnu.mapper.AlgorithmMapper;
import com.hnu.pojo.Result;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import com.hnu.utlis.UploadUtlis;
@SpringBootTest
class RlPlatformDemoApplicationTests {
    @Autowired
    UploadUtlis uploadUtlis;
    @Autowired
    private AlgorithmMapper algorithmMapper;
    @Test
    void contextLoads() {
        try{
            uploadUtlis.versionComparison("4ad6ada5f0b8214642537ae3bf01c15e96d4a4b9", "449d2638154fff97bc38d4b41f01126e86e2cf52");
            System.out.println("对比完成");
        }catch (Exception e){
            System.out.println("对比失败");
        }
    }

    @Test
    void export(){
        uploadUtlis.exportVersion("449d2638154fff97bc38d4b41f01126e86e2cf52","Algorithm_1/DNQN_SimHash","F:\\Java\\RLplatform\\Algorithm_repos");
    }
    @Test
    void zipname(){
        String zipname = uploadUtlis.getFileNameWithoutExtension("testdir.zip");
        System.out.println(zipname);
    }
    @Test
    void ran(){

    }

}
