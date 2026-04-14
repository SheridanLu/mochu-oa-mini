package com.mochu.oa.controller;

import com.mochu.oa.common.Result;
import com.mochu.oa.common.JwtUtils;
import com.mochu.oa.dto.LoginDTO;
import com.mochu.oa.entity.SysUser;
import com.mochu.oa.service.SysUserService;
import com.mochu.oa.vo.LoginVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "认证管理")
public class LoginController {
    
    private final SysUserService sysUserService;
    private final JwtUtils jwtUtils;
    private final PasswordEncoder passwordEncoder;
    
    @PostMapping("/login")
    @Operation(summary = "用户名密码登录")
    public Result<LoginVO> login(@Valid @RequestBody LoginDTO loginDTO, HttpServletRequest request) {
        SysUser user = sysUserService.login(loginDTO.getUsername(), loginDTO.getPassword(), passwordEncoder);
        if (user == null) {
            return Result.error("用户名或密码错误");
        }
        
        String token = jwtUtils.generateToken(user.getId(), user.getUsername());
        sysUserService.updateLoginInfo(user.getId(), getClientIp(request));
        
        LoginVO loginVO = new LoginVO();
        loginVO.setToken(token);
        loginVO.setUserId(user.getId());
        loginVO.setUsername(user.getUsername());
        loginVO.setRealName(user.getRealName());
        loginVO.setAvatar(user.getAvatar());
        loginVO.setDepartmentId(user.getDepartmentId());
        loginVO.setDepartmentName(user.getDepartmentName());
        
        return Result.success(loginVO);
    }
    
    @GetMapping("/info")
    @Operation(summary = "获取当前用户信息")
    public Result<Map<String, Object>> getUserInfo(@RequestAttribute("userId") Long userId) {
        SysUser user = sysUserService.getById(userId);
        if (user == null) {
            return Result.error("用户不存在");
        }
        
        Map<String, Object> data = new HashMap<>();
        data.put("userId", user.getId());
        data.put("username", user.getUsername());
        data.put("realName", user.getRealName());
        data.put("avatar", user.getAvatar());
        data.put("departmentId", user.getDepartmentId());
        data.put("departmentName", user.getDepartmentName());
        data.put("position", user.getPosition());
        
        return Result.success(data);
    }
    
    @PostMapping("/logout")
    @Operation(summary = "退出登录")
    public Result<Void> logout() {
        return Result.success(null);
    }
    
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }
}