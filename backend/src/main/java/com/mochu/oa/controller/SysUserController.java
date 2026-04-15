package com.mochu.oa.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.mochu.oa.common.Result;
import com.mochu.oa.entity.SysUser;
import com.mochu.oa.service.SysUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/system/user")
@RequiredArgsConstructor
@Tag(name = "用户管理")
public class SysUserController {
    
    private final SysUserService sysUserService;
    private final PasswordEncoder passwordEncoder;
    
    @GetMapping("/list")
    @Operation(summary = "分页查询用户")
    public Result<Map<String, Object>> list(
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String realName,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        IPage<SysUser> pageResult = sysUserService.pageUsers(username, realName, status, page, size);
        return Result.success(Map.of(
                "list", pageResult.getRecords(),
                "total", pageResult.getTotal(),
                "pages", pageResult.getPages(),
                "current", pageResult.getCurrent()
        ));
    }

    @GetMapping("/select-list")
    @Operation(summary = "用户下拉（部门负责人等，不分页）")
    public Result<List<SysUser>> selectList() {
        return Result.success(sysUserService.listForSelect());
    }
    
    @GetMapping("/{id}")
    @Operation(summary = "获取用户详情")
    public Result<SysUser> getById(@PathVariable Long id) {
        SysUser user = sysUserService.getById(id);
        return Result.success(user);
    }
    
    @PostMapping
    @Operation(summary = "创建用户")
    public Result<Void> create(@RequestBody SysUser user) {
        if (!StringUtils.hasText(user.getUsername())) {
            return Result.badRequest("用户名不能为空");
        }
        if (!StringUtils.hasText(user.getPassword())) {
            return Result.badRequest("密码不能为空");
        }
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        sysUserService.save(user);
        return Result.success(null);
    }
    
    @PutMapping
    @Operation(summary = "更新用户")
    public Result<Void> update(@RequestBody SysUser user) {
        if (user.getId() == null) {
            return Result.badRequest("用户ID不能为空");
        }
        SysUser existing = sysUserService.getById(user.getId());
        if (existing == null) {
            return Result.notFound("用户不存在");
        }
        if (StringUtils.hasText(user.getPassword())) {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        } else {
            user.setPassword(existing.getPassword());
        }
        sysUserService.updateById(user);
        return Result.success(null);
    }
    
    @DeleteMapping("/{id}")
    @Operation(summary = "删除用户")
    public Result<Void> delete(@PathVariable Long id) {
        sysUserService.removeById(id);
        return Result.success(null);
    }
    
    @PutMapping("/{id}/roles")
    @Operation(summary = "更新用户角色")
    public Result<Void> updateRoles(@PathVariable Long id, @RequestBody List<Long> roleIds) {
        sysUserService.updateUserRoles(id, roleIds);
        return Result.success(null);
    }
}