package com.zolo.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;


@FeignClient(name = "order-service", url = "127.0.0.1:8082")
public interface OrderFeignClient {

    @GetMapping("/create")
    int create(@RequestParam("userId") String userId,
                @RequestParam("commodityCode") String commodityCode,
                @RequestParam("count") Integer count,
               @RequestParam("orderNo") String orderNo);

}
