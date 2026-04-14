package com.mochu.oa.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "登录响应")
public class LoginVO {
    
    @Schema(description = "访问令牌")
    private String token;
    
    @Schema(description = "用户ID")
    private Long userId;
    
    @Schema(description = "用户名")
    private String username;
    
    @Schema(description = "真实姓名")
    private String realName;
    
    @Schema(description = "头像")
    private String avatar;
    
    @Schema(description = "部门ID")
    private Long departmentId;
    
    @Schema(description = "部门名称")
    private String departmentName;
}