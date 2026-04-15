package com.mochu.oa.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import cn.hutool.poi.excel.ExcelReader;
import cn.hutool.poi.excel.ExcelUtil;
import com.mochu.oa.common.PageRequestGuard;
import com.mochu.oa.common.ParamGuard;
import com.mochu.oa.common.Result;
import com.mochu.oa.entity.BizApprovalTodo;
import com.mochu.oa.entity.BizContractTemplate;
import com.mochu.oa.entity.BizIncomeContract;
import com.mochu.oa.entity.BizPurchaseOrder;
import com.mochu.oa.entity.BizPurchaseOrderItem;
import com.mochu.oa.entity.SysRole;
import com.mochu.oa.entity.SysUser;
import com.mochu.oa.entity.SysUserRole;
import com.mochu.oa.mapper.BizApprovalTodoMapper;
import com.mochu.oa.service.BizContractTemplateService;
import com.mochu.oa.service.BizIncomeContractService;
import com.mochu.oa.service.BizPurchaseOrderItemService;
import com.mochu.oa.service.BizPurchaseOrderService;
import com.mochu.oa.service.SysRoleService;
import com.mochu.oa.service.SysUserRoleService;
import com.mochu.oa.service.SysUserService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@RestController
@RequestMapping("/api/contract/income")
@RequiredArgsConstructor
@Tag(name = "收入合同管理")
public class BizIncomeContractController {
    
    private final BizIncomeContractService bizIncomeContractService;
    private final BizContractTemplateService templateService;
    private final BizPurchaseOrderService purchaseOrderService;
    private final BizPurchaseOrderItemService purchaseOrderItemService;
    private final BizApprovalTodoMapper bizApprovalTodoMapper;
    private final SysRoleService sysRoleService;
    private final SysUserRoleService sysUserRoleService;
    private final SysUserService sysUserService;
    private static final ObjectMapper MAPPER = new ObjectMapper();
    
    @GetMapping("/list")
    @Operation(summary = "获取收入合同列表")
    public Result<List<BizIncomeContract>> list() {
        List<BizIncomeContract> list = bizIncomeContractService.list();
        return Result.success(list);
    }
    
    @GetMapping("/page")
    @Operation(summary = "分页查询收入合同")
    public Result<Page<BizIncomeContract>> page(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer pageNum,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") Integer pageSize,
            @Parameter(description = "合同名称") @RequestParam(required = false) String contractName,
            @Parameter(description = "项目ID") @RequestParam(required = false) Long projectId,
            @Parameter(description = "状态（1-7）") @RequestParam(required = false) Integer status) {
        String pageErr = PageRequestGuard.validate(pageNum, pageSize);
        if (pageErr != null) {
            return Result.badRequest(pageErr);
        }
        pageSize = PageRequestGuard.normalizePageSize(pageSize, 500);
        String statusErr = ParamGuard.validateRange(status, 1, 7, "status");
        if (statusErr != null) {
            return Result.badRequest(statusErr);
        }
        Page<BizIncomeContract> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<BizIncomeContract> wrapper = new LambdaQueryWrapper<>();
        if (contractName != null) {
            wrapper.like(BizIncomeContract::getContractName, contractName);
        }
        if (projectId != null) {
            wrapper.eq(BizIncomeContract::getProjectId, projectId);
        }
        if (status != null) {
            wrapper.eq(BizIncomeContract::getStatus, status);
        }
        Page<BizIncomeContract> result = bizIncomeContractService.page(page, wrapper);
        return Result.success(result);
    }
    
    @GetMapping("/{id}")
    @Operation(summary = "获取收入合同详情")
    public Result<BizIncomeContract> getById(@Parameter(description = "合同ID") @PathVariable Long id) {
        BizIncomeContract contract = bizIncomeContractService.getById(id);
        return Result.success(contract);
    }

