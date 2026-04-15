package com.mochu.oa.controller;

import com.mochu.oa.common.Result;
import com.mochu.oa.common.JwtUtils;
import com.mochu.oa.dto.ChangePasswordDTO;
import com.mochu.oa.dto.LoginDTO;
import com.mochu.oa.entity.SysRole;
import com.mochu.oa.entity.SysUser;
import com.mochu.oa.entity.SysUserRole;
import com.mochu.oa.service.SysRoleService;
import com.mochu.oa.service.SysUserService;
import com.mochu.oa.service.SysUserRoleService;
import com.mochu.oa.vo.LoginVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "认证管理")
public class LoginController {

    private static final Pattern PASSWORD_RULE = Pattern.compile("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[~!@#$%^&*()_+\\-={}\\[\\]|:;\"'<>,.?/]).{8,20}$");
    private static final String ADMIN_USERNAME = "admin";
    
    private final SysUserService sysUserService;
    private final SysRoleService sysRoleService;
    private final SysUserRoleService sysUserRoleService;
    private final JwtUtils jwtUtils;
    private final PasswordEncoder passwordEncoder;
    
    @PostMapping("/login")
    @Operation(summary = "用户名密码登录")
    public Result<LoginVO> login(@Valid @RequestBody LoginDTO loginDTO, HttpServletRequest request) {
        SysUser user = sysUserService.login(loginDTO.getUsername(), loginDTO.getPassword(), passwordEncoder);
        if (user == null) {
            return Result.error("用户名或密码错误");
        }
        
        boolean mustChangePassword = user.getLastLoginTime() == null && !isAdminAccount(user);
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
        loginVO.setMustChangePassword(mustChangePassword);
        
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

    @PostMapping("/change-password")
    @Operation(summary = "登录后修改密码")
    public Result<Void> changePassword(
            @RequestAttribute(value = "userId", required = false) Long userId,
            @Valid @RequestBody ChangePasswordDTO dto) {
        if (userId == null) {
            return Result.unauthorized("未登录或登录已失效");
        }
        SysUser user = sysUserService.getById(userId);
        if (user == null) {
            return Result.error("用户不存在");
        }
        if (!passwordEncoder.matches(dto.getOldPassword(), user.getPassword())) {
            return Result.error("旧密码错误");
        }
        if (!dto.getNewPassword().equals(dto.getConfirmPassword())) {
            return Result.error("两次输入的新密码不一致");
        }
        if (dto.getNewPassword().equals(dto.getOldPassword())) {
            return Result.error("新密码不能与旧密码相同");
        }
        if (!PASSWORD_RULE.matcher(dto.getNewPassword()).matches()) {
            return Result.error("密码必须8-20位，且包含大小写字母、数字和特殊字符");
        }
        SysUser updateUser = new SysUser();
        updateUser.setId(userId);
        updateUser.setPassword(passwordEncoder.encode(dto.getNewPassword()));
        sysUserService.updateById(updateUser);
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

    private boolean isAdminAccount(SysUser user) {
        if (user == null) {
            return false;
        }
        if (ADMIN_USERNAME.equalsIgnoreCase(user.getUsername())) {
            return true;
        }
        List<Long> roleIds = sysUserRoleService.list(new LambdaQueryWrapper<SysUserRole>()
                        .eq(SysUserRole::getUserId, user.getId()))
                .stream()
                .map(SysUserRole::getRoleId)
                .toList();
        if (roleIds.isEmpty()) {
            return false;
        }
        return sysRoleService.listByIds(roleIds).stream()
                .map(SysRole::getRoleCode)
                .filter(code -> code != null && !code.isBlank())
                .map(String::toLowerCase)
                .anyMatch(code -> code.contains("admin"));
    }
}