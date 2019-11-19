package com.lfc.springboot1.service;

import com.lfc.springboot1.annotation.InterfaceIdempotence;
import com.lfc.springboot1.common.LockStatus;
import com.lfc.springboot1.util.JedisUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.validation.constraints.NotNull;
import java.time.LocalTime;

/**
 * 〈一句话功能简述〉<br>
 *
 * @author lfc
 * @create 2019/11/14 17:52
 * @since 1.0.0
 */
@Component
@Slf4j
public class TestService {

    @Autowired
    JedisUtil jedisUtil;

    @InterfaceIdempotence(module = "测试使用")
    public String print(){
        log.info(">>>>>>>>>>>>>>>>do sonmething>>>>>>");
        return "有呵呵呵";
    }

    @InterfaceIdempotence(expireTime = 1000000)
    public LockStatus<String> payment(@NotNull String payNo){
        //模拟数据库查询数据是否落地
        String val = jedisUtil.get("payType"+payNo);
        if(StringUtils.isBlank(val)){
            String ret = jedisUtil.set("payType"+payNo,LocalTime.now().toString());
            log.info("支付号: {} 到账确认",payNo);
            if(null == ret ){
                log.error("支付号：{} 支付落地数据存储异常！",payNo);
                return LockStatus.retFailed("支付号：" + payNo + "存储失败");
            }
            return LockStatus.retSuccess("支付号：" + payNo + "操作成功");
        }
        return LockStatus.retSuccess("支付号：" + payNo + "已支付");

    }
}