package com.zoro.persistence;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
public class Wms implements Serializable {

    private int wmsId;
    private int orderId;
    private String address;
    private String wmsStatus;
    private Date createTime;
    private Date updateTime;

}
