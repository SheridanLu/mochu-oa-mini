package com.mochu.oa.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import cn.hutool.poi.excel.ExcelReader;
import cn.hutool.poi.excel.ExcelUtil;
import com.mochu.oa.common.Result;
import com.mochu.oa.entity.BizApprovalTodo;
import com.mochu.oa.entity.BizIncomeContract;
import com.mochu.oa.entity.BizPurchaseOrder;
import com.mochu.oa.entity.BizInquiryRecord;
import com.mochu.oa.entity.BizPurchaseOrderItem;
import com.mochu.oa.entity.SysRole;
import com.mochu.oa.entity.SysUser;
import com.mochu.oa.entity.SysUserRole;
import com.mochu.oa.mapper.BizApprovalTodoMapper;
import com.mochu.oa.service.BizIncomeContractService;
import com.mochu.oa.service.BizPurchaseOrderService;
import com.mochu.oa.service.BizInquiryRecordService;
import com.mochu.oa.service.BizPurchaseOrderItemService;
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
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@RestController
@RequestMapping("/api/purchase")
@RequiredArgsConstructor
@Tag(name = "采购管理")
public class BizPurchaseOrderController {
    private static final Path UPLOAD_ROOT = Paths.get("/tmp/mochu-uploads").toAbsolutePath().normalize();
    private static final ObjectMapper MAPPER = new ObjectMapper();
    
    private final BizPurchaseOrderService bizPurchaseOrderService;
    private final BizPurchaseOrderItemService bizPurchaseOrderItemService;
    private final BizInquiryRecordService bizInquiryRecordService;
    private final BizIncomeContractService bizIncomeContractService;
    private final BizApprovalTodoMapper bizApprovalTodoMapper;
    private final SysRoleService sysRoleService;
    private final SysUserRoleService sysUserRoleService;
    private final SysUserService sysUserService;
    
    @GetMapping("/list")
    @Operation(summary = "获取采购订单列表")
    public Result<List<BizPurchaseOrder>> list() {
        List<BizPurchaseOrder> list = bizPurchaseOrderService.list();
        return Result.success(list);
    }
    
    @GetMapping("/page")
    @Operation(summary = "分页查询采购订单")
    public Result<Page<BizPurchaseOrder>> page(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer pageNum,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") Integer pageSize,
            @Parameter(description = "项目ID") @RequestParam(required = false) Long projectId,
            @Parameter(description = "状态") @RequestParam(required = false) Integer status) {
        Page<BizPurchaseOrder> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<BizPurchaseOrder> wrapper = new LambdaQueryWrapper<>();
        if (projectId != null) {
            wrapper.eq(BizPurchaseOrder::getProjectId, projectId);
        }
        if (status != null) {
            wrapper.eq(BizPurchaseOrder::getStatus, status);
        }
        wrapper.orderByDesc(BizPurchaseOrder::getCreatedAt);
        Page<BizPurchaseOrder> result = bizPurchaseOrderService.page(page, wrapper);
        return Result.success(result);
    }
    
    @GetMapping("/{id}")
    @Operation(summary = "获取采购订单详情")
    public Result<BizPurchaseOrder> getById(@Parameter(description = "订单ID") @PathVariable Long id) {
        BizPurchaseOrder order = bizPurchaseOrderService.getById(id);
        if (order != null) {
            List<BizPurchaseOrderItem> items = bizPurchaseOrderItemService.list(new LambdaQueryWrapper<BizPurchaseOrderItem>()
                    .eq(BizPurchaseOrderItem::getOrderId, id)
                    .orderByAsc(BizPurchaseOrderItem::getSortOrder, BizPurchaseOrderItem::getId));
            order.setItems(items);
        }
        return Result.success(order);
    }
    
    @PostMapping
    @Operation(summary = "创建采购订单")
    public Result<Map<String, Object>> create(@RequestBody Map<String, Object> payload) {
        BizPurchaseOrder order = MAPPER.convertValue(payload, BizPurchaseOrder.class);
        bizPurchaseOrderService.save(order);
        saveItems(order.getId(), payload.get("items"));
        return Result.success(Map.of("id", order.getId()));
    }
    
