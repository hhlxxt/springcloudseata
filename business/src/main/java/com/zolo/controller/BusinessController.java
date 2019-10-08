package com.zolo.controller;

import com.zolo.entity.CommonDto;
import com.zolo.service.BusinessService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
public class BusinessController {

    @Autowired
    private BusinessService businessService;

    /**
     * 购买下单，模拟全局事务提交
     *
     * @return
     */
    @RequestMapping(value = "/purchase/commit", produces = "application/json")
    public String purchaseCommit() {
        try {
            CommonDto dto = new CommonDto();
            dto.setUserId("U100000");
            dto.setCommodityCode("C100000");
            dto.setOrderCount(2);
            businessService.business(dto);
        } catch (Exception exx) {
            return exx.getMessage();
        }
        return "全局事务提交";
    }

    /**
     * 购买下单，模拟全局事务回滚
     * 账户或库存不足
     *
     * @return
     */
    @RequestMapping("/purchase/rollback")
    public String purchaseRollback() {
        try {
            CommonDto dto = new CommonDto() ;
            dto.setUserId("U100000");
            dto.setCommodityCode("C100000");
            dto.setOrderCount(99999);
            businessService.business(dto);
        } catch (Exception exx) {
            return exx.getMessage();
        }
        return "全局事务提交";
    }
}
