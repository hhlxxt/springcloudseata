package com.zolo.entity;

import lombok.Data;

@Data
public class CommonDto {
    private String userId;
    private String commodityCode;
    private int orderCount;
    private String orderNo;
}
