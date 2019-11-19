package com.lfc.springboot1.controllers;

import com.lfc.springboot1.common.LockStatus;
import com.lfc.springboot1.service.TestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 〈一句话功能简述〉<br>
 * 支付接口
 *
 * @author lfc
 * @create 2019/11/16 23:55
 * @since 1.0.0
 */
@RestController
@RequestMapping("/pay")
public class PayController {

    @Autowired
    TestService testService;

    @GetMapping("/confirm")
    public String payConfirm(@RequestParam  String payNo){
        LockStatus lockStatus = testService.payment(payNo);
        return String.valueOf(lockStatus.getData() == null ? lockStatus.getMsg() : lockStatus.getData());
    }
}