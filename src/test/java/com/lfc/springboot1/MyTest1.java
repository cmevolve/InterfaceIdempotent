package com.lfc.springboot1;

import com.lfc.springboot1.common.ConstantEnum;
import com.lfc.springboot1.service.TestService;
import com.lfc.springboot1.util.JedisUtil;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * 〈一句话功能简述〉<br>
 *
 * @author lfc
 * @create 2019/11/14 17:28
 * @since 1.0.0
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class MyTest1 {
    @Autowired
    TestService testService;
    @Autowired
    private JedisUtil jedisUtil;
    @Test
    public void annotationTest()  {
       //System.out.println(testService.print("abcdefg").getMsg());
       //testService.print();

    }

    @Test
    public void addLock(){
        System.out.println("======加锁==========");
        System.out.println(jedisUtil.tryGetDistributedLock("sf","sfa",100000));
        System.out.println(jedisUtil.get("sf"));

        System.out.println("======非正确释放锁==========");
        System.out.println(jedisUtil.releaseDistributedLock("sf","zcxv"));
        System.out.println("======正确释放==========");
        System.out.println(jedisUtil.releaseDistributedLock("sf","sfa"));
        System.out.println("======重复释放==========");
        System.out.println(jedisUtil.releaseDistributedLock("sf","sfa"));
    }

    void typeTest(Object i){
        System.out.println(String.class.equals(i.getClass()));
    }
}