    @PutMapping
    @Operation(summary = "更新采购订单")
    public Result<Void> update(@RequestBody Map<String, Object> payload) {
        BizPurchaseOrder order = MAPPER.convertValue(payload, BizPurchaseOrder.class);
        bizPurchaseOrderService.updateById(order);
        saveItems(order.getId(), payload.get("items"));
        return Result.success(null);
    }

    @PostMapping("/{id}/submit")
    @Operation(summary = "提交采购订单审批（草稿→待审批）")
    public Result<Void> submit(@Parameter(description = "订单ID") @PathVariable Long id) {
        BizPurchaseOrder order = bizPurchaseOrderService.getById(id);
        if (order == null) {
            return Result.notFound("采购订单不存在");
        }
        if (order.getStatus() == null || order.getStatus() != 1) {
            return Result.badRequest("仅草稿状态可提交审批");
        }
        BizPurchaseOrder upd = new BizPurchaseOrder();
        upd.setId(id);
        upd.setStatus(2);
        bizPurchaseOrderService.updateById(upd);
        createBudgetTodoIfNeeded(id, "采购清单预算审核");
        return Result.success(null);
    }

    @PostMapping("/generate-from-income-contract")
    @Operation(summary = "从收入合同附件生成项目采购清单并进入预算审核")
    public Result<Map<String, Object>> generateFromIncomeContract(@RequestBody Map<String, Object> payload) {
        Long contractId = asLong(payload.get("contractId"));
        boolean autoSubmit = payload.get("autoSubmit") == null || Boolean.parseBoolean(String.valueOf(payload.get("autoSubmit")));
        if (contractId == null || contractId <= 0) return Result.badRequest("contractId不能为空");
        BizIncomeContract contract = bizIncomeContractService.getById(contractId);
        if (contract == null) return Result.notFound("收入合同不存在");
        String signed = contract.getSignedFilePath();
        if (signed == null || signed.isBlank()) return Result.badRequest("收入合同未上传可解析附件");
        try {
            Map<String, Object> json = MAPPER.readValue(signed, new TypeReference<Map<String, Object>>() {});
            List<String> attachments = extractAttachments(json);
            if (attachments.isEmpty()) return Result.badRequest("收入合同附件为空，无法生成采购清单");
            Path file = pickReadableAttachment(attachments);
            if (file == null) return Result.badRequest("未找到可解析的广联达文件（xlsx/xls/csv/txt）");
            List<Map<String, String>> rows = parseGlodonRows(file);
            if (rows.isEmpty()) return Result.badRequest("未从附件解析到有效清单行");
            BizPurchaseOrder order = createPurchaseOrderFromRows(contract, rows);
            if (autoSubmit) {
                BizPurchaseOrder upd = new BizPurchaseOrder();
                upd.setId(order.getId());
                upd.setStatus(2);
                bizPurchaseOrderService.updateById(upd);
                createBudgetTodoIfNeeded(order.getId(), "收入合同附件生成采购清单预算审核");
            }
            return Result.success(Map.of(
                    "purchaseId", order.getId(),
                    "purchaseNo", order.getOrderNo(),
                    "parsedCount", rows.size(),
                    "submitted", autoSubmit
            ));
        } catch (Exception e) {
            return Result.error("生成采购清单失败: " + e.getMessage());
        }
    }

