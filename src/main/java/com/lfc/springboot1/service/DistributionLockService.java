package com.lfc.springboot1.service;

import com.lfc.springboot1.common.ConstantEnum;
import com.lfc.springboot1.common.LockStatus;
import com.lfc.springboot1.util.JedisUtil;
import com.lfc.springboot1.util.RandomUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.text.StrBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 〈一句话功能简述〉<br>
 * 加锁/解锁
 *
 * @author lfc
 * @create 2019/11/14 22:05
 * @since 1.0.0
 */
@Service
@Slf4j
public class DistributionLockService {
    private static final String TOKEN_PREFIX = new String("redisLock");
    @Autowired
    private JedisUtil jedisUtil;

    /**
     * 创建锁
     * @return
     */
    public LockStatus createLock(String tokenId,int outTime){
        String str = RandomUtil.UUID32();
        StrBuilder token = new StrBuilder();
        token.append(TOKEN_PREFIX).append(str);
        if (jedisUtil.tryGetDistributedLock(tokenId,token.toString(),outTime)){
            //加锁成功
            log.info("{}加锁成功",tokenId);
            return LockStatus.lockUpSuccess(token.toString());
        }
        return LockStatus.lockUpFailed();
    }

    /**
     * 解锁
     * @param tokenId
     * @param token
     * @return
     */
    public LockStatus unLock(String tokenId, String token){
        if (jedisUtil.releaseDistributedLock(tokenId,token)){
            //解锁成功
            log.info("{}解锁成功",tokenId);
            return LockStatus.unLockSuccess();
        }
        String val = jedisUtil.get(tokenId);
        if(StringUtils.isNotBlank(val) && !StringUtils.equals(val,token)){
            log.info("{}解锁失败",tokenId);
            return LockStatus.unLockFailed(val);
        }
        return LockStatus.unLockFailed();
    }

}