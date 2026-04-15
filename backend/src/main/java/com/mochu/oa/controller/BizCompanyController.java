package com.mochu.oa.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.mochu.oa.common.Result;
import com.mochu.oa.entity.BizCompany;
import com.mochu.oa.service.BizCompanyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api/company")
@RequiredArgsConstructor
@Tag(name = "公司信息")
public class BizCompanyController {
    
    private final BizCompanyService companyService;
    
    @GetMapping("/search")
    @Operation(summary = "搜索公司")
    public Result<Map<String, Object>> search(
            @RequestParam(required = false) String companyName,
            @RequestParam(required = false) String creditCode,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        IPage<BizCompany> pageResult = companyService.search(companyName, creditCode, page, size);
        return Result.success(Map.of(
            "list", pageResult.getRecords(),
            "total", pageResult.getTotal(),
            "pages", pageResult.getPages(),
            "current", pageResult.getCurrent()
        ));
    }
    
    @GetMapping("/{id}")
    @Operation(summary = "获取公司详情")
    public Result<BizCompany> get(@PathVariable Long id) {
        BizCompany company = companyService.getById(id);
        return Result.success(company);
    }
    
    @PostMapping
    @Operation(summary = "创建公司")
    public Result<Void> create(@RequestBody BizCompany company) {
        company.setCreatedAt(LocalDateTime.now());
        companyService.save(company);
        return Result.success(null);
    }
    
    @PutMapping
    @Operation(summary = "更新公司")
    public Result<Void> update(@RequestBody BizCompany company) {
        company.setUpdatedAt(LocalDateTime.now());
        companyService.updateById(company);
        return Result.success(null);
    }
}