    @PostMapping("/import-excel")
    @Operation(summary = "导入Excel/CSV采购明细")
    public Result<Map<String, Object>> importExcel(@RequestParam("file") MultipartFile file) {
        try {
            if (file == null || file.isEmpty()) return Result.badRequest("文件不能为空");
            String name = file.getOriginalFilename() == null ? "" : file.getOriginalFilename().toLowerCase(Locale.ROOT);
            List<List<Object>> matrix = new ArrayList<>();
            if (name.endsWith(".xlsx") || name.endsWith(".xls")) {
                try (ExcelReader reader = ExcelUtil.getReader(file.getInputStream())) {
                    matrix = reader.read();
                }
            } else if (name.endsWith(".csv")) {
                String text = new String(file.getBytes(), StandardCharsets.UTF_8);
                for (String line : text.split("\\r?\\n")) {
                    if (line == null || line.isBlank()) continue;
                    matrix.add(new ArrayList<>(Arrays.asList(line.split(",", -1))));
                }
            } else {
                return Result.badRequest("仅支持 xlsx/xls/csv 文件");
            }
            if (matrix.size() < 2) return Result.badRequest("未识别到有效数据");
            List<Object> header = matrix.get(0);
            int idxName = findIdx(header, "物资", "材料", "名称", "name");
            int idxSpec = findIdx(header, "规格", "型号", "spec", "model");
            int idxBrand = findIdx(header, "品牌", "brand");
            int idxUnit = findIdx(header, "单位", "unit");
            int idxQty = findIdx(header, "数量", "工程量", "qty", "quantity");
            int idxPrice = findIdx(header, "单价", "price", "预算单价");
            List<Map<String, Object>> rows = new ArrayList<>();
            List<Map<String, Object>> reportRows = new ArrayList<>();
            for (int i = 1; i < matrix.size(); i++) {
                List<Object> r = matrix.get(i);
                String materialName = cell(r, idxName);
                String specModel = cell(r, idxSpec);
                String brand = cell(r, idxBrand);
                String unit = cell(r, idxUnit);
                String qtyText = cell(r, idxQty);
                String priceText = cell(r, idxPrice);
                if (materialName.isBlank() && qtyText.isBlank()) continue;
                List<String> issues = new ArrayList<>();
                if (materialName.isBlank()) issues.add("物资名称不能为空");
                BigDecimal quantity = parseDecimalStrict(qtyText);
                if (quantity == null) {
                    issues.add("数量格式错误");
                } else if (quantity.compareTo(BigDecimal.ZERO) <= 0) {
                    issues.add("数量必须大于0");
                }
                BigDecimal budgetPrice = parseDecimalStrict(priceText);
                if (!priceText.isBlank() && budgetPrice == null) {
                    issues.add("预算单价格式错误");
                } else if (budgetPrice != null && budgetPrice.compareTo(BigDecimal.ZERO) < 0) {
                    issues.add("预算单价不能为负数");
                }
                boolean valid = issues.isEmpty();
                Map<String, Object> row = new HashMap<>();
                row.put("rowNo", i + 1);
                row.put("materialName", materialName);
                row.put("specModel", specModel);
                row.put("brand", brand);
                row.put("unit", unit);
                row.put("quantity", quantity == null ? BigDecimal.ZERO : quantity);
                row.put("budgetPrice", budgetPrice == null ? BigDecimal.ZERO : budgetPrice);
                row.put("rawQuantity", qtyText);
                row.put("rawBudgetPrice", priceText);
                row.put("valid", valid);
                row.put("issues", issues);
                reportRows.add(row);
                if (valid) rows.add(row);
            }
            return Result.success(Map.of(
                    "count", rows.size(),
                    "rows", rows,
                    "reportRows", reportRows,
                    "validCount", rows.size(),
                    "invalidCount", reportRows.size() - rows.size()
            ));
        } catch (Exception e) {
            return Result.error("Excel导入失败: " + e.getMessage());
        }
    }

