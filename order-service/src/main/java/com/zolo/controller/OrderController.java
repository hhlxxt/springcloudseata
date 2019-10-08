package com.zolo.controller;

import com.zolo.entity.CommonDto;
import com.zolo.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
public class OrderController {

    @Autowired
    private OrderService orderService;

    @GetMapping(value = "/create", produces = "application/json")
    public int create(String userId, String commodityCode, Integer count,String orderNo) {
        CommonDto dto = new CommonDto() ;
        dto.setUserId(userId);
        dto.setCommodityCode(commodityCode);
        dto.setOrderCount(count);
        dto.setOrderNo(orderNo);

        int result = orderService.create(dto);
        return result;
    }

}