    @PostMapping("/import-glodon")
    @Operation(summary = "收入合同广联达文件倒表")
    public Result<Map<String, Object>> importGlodon(@RequestParam("file") MultipartFile file) {
        try {
            if (file == null || file.isEmpty()) {
                return Result.badRequest("文件不能为空");
            }
            String name = file.getOriginalFilename() == null ? "" : file.getOriginalFilename().toLowerCase(Locale.ROOT);
            List<List<Object>> matrix;
            if (name.endsWith(".xlsx") || name.endsWith(".xls")) {
                try (ExcelReader reader = ExcelUtil.getReader(file.getInputStream())) {
                    matrix = reader.read();
                }
            } else if (name.endsWith(".csv") || name.endsWith(".txt")) {
                String text = new String(file.getBytes(), StandardCharsets.UTF_8);
                matrix = new ArrayList<>();
                for (String line : text.split("\\r?\\n")) {
                    if (line == null || line.isBlank()) continue;
                    matrix.add(new ArrayList<>(Arrays.asList(line.split(",", -1))));
                }
            } else {
                return Result.badRequest("仅支持 Excel/CSV/TXT 文件");
            }
            if (matrix.size() < 2) {
                return Result.badRequest("未识别到有效数据");
            }
            List<Object> header = matrix.get(0);
            int idxProject = findIdx(header, "项目名称", "工程名称", "project");
            int idxClient = findIdx(header, "甲方", "建设单位", "业主", "client");
            int idxWithTax = findIdx(header, "含税", "合同金额", "总价", "amount");
            int idxWithoutTax = findIdx(header, "不含税", "未税", "net");
            int idxTaxRate = findIdx(header, "税率", "tax");

            List<Map<String, Object>> rows = new ArrayList<>();
            BigDecimal sumWithTax = BigDecimal.ZERO;
            BigDecimal sumWithoutTax = BigDecimal.ZERO;
            BigDecimal taxRate = BigDecimal.ZERO;
            String projectName = "";
            String partyA = "";
            for (int i = 1; i < matrix.size(); i++) {
                List<Object> row = matrix.get(i);
                String itemProject = cell(row, idxProject);
                String itemClient = cell(row, idxClient);
                BigDecimal withTax = num(cell(row, idxWithTax));
                BigDecimal withoutTax = num(cell(row, idxWithoutTax));
                BigDecimal rowTaxRate = num(cell(row, idxTaxRate));
                if (!itemProject.isBlank() && projectName.isBlank()) projectName = itemProject;
                if (!itemClient.isBlank() && partyA.isBlank()) partyA = itemClient;
                sumWithTax = sumWithTax.add(withTax);
                sumWithoutTax = sumWithoutTax.add(withoutTax);
                if (taxRate.compareTo(BigDecimal.ZERO) == 0 && rowTaxRate.compareTo(BigDecimal.ZERO) > 0) {
                    taxRate = rowTaxRate;
                }
                if (itemProject.isBlank() && withTax.compareTo(BigDecimal.ZERO) == 0 && withoutTax.compareTo(BigDecimal.ZERO) == 0) continue;
                Map<String, Object> one = new HashMap<>();
                one.put("projectName", itemProject);
                one.put("partyA", itemClient);
                one.put("amountWithTax", withTax);
                one.put("amountWithoutTax", withoutTax);
                one.put("taxRate", rowTaxRate);
                rows.add(one);
            }
            if (sumWithoutTax.compareTo(BigDecimal.ZERO) > 0 && taxRate.compareTo(BigDecimal.ZERO) == 0) {
                taxRate = sumWithTax.subtract(sumWithoutTax)
                        .divide(sumWithoutTax, 4, java.math.RoundingMode.HALF_UP);
            }
            String summary = "广联达倒表共" + rows.size() + "行，含税总额=" + sumWithTax
                    + "，不含税总额=" + sumWithoutTax + "，税率=" + taxRate;
            Map<String, Object> data = new HashMap<>();
            data.put("rows", rows);
            data.put("projectName", projectName);
            data.put("partyA", partyA);
            data.put("totalAmountWithTax", sumWithTax);
            data.put("amountWithoutTax", sumWithoutTax);
            data.put("taxRate", taxRate);
            data.put("summary", summary);
            return Result.success(data);
        } catch (Exception e) {
            return Result.error("广联达倒表失败: " + e.getMessage());
        }
    }

    @PostMapping("/create-purchase-draft")
    @Operation(summary = "根据广联达倒表结果生成采购清单草稿并推送预算员待办")
    public Result<Map<String, Object>> createPurchaseDraft(@RequestBody Map<String, Object> params) {
        try {
            Long projectId = asLong(params.get("projectId"));
            String projectName = str(params.get("projectName"));
            String contractNo = str(params.get("contractNo"));
            String contractName = str(params.get("contractName"));
            if (projectId == null || projectId <= 0) {
                return Result.badRequest("projectId不能为空");
            }
            Object rowsObj = params.get("rows");
            if (!(rowsObj instanceof List<?> rows) || rows.isEmpty()) {
                return Result.badRequest("rows不能为空");
            }

            BizPurchaseOrder order = new BizPurchaseOrder();
            order.setOrderNo("PO" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyMMdd")) + String.format("%04d", (int) (Math.random() * 10000)));
            order.setProjectId(projectId);
            order.setProjectName(projectName);
            order.setContractNo(contractNo);
            order.setSourceType(1);
            order.setStatus(1); // 草稿
            order.setTotalBudget(BigDecimal.ZERO);
            order.setTotalQuantity(BigDecimal.ZERO);
            order.setAbnormalCount(0);
            order.setSubmitterName(contractName.isBlank() ? "广联达导入" : contractName);
            purchaseOrderService.save(order);

            BigDecimal totalBudget = BigDecimal.ZERO;
            BigDecimal totalQty = BigDecimal.ZERO;
            int sort = 1;
            for (Object obj : rows) {
                if (!(obj instanceof Map<?, ?> row)) continue;
                String name = str(row.get("name"));
                if (name.isBlank()) name = str(row.get("materialName"));
                String spec = str(row.get("spec"));
                if (spec.isBlank()) spec = str(row.get("specModel"));
                String unit = str(row.get("unit"));
                BigDecimal qty = num(str(row.get("qty")));
                if (qty.compareTo(BigDecimal.ZERO) == 0) qty = num(str(row.get("planQuantity")));
                BigDecimal price = num(str(row.get("price")));
                if (price.compareTo(BigDecimal.ZERO) == 0) price = num(str(row.get("budgetPrice")));
                if (name.isBlank() && qty.compareTo(BigDecimal.ZERO) == 0) continue;
                BizPurchaseOrderItem item = new BizPurchaseOrderItem();
                item.setOrderId(order.getId());
                item.setMaterialName(name);
                item.setSpecModel(spec);
                item.setUnit(unit);
                item.setPlanQuantity(qty);
                item.setBudgetPrice(price);
                item.setBudgetAmount(price.multiply(qty));
                item.setMatchSource(0);
                item.setRowStatus(1);
                item.setSortOrder(sort++);
                purchaseOrderItemService.save(item);
                totalBudget = totalBudget.add(item.getBudgetAmount() == null ? BigDecimal.ZERO : item.getBudgetAmount());
                totalQty = totalQty.add(qty);
            }
            BizPurchaseOrder upd = new BizPurchaseOrder();
            upd.setId(order.getId());
            upd.setTotalBudget(totalBudget);
            upd.setTotalQuantity(totalQty);
            purchaseOrderService.updateById(upd);

            Map<String, Object> budgetUser = resolveBudgetHandler();
            boolean todoCreated = false;
            if (budgetUser != null) {
                BizApprovalTodo todo = new BizApprovalTodo();
                todo.setInstanceId(order.getId());
                todo.setNodeOrder(1);
                todo.setNodeName("广联达导表采购清单预算审核");
                todo.setHandlerId((Long) budgetUser.get("id"));
                todo.setHandlerName((String) budgetUser.get("name"));
                todo.setCategory("TODO");
                todo.setPriority(2);
                todo.setStatus(1);
                bizApprovalTodoMapper.insert(todo);
                todoCreated = true;
            }

            return Result.success(Map.of(
                    "purchaseId", order.getId(),
                    "purchaseNo", order.getOrderNo(),
                    "totalBudget", totalBudget,
                    "totalQuantity", totalQty,
                    "todoCreated", todoCreated
            ));
        } catch (Exception e) {
            return Result.error("生成采购清单草稿失败: " + e.getMessage());
        }
    }
    
    @PostMapping
    @Operation(summary = "创建收入合同")
    public Result<Void> create(@RequestBody BizIncomeContract contract) {
        Result<Void> check = validateTemplate(contract);
        if (check != null) return check;
        bizIncomeContractService.save(contract);
        return Result.success(null);
    }
    
    @PutMapping
    @Operation(summary = "更新收入合同")
    public Result<Void> update(@RequestBody BizIncomeContract contract) {
        Result<Void> check = validateTemplate(contract);
        if (check != null) return check;
        bizIncomeContractService.updateById(contract);
        return Result.success(null);
    }

    @PostMapping("/{id}/submit")
    @Operation(summary = "提交审批（草稿→待审批）")
    public Result<Void> submit(@Parameter(description = "合同ID") @PathVariable Long id) {
        BizIncomeContract c = bizIncomeContractService.getById(id);
        if (c == null) {
            return Result.notFound("合同不存在");
        }
        if (c.getStatus() == null || c.getStatus() != 1) {
            return Result.badRequest("仅草稿状态可提交审批");
        }
        BizIncomeContract upd = new BizIncomeContract();
        upd.setId(id);
        upd.setStatus(2);
        bizIncomeContractService.updateById(upd);
        return Result.success(null);
    }
    
    @DeleteMapping("/{id}")
    @Operation(summary = "删除收入合同")
    public Result<Void> delete(@Parameter(description = "合同ID") @PathVariable Long id) {
        bizIncomeContractService.removeById(id);
        return Result.success(null);
    }

    private Result<Void> validateTemplate(BizIncomeContract contract) {
        if (contract == null || contract.getTemplateId() == null) {
            return null;
        }
        BizContractTemplate t = templateService.getById(contract.getTemplateId());
        if (t == null || t.getStatus() == null || t.getStatus() != 1) {
            return Result.badRequest("合同模板不存在或已停用");
        }
        if (contract.getTemplateVersion() == null || !contract.getTemplateVersion().equals(t.getTemplateVersion())) {
            return Result.badRequest("模板版本已更新，请重新加载合同模板后再保存");
        }
        String hash = contract.getTemplateContentHash() == null ? "" : contract.getTemplateContentHash().trim();
        if (hash.isEmpty() || !hash.equalsIgnoreCase(t.getContentHash())) {
            return Result.badRequest("模板内容校验失败，请重新选择模板");
        }
        try {
            Map<String, Object> payload = MAPPER.readValue(
                    contract.getSignedFilePath() == null ? "{}" : contract.getSignedFilePath(),
                    new TypeReference<>() {});
            Object fields = payload.get("fields");
            if (!(fields instanceof Map<?, ?> map) || map.isEmpty()) {
                return Result.badRequest("请填写合同正文中的可编辑区域");
            }
        } catch (Exception e) {
            return Result.badRequest("合同正文格式错误，请重新填写模板字段");
        }
        return null;
    }

    private int findIdx(List<Object> header, String... aliases) {
        if (header == null) return -1;
        List<String> keys = Arrays.stream(aliases).map(x -> x.toLowerCase(Locale.ROOT)).toList();
        for (int i = 0; i < header.size(); i++) {
            String h = String.valueOf(header.get(i) == null ? "" : header.get(i)).trim().toLowerCase(Locale.ROOT);
            for (String k : keys) {
                if (h.contains(k)) return i;
            }
        }
        return -1;
    }

    private String cell(List<Object> row, int idx) {
        if (idx < 0 || row == null || idx >= row.size()) return "";
        return String.valueOf(row.get(idx) == null ? "" : row.get(idx)).trim();
    }

    private BigDecimal num(String raw) {
        if (raw == null || raw.isBlank()) return BigDecimal.ZERO;
        String cleaned = raw.replace(",", "").replace("%", "").trim();
        try {
            BigDecimal n = new BigDecimal(cleaned);
            if (raw.contains("%")) {
                return n.divide(BigDecimal.valueOf(100), 4, java.math.RoundingMode.HALF_UP);
            }
            return n;
        } catch (Exception ignore) {
            return BigDecimal.ZERO;
        }
    }

    private Long asLong(Object v) {
        if (v == null) return null;
        if (v instanceof Number n) return n.longValue();
        try {
            return Long.parseLong(String.valueOf(v).trim());
        } catch (Exception e) {
            return null;
        }
    }

    private String str(Object v) {
        return v == null ? "" : String.valueOf(v).trim();
    }

    private Map<String, Object> resolveBudgetHandler() {
        List<Long> budgetRoleIds = sysRoleService.list().stream()
                .filter(r -> r.getStatus() != null && r.getStatus() == 1)
                .filter(r -> {
                    String code = r.getRoleCode() == null ? "" : r.getRoleCode().toLowerCase(Locale.ROOT);
                    String name = r.getRoleName() == null ? "" : r.getRoleName().toLowerCase(Locale.ROOT);
                    return code.contains("budget") || name.contains("预算");
                })
                .map(SysRole::getId)
                .toList();
        if (budgetRoleIds.isEmpty()) return null;
        List<Long> userIds = sysUserRoleService.list(new LambdaQueryWrapper<SysUserRole>()
                        .in(SysUserRole::getRoleId, budgetRoleIds))
                .stream().map(SysUserRole::getUserId).distinct().toList();
        if (userIds.isEmpty()) return null;
        SysUser u = sysUserService.listByIds(userIds).stream()
                .filter(x -> x.getStatus() != null && x.getStatus() == 1)
                .findFirst().orElse(null);
        if (u == null) return null;
        Map<String, Object> m = new HashMap<>();
        m.put("id", u.getId());
        m.put("name", (u.getRealName() == null || u.getRealName().isBlank()) ? u.getUsername() : u.getRealName());
        return m;
    }
}