    @GetMapping("/import-template")
    @Operation(summary = "下载采购明细导入模板(CSV)")
    public ResponseEntity<byte[]> downloadImportTemplate() {
        String csv = String.join("\n",
                "物资名称,规格型号,品牌,单位,数量,预算单价",
                "钢筋,HRB400 Φ12,宝钢,吨,100,4200",
                "水泥,P.O42.5,海螺,吨,200,380");
        byte[] body = ("\uFEFF" + csv).getBytes(StandardCharsets.UTF_8);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"purchase-import-template.csv\"")
                .contentType(MediaType.parseMediaType("text/csv; charset=UTF-8"))
                .body(body);
    }
    
    @DeleteMapping("/{id}")
    @Operation(summary = "删除采购订单")
    public Result<Void> delete(@Parameter(description = "订单ID") @PathVariable Long id) {
        bizPurchaseOrderService.removeById(id);
        return Result.success(null);
    }
    
    @GetMapping("/price/base/list")
    @Operation(summary = "获取基准价列表")
    public Result<List<Map<String, Object>>> getBasePriceList(
            @Parameter(description = "物资名称") @RequestParam(required = false) String materialName,
            @Parameter(description = "分类ID") @RequestParam(required = false) Long categoryId) {
        return Result.success(List.of());
    }
    
    @GetMapping("/price/standard/list")
    @Operation(summary = "获取标准价列表")
    public Result<List<Map<String, Object>>> getStandardPriceList(
            @Parameter(description = "物资名称") @RequestParam(required = false) String materialName,
            @Parameter(description = "供应商ID") @RequestParam(required = false) Long supplierId) {
        return Result.success(List.of());
    }
    
    @GetMapping("/price/search")
    @Operation(summary = "价格搜索")
    public Result<Map<String, Object>> searchPrice(@RequestBody Map<String, Object> params) {
        Map<String, Object> result = new HashMap<>();
        result.put("basePrice", 100.00);
        result.put("standardPrice", 120.00);
        result.put("marketPrice", 115.00);
        result.put("supplierPrices", List.of());
        return Result.success(result);
    }
    
    @GetMapping("/price/{id}/history")
    @Operation(summary = "价格历史")
    public Result<List<Map<String, Object>>> getPriceHistory(@Parameter(description = "价格ID") @PathVariable Long id) {
        return Result.success(List.of());
    }
    
    @GetMapping("/inquiry/list")
    @Operation(summary = "获取询价单列表")
    public Result<List<Map<String, Object>>> getInquiryList(@RequestParam(required = false) Long projectId) {
        List<BizInquiryRecord> records = bizInquiryRecordService.getByProjectId(projectId);
        List<Map<String, Object>> result = records.stream().map(r -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", r.getId());
            map.put("inquiryNo", r.getInquiryNo());
            map.put("materialName", r.getMaterialName());
            map.put("supplierName", r.getSupplierName());
            map.put("unitPrice", r.getUnitPrice());
            map.put("inquiryDate", r.getInquiryDate());
            map.put("status", r.getStatus());
            return map;
        }).collect(java.util.stream.Collectors.toList());
        return Result.success(result);
    }
    
    @GetMapping("/inquiry/page")
    @Operation(summary = "分页查询询价单")
    public Result<Page<Map<String, Object>>> pageInquiry(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Long projectId) {
        Page<Map<String, Object>> page = new Page<>(pageNum, pageSize);
        return Result.success(page);
    }
    
    @GetMapping("/inquiry/{id}")
    @Operation(summary = "获取询价单详情")
    public Result<Map<String, Object>> getInquiry(@Parameter(description = "询价单ID") @PathVariable Long id) {
        BizInquiryRecord record = bizInquiryRecordService.getById(id);
        Map<String, Object> inquiry = new HashMap<>();
        if (record != null) {
            inquiry.put("id", record.getId());
            inquiry.put("inquiryNo", record.getInquiryNo());
            inquiry.put("materialName", record.getMaterialName());
            inquiry.put("supplierName", record.getSupplierName());
            inquiry.put("unitPrice", record.getUnitPrice());
            inquiry.put("status", record.getStatus());
        }
        return Result.success(inquiry);
    }
    
    @PostMapping("/inquiry")
    @Operation(summary = "创建询价单")
    public Result<Void> createInquiry(@RequestBody BizInquiryRecord inquiry) {
        inquiry.setDeleted(0);
        inquiry.setStatus(1);
        bizInquiryRecordService.save(inquiry);
        return Result.success(null);
    }
    
    @PostMapping("/inquiry/{id}/submit")
    @Operation(summary = "提交询价单")
    public Result<Void> submitInquiry(@Parameter(description = "询价单ID") @PathVariable Long id) {
        BizInquiryRecord record = new BizInquiryRecord();
        record.setId(id);
        record.setStatus(2);
        bizInquiryRecordService.updateById(record);
        return Result.success(null);
    }
    
    @GetMapping("/price/match")
    @Operation(summary = "价格匹配")
    public Result<Map<String, Object>> matchPrice(
            @Parameter(description = "物资ID") @RequestParam Long materialId,
            @Parameter(description = "项目ID") @RequestParam Long projectId) {
        Map<String, Object> result = bizInquiryRecordService.matchPrice(materialId, projectId);
        return Result.success(result);
    }
    
    @GetMapping("/price/compare")
    @Operation(summary = "价格比价")
    public Result<List<Map<String, Object>>> comparePrice(
            @Parameter(description = "物资ID") @RequestParam Long materialId,
            @Parameter(description = "项目ID") @RequestParam Long projectId) {
        List<Map<String, Object>> result = bizInquiryRecordService.comparePrices(materialId, projectId);
        return Result.success(result);
    }
    
    @GetMapping("/price/auto-compare")
    @Operation(summary = "自动比价")
    public Result<Map<String, Object>> autoCompare(
            @Parameter(description = "物资ID") @RequestParam Long materialId,
            @Parameter(description = "项目ID") @RequestParam Long projectId) {
        Map<String, Object> result = bizInquiryRecordService.autoCompare(materialId, projectId);
        return Result.success(result);
    }
    
    @PostMapping("/compare")
    @Operation(summary = "比价")
    public Result<Map<String, Object>> compare(@RequestBody Map<String, Object> params) {
        Map<String, Object> result = new HashMap<>();
        result.put("suppliers", List.of());
        result.put("bestPrice", 100.00);
        result.put("recommendation", "Supplier A");
        return Result.success(result);
    }
    
    @GetMapping("/limit-check")
    @Operation(summary = "零星采购限额检查")
    public Result<Map<String, Object>> checkLimit(
            @Parameter(description = "项目ID") @RequestParam Long projectId,
            @Parameter(description = "本次金额") @RequestParam java.math.BigDecimal amount) {
        Map<String, Object> result = new HashMap<>();
        
        java.math.BigDecimal historicalAmount = new java.math.BigDecimal("0");
        java.math.BigDecimal totalAmount = historicalAmount.add(amount);
        java.math.BigDecimal limitRatio = new java.math.BigDecimal("1.5");
        
        result.put("isOverLimit", false);
        result.put("historicalAmount", historicalAmount);
        result.put("currentAmount", amount);
        result.put("totalAmount", totalAmount);
        result.put("limitRatio", limitRatio);
        result.put("limitAmount", new java.math.BigDecimal("0"));
        result.put("warningLevel", "normal");
        
        if (amount.compareTo(new java.math.BigDecimal("5000")) > 0) {
            result.put("isOverLimit", true);
            result.put("warningLevel", "high");
            result.put("warningMessage", "大额采购需走审批流程");
        }
        
        return Result.success(result);
    }
    
    @PostMapping("/limit-override")
    @Operation(summary = "超限额审批")
    public Result<Void> overrideLimit(@RequestBody Map<String, Object> params) {
        return Result.success(null);
    }
    
    @GetMapping("/match-result/{purchaseId}")
    @Operation(summary = "获取匹配结果")
    public Result<Map<String, Object>> getMatchResult(@PathVariable Long purchaseId) {
        Map<String, Object> result = new HashMap<>();
        result.put("purchaseId", purchaseId);
        result.put("matchSource", "base");
        result.put("basePriceId", 1L);
        result.put("basePrice", new java.math.BigDecimal("5000.00"));
        result.put("matchDetails", List.of());
        return Result.success(result);
    }
    
    @PostMapping("/confirm-price")
    @Operation(summary = "确认价格入库")
    public Result<Void> confirmPrice(@RequestBody Map<String, Object> params) {
        return Result.success(null);
    }

    private void saveItems(Long orderId, Object itemsObj) {
        if (orderId == null) return;
        bizPurchaseOrderItemService.remove(new LambdaQueryWrapper<BizPurchaseOrderItem>()
                .eq(BizPurchaseOrderItem::getOrderId, orderId));
        if (!(itemsObj instanceof List<?> rows) || rows.isEmpty()) return;
        int i = 1;
        for (Object row : rows) {
            if (!(row instanceof Map<?, ?> itemMap)) continue;
            BizPurchaseOrderItem item = new BizPurchaseOrderItem();
            item.setOrderId(orderId);
            item.setMaterialName(str(itemMap.get("materialName")));
            item.setSpecModel(str(itemMap.get("specModel")));
            item.setBrand(str(itemMap.get("brand")));
            item.setUnit(str(itemMap.get("unit")));
            item.setPlanQuantity(num(itemMap.get("quantity")));
            item.setBudgetPrice(num(itemMap.get("budgetPrice")));
            item.setBudgetAmount(item.getPlanQuantity().multiply(item.getBudgetPrice()));
            item.setRowStatus(1);
            item.setSortOrder(i++);
            bizPurchaseOrderItemService.save(item);
        }
    }

    private List<String> extractAttachments(Map<String, Object> json) {
        Object mode = json.get("mode");
        if (mode != null && !"attachment".equals(String.valueOf(mode))) return List.of();
        Object list = json.get("attachments");
        if (!(list instanceof List<?> raw)) return List.of();
        List<String> out = new ArrayList<>();
        for (Object v : raw) {
            if (v == null) continue;
            String s = String.valueOf(v).trim();
            if (!s.isBlank()) out.add(s);
        }
        return out;
    }

    private Path pickReadableAttachment(List<String> attachments) {
        for (String url : attachments) {
            String lower = url.toLowerCase(Locale.ROOT);
            if (!(lower.endsWith(".xlsx") || lower.endsWith(".xls") || lower.endsWith(".csv") || lower.endsWith(".txt"))) continue;
            String relative = url.startsWith("/uploads/") ? url.substring("/uploads/".length()) : url;
            Path candidate = UPLOAD_ROOT.resolve(relative).normalize();
            if (!candidate.startsWith(UPLOAD_ROOT)) continue;
            if (Files.exists(candidate)) return candidate;
        }
        return null;
    }

    private List<Map<String, String>> parseGlodonRows(Path file) throws Exception {
        String name = file.getFileName().toString().toLowerCase(Locale.ROOT);
        List<List<Object>> matrix = new ArrayList<>();
        if (name.endsWith(".xlsx") || name.endsWith(".xls")) {
            try (ExcelReader reader = ExcelUtil.getReader(file.toFile())) {
                matrix = reader.read();
            }
        } else if (name.endsWith(".csv") || name.endsWith(".txt")) {
            String text = Files.readString(file, StandardCharsets.UTF_8);
            for (String line : text.split("\\r?\\n")) {
                if (line == null || line.isBlank()) continue;
                matrix.add(new ArrayList<>(Arrays.asList(line.split(",", -1))));
            }
        }
        if (matrix.size() < 2) return List.of();
        List<Object> header = matrix.get(0);
        int idxName = findIdx(header, "物资", "材料", "名称", "name");
        int idxSpec = findIdx(header, "规格", "型号", "spec", "model");
        int idxUnit = findIdx(header, "单位", "unit");
        int idxQty = findIdx(header, "数量", "工程量", "qty", "quantity");
        int idxPrice = findIdx(header, "单价", "price", "不含税单价", "含税单价");
        List<Map<String, String>> rows = new ArrayList<>();
        for (int i = 1; i < matrix.size(); i++) {
            List<Object> r = matrix.get(i);
            String materialName = cell(r, idxName);
            String qty = cell(r, idxQty);
            if (materialName.isBlank() && qty.isBlank()) continue;
            Map<String, String> map = new HashMap<>();
            map.put("name", materialName);
            map.put("spec", cell(r, idxSpec));
            map.put("unit", cell(r, idxUnit));
            map.put("qty", qty);
            map.put("price", cell(r, idxPrice));
            rows.add(map);
        }
        return rows;
    }

    private BizPurchaseOrder createPurchaseOrderFromRows(BizIncomeContract contract, List<Map<String, String>> rows) {
        BizPurchaseOrder order = new BizPurchaseOrder();
        order.setOrderNo("PO" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyMMdd")) + String.format("%04d", (int) (Math.random() * 10000)));
        order.setProjectId(contract.getProjectId());
        order.setProjectName(contract.getProjectName());
        order.setContractId(contract.getId());
        order.setContractNo(contract.getContractNo());
        order.setSourceType(2);
        order.setStatus(1);
        order.setSubmitterName("收入合同附件导入");
        order.setTotalBudget(BigDecimal.ZERO);
        order.setTotalQuantity(BigDecimal.ZERO);
        order.setAbnormalCount(0);
        bizPurchaseOrderService.save(order);
        BigDecimal totalBudget = BigDecimal.ZERO;
        BigDecimal totalQty = BigDecimal.ZERO;
        int sort = 1;
        for (Map<String, String> r : rows) {
            BizPurchaseOrderItem item = new BizPurchaseOrderItem();
            item.setOrderId(order.getId());
            item.setMaterialName(r.getOrDefault("name", ""));
            item.setSpecModel(r.getOrDefault("spec", ""));
            item.setUnit(r.getOrDefault("unit", ""));
            item.setPlanQuantity(num(r.get("qty")));
            item.setBudgetPrice(num(r.get("price")));
            item.setBudgetAmount(item.getPlanQuantity().multiply(item.getBudgetPrice()));
            item.setRowStatus(1);
            item.setSortOrder(sort++);
            bizPurchaseOrderItemService.save(item);
            totalBudget = totalBudget.add(item.getBudgetAmount());
            totalQty = totalQty.add(item.getPlanQuantity());
        }
        BizPurchaseOrder upd = new BizPurchaseOrder();
        upd.setId(order.getId());
        upd.setTotalBudget(totalBudget);
        upd.setTotalQuantity(totalQty);
        bizPurchaseOrderService.updateById(upd);
        order.setTotalBudget(totalBudget);
        order.setTotalQuantity(totalQty);
        return order;
    }

    private void createBudgetTodoIfNeeded(Long purchaseId, String nodeName) {
        Map<String, Object> budgetUser = resolveBudgetHandler();
        if (budgetUser == null) return;
        Long handlerId = (Long) budgetUser.get("id");
        LambdaQueryWrapper<BizApprovalTodo> query = new LambdaQueryWrapper<BizApprovalTodo>()
                .eq(BizApprovalTodo::getInstanceId, purchaseId)
                .eq(BizApprovalTodo::getNodeOrder, 1)
                .eq(BizApprovalTodo::getHandlerId, handlerId)
                .eq(BizApprovalTodo::getCategory, "TODO")
                .eq(BizApprovalTodo::getStatus, 1);
        BizApprovalTodo exists = bizApprovalTodoMapper.selectOne(query);
        if (exists != null) return;
        BizApprovalTodo todo = new BizApprovalTodo();
        todo.setInstanceId(purchaseId);
        todo.setNodeOrder(1);
        todo.setNodeName(nodeName);
        todo.setHandlerId(handlerId);
        todo.setHandlerName((String) budgetUser.get("name"));
        todo.setCategory("TODO");
        todo.setPriority(2);
        todo.setStatus(1);
        bizApprovalTodoMapper.insert(todo);
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

    private int findIdx(List<Object> header, String... aliases) {
        if (header == null) return -1;
        for (int i = 0; i < header.size(); i++) {
            String text = String.valueOf(header.get(i) == null ? "" : header.get(i)).toLowerCase(Locale.ROOT);
            for (String a : aliases) {
                if (text.contains(a.toLowerCase(Locale.ROOT))) return i;
            }
        }
        return -1;
    }

    private String cell(List<Object> row, int idx) {
        if (row == null || idx < 0 || idx >= row.size() || row.get(idx) == null) return "";
        return String.valueOf(row.get(idx)).trim();
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

    private BigDecimal num(Object v) {
        if (v == null) return BigDecimal.ZERO;
        String s = String.valueOf(v).replaceAll("[^0-9.\\-]", "");
        if (s.isBlank() || "-".equals(s) || ".".equals(s)) return BigDecimal.ZERO;
        try {
            return new BigDecimal(s);
        } catch (Exception e) {
            return BigDecimal.ZERO;
        }
    }

    private BigDecimal parseDecimalStrict(String raw) {
        if (raw == null || raw.trim().isBlank()) return null;
        String s = raw.trim().replace(",", "");
        if (!s.matches("^-?\\d+(\\.\\d+)?$")) return null;
        try {
            return new BigDecimal(s);
        } catch (Exception e) {
            return null;
        }
    }
}