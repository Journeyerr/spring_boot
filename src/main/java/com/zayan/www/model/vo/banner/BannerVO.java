package com.zayan.www.model.vo.banner;


import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class BannerVO {

    @JsonProperty("id")
    private Integer id;

    @ApiModelProperty("门店id")
    @JsonProperty("shop_id")
    private Integer shopId;

    @ApiModelProperty("门店名称")
    @JsonProperty("shop_name")
    private String shopName;

    @ApiModelProperty("状态")
    @JsonProperty("status")
    private String status;

    @ApiModelProperty("图片url")
    @JsonProperty("banner_image")
    private String bannerImage;

    @ApiModelProperty("创建时间")
    @JsonProperty("created_at")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
}