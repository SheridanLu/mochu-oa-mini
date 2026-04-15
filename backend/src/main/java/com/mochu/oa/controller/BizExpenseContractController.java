package com.mochu.oa.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import cn.hutool.poi.excel.ExcelReader;
import cn.hutool.poi.excel.ExcelUtil;
import com.mochu.oa.common.PageRequestGuard;
import com.mochu.oa.common.ParamGuard;
import com.mochu.oa.common.Result;
import com.mochu.oa.entity.BizContractTemplate;
import com.mochu.oa.entity.BizExpenseContract;
import com.mochu.oa.service.BizContractTemplateService;
import com.mochu.oa.service.BizExpenseContractService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RestController
@RequestMapping("/api/contract/expense")
@RequiredArgsConstructor
@Tag(name = "支出合同管理")
public class BizExpenseContractController {
    
    private static final Path UPLOAD_ROOT = Paths.get("/tmp/mochu-uploads").toAbsolutePath().normalize();
    private static final Pattern PLACEHOLDER_PATTERN = Pattern.compile("\\{\\{\\s*([^{}\\s]+)\\s*}}");
    private static final Pattern UNDERLINE_PATTERN = Pattern.compile("_{3,}");
    private static final Set<String> EXPENSE_CATEGORIES = new HashSet<>(Arrays.asList(
            "EQUIPMENT", "MATERIAL", "LABOR", "SOFTWARE", "OTHER", "PROFESSIONAL_SUBCONTRACT"
    ));

    private final BizExpenseContractService bizExpenseContractService;
    private final BizContractTemplateService templateService;
    private static final ObjectMapper MAPPER = new ObjectMapper();
    
    @GetMapping("/list")
    @Operation(summary = "获取支出合同列表")
    public Result<List<BizExpenseContract>> list() {
        List<BizExpenseContract> list = bizExpenseContractService.list();
        return Result.success(list);
    }

    @GetMapping("/categories")
    @Operation(summary = "获取支出合同分类字典")
    public Result<List<Map<String, String>>> categories() {
        return Result.success(List.of(
                Map.of("value", "EQUIPMENT", "label", "设备合同"),
                Map.of("value", "MATERIAL", "label", "材料合同"),
                Map.of("value", "LABOR", "label", "劳务合同"),
                Map.of("value", "SOFTWARE", "label", "软件合同"),
                Map.of("value", "OTHER", "label", "其他合同"),
                Map.of("value", "PROFESSIONAL_SUBCONTRACT", "label", "专业分包合同")
        ));
    }

    @PostMapping("/import-items")
    @Operation(summary = "导入设备/材料清单（倒表）")
    public Result<Map<String, Object>> importItems(
            @RequestParam("file") MultipartFile file,
            @RequestParam("category") String category) {
        try {
            String c = category == null ? "" : category.trim().toUpperCase();
            if (!"EQUIPMENT".equals(c) && !"MATERIAL".equals(c)) {
                return Result.badRequest("仅设备合同或材料合同支持清单倒表");
            }
            if (file == null || file.isEmpty()) {
                return Result.badRequest("清单文件不能为空");
            }
            String name = file.getOriginalFilename() == null ? "" : file.getOriginalFilename().toLowerCase();
            List<Map<String, Object>> rows = name.endsWith(".csv")
                    ? parseCsvRows(file)
                    : parseExcelRows(file);
            if (rows.isEmpty()) {
                return Result.badRequest("未解析到有效清单数据");
            }
            String merged = buildMergedTableText(rows, c);
            return Result.success(Map.of(
                    "rows", rows,
                    "count", rows.size(),
                    "mergedText", merged
            ));
        } catch (Exception e) {
            return Result.error("清单导入失败: " + e.getMessage());
        }
    }
    
    @GetMapping("/page")
    @Operation(summary = "分页查询支出合同")
    public Result<Page<BizExpenseContract>> page(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer pageNum,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") Integer pageSize,
            @Parameter(description = "合同名称") @RequestParam(required = false) String contractName,
            @Parameter(description = "项目ID") @RequestParam(required = false) Long projectId,
            @Parameter(description = "状态（1-7）") @RequestParam(required = false) Integer status,
            @Parameter(description = "支出合同分类") @RequestParam(required = false) String expenseCategory) {
        String pageErr = PageRequestGuard.validate(pageNum, pageSize);
        if (pageErr != null) {
            return Result.badRequest(pageErr);
        }
        pageSize = PageRequestGuard.normalizePageSize(pageSize, 500);
        String statusErr = ParamGuard.validateRange(status, 1, 7, "status");
        if (statusErr != null) {
            return Result.badRequest(statusErr);
        }
        Page<BizExpenseContract> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<BizExpenseContract> wrapper = new LambdaQueryWrapper<>();
        if (contractName != null) {
            wrapper.like(BizExpenseContract::getContractName, contractName);
        }
        if (projectId != null) {
            wrapper.eq(BizExpenseContract::getProjectId, projectId);
        }
        if (status != null) {
            wrapper.eq(BizExpenseContract::getStatus, status);
        }
        if (expenseCategory != null && !expenseCategory.isBlank()) {
            wrapper.eq(BizExpenseContract::getContractType, expenseCategory.trim().toUpperCase());
        }
        Page<BizExpenseContract> result = bizExpenseContractService.page(page, wrapper);
        return Result.success(result);
    }
    
    @GetMapping("/{id}")
    @Operation(summary = "获取支出合同详情")
    public Result<BizExpenseContract> getById(@Parameter(description = "合同ID") @PathVariable Long id) {
        BizExpenseContract contract = bizExpenseContractService.getById(id);
        return Result.success(contract);
    }
    
    @PostMapping
    @Operation(summary = "创建支出合同")
    public Result<Void> create(@RequestBody BizExpenseContract contract) {
        Result<Void> check = validateTemplate(contract);
        if (check != null) return check;
        bizExpenseContractService.save(contract);
        return Result.success(null);
    }
    
    @PutMapping
    @Operation(summary = "更新支出合同")
    public Result<Void> update(@RequestBody BizExpenseContract contract) {
        Result<Void> check = validateTemplate(contract);
        if (check != null) return check;
        bizExpenseContractService.updateById(contract);
        return Result.success(null);
    }
    
    @DeleteMapping("/{id}")
    @Operation(summary = "删除支出合同")
    public Result<Void> delete(@Parameter(description = "合同ID") @PathVariable Long id) {
        bizExpenseContractService.removeById(id);
        return Result.success(null);
    }

    @PostMapping("/print/generate")
    @Operation(summary = "生成支出合同水印打印文件")
    public Result<Map<String, Object>> generatePrintFile(@RequestBody BizExpenseContract contract) {
        Result<Void> check = validateTemplate(contract);
        if (check != null) {
            return Result.error(check.getCode(), check.getMessage());
        }
        try {
            BizContractTemplate t = templateService.getById(contract.getTemplateId());
            Map<String, Object> payload = MAPPER.readValue(contract.getSignedFilePath(), new TypeReference<>() {});
            Map<String, Object> fields = (Map<String, Object>) payload.get("fields");
            String rendered = renderTemplate(t.getTemplateContent(), fields);
            String title = contract.getContractName() == null || contract.getContractName().isBlank()
                    ? "支出合同"
                    : contract.getContractName().trim();
            String html = buildPrintHtml(title, rendered);

            String day = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
            Path dir = UPLOAD_ROOT.resolve("print").resolve(day).normalize();
            Files.createDirectories(dir);
            String fileName = "expense-contract-" + UUID.randomUUID().toString().replace("-", "") + ".html";
            Path file = dir.resolve(fileName);
            Files.writeString(file, html, StandardCharsets.UTF_8);

            String relative = "print/" + day + "/" + fileName;
            String url = "/api/common/file?path=" + relative;
            persistPrintArchive(contract, relative, url);
            return Result.success(Map.of(
                    "filePath", relative,
                    "previewUrl", url,
                    "url", url,
                    "fileName", fileName
            ));
        } catch (Exception e) {
            return Result.error("生成打印文件失败: " + e.getMessage());
        }
    }

    private Result<Void> validateTemplate(BizExpenseContract contract) {
        if (contract == null) {
            return Result.badRequest("合同数据不能为空");
        }
        if (contract.getTemplateId() == null) {
            return Result.badRequest("支出合同必须选择合同模板");
        }
        String category = contract.getContractType() == null ? "" : contract.getContractType().trim().toUpperCase();
        if (category.isEmpty()) {
            return Result.badRequest("支出合同分类不能为空");
        }
        if (!EXPENSE_CATEGORIES.contains(category)) {
            return Result.badRequest("支出合同分类非法");
        }
        contract.setContractType(category);
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
            @SuppressWarnings("unchecked")
            Map<String, Object> fieldMap = (Map<String, Object>) fields;
            String categoryRuleErr = validateCategoryRules(category, fieldMap);
            if (categoryRuleErr != null) {
                return Result.badRequest(categoryRuleErr);
            }
        } catch (Exception e) {
            return Result.badRequest("合同正文格式错误，请重新填写模板字段");
        }
        return null;
    }

    private String validateCategoryRules(String category, Map<String, Object> fields) {
        if (!hasAnyFilledField(fields, "物资名称", "名称", "material", "item")) return "请填写物资名称";
        if (!hasAnyFilledField(fields, "单位", "unit")) return "请填写单位";
        if (!hasAnyFilledField(fields, "数量", "qty", "quantity")) return "请填写数量";
        if (("EQUIPMENT".equals(category) || "MATERIAL".equals(category))
                && !hasAnyFilledField(fields, "品牌", "brand")) {
            return "设备/材料合同需填写品牌";
        }
        if ("EQUIPMENT".equals(category) && !hasAnyFilledField(fields, "规格", "型号", "spec", "model")) {
            return "设备合同需填写产品规格/型号";
        }
        if ("MATERIAL".equals(category) && !hasAnyFilledField(fields, "规格", "型号", "spec", "model")) {
            return "材料合同需填写规格/型号";
        }
        if ("LABOR".equals(category) && !hasAnyFilledField(fields, "工种", "班组", "labor", "team")) {
            return "劳务合同需填写工种/班组信息";
        }
        if ("SOFTWARE".equals(category) && !hasAnyFilledField(fields, "授权", "license", "许可")) {
            return "软件合同需填写授权/许可信息";
        }
        if ("PROFESSIONAL_SUBCONTRACT".equals(category)
                && !hasAnyFilledField(fields, "分包范围", "subcontract", "专业")) {
            return "专业分包合同需填写分包范围";
        }
        return null;
    }

    private boolean hasAnyFilledField(Map<String, Object> fields, String... aliases) {
        if (fields == null || fields.isEmpty()) {
            return false;
        }
        List<String> aliasList = Arrays.stream(aliases).map(String::toLowerCase).toList();
        for (Map.Entry<String, Object> entry : fields.entrySet()) {
            String key = entry.getKey() == null ? "" : entry.getKey().toLowerCase();
            String value = entry.getValue() == null ? "" : String.valueOf(entry.getValue()).trim();
            if (value.isEmpty()) {
                continue;
            }
            for (String alias : aliasList) {
                if (key.contains(alias)) {
                    return true;
                }
            }
        }
        return false;
    }

    private List<Map<String, Object>> parseExcelRows(MultipartFile file) throws Exception {
        try (ExcelReader reader = ExcelUtil.getReader(file.getInputStream())) {
            List<List<Object>> all = reader.read();
            return mapRowsFromMatrix(all);
        }
    }

    private List<Map<String, Object>> parseCsvRows(MultipartFile file) throws Exception {
        String text = new String(file.getBytes(), StandardCharsets.UTF_8);
        String[] lines = text.split("\\r?\\n");
        List<List<Object>> matrix = new ArrayList<>();
        for (String line : lines) {
            if (line == null || line.isBlank()) continue;
            String[] arr = line.split(",", -1);
            List<Object> row = new ArrayList<>();
            row.addAll(Arrays.asList(arr));
            matrix.add(row);
        }
        return mapRowsFromMatrix(matrix);
    }

    private List<Map<String, Object>> mapRowsFromMatrix(List<List<Object>> matrix) {
        List<Map<String, Object>> out = new ArrayList<>();
        if (matrix == null || matrix.size() < 2) return out;
        List<Object> header = matrix.get(0);
        int idxName = findHeaderIndex(header, "名称", "物资名称", "设备名称", "材料名称", "name", "item");
        int idxSpec = findHeaderIndex(header, "规格", "型号", "规格型号", "spec", "model");
        int idxBrand = findHeaderIndex(header, "品牌", "brand");
        int idxUnit = findHeaderIndex(header, "单位", "unit");
        int idxQty = findHeaderIndex(header, "数量", "qty", "quantity");
        int idxPrice = findHeaderIndex(header, "单价", "price");
        for (int i = 1; i < matrix.size(); i++) {
            List<Object> row = matrix.get(i);
            String itemName = pickCell(row, idxName);
            String spec = pickCell(row, idxSpec);
            String brand = pickCell(row, idxBrand);
            String unit = pickCell(row, idxUnit);
            String qty = pickCell(row, idxQty);
            String price = pickCell(row, idxPrice);
            if (itemName.isBlank() && qty.isBlank()) continue;
            Map<String, Object> one = new java.util.LinkedHashMap<>();
            one.put("name", itemName);
            one.put("spec", spec);
            one.put("brand", brand);
            one.put("unit", unit);
            one.put("qty", qty);
            one.put("price", price);
            out.add(one);
        }
        return out;
    }

    private int findHeaderIndex(List<Object> header, String... aliases) {
        if (header == null || header.isEmpty()) return -1;
        List<String> as = Arrays.stream(aliases).map(String::toLowerCase).toList();
        for (int i = 0; i < header.size(); i++) {
            String h = String.valueOf(header.get(i) == null ? "" : header.get(i)).trim().toLowerCase();
            for (String a : as) {
                if (h.contains(a)) return i;
            }
        }
        return -1;
    }

    private String pickCell(List<Object> row, int idx) {
        if (idx < 0 || row == null || idx >= row.size()) return "";
        return String.valueOf(row.get(idx) == null ? "" : row.get(idx)).trim();
    }

    private String buildMergedTableText(List<Map<String, Object>> rows, String category) {
        String title = "EQUIPMENT".equals(category) ? "设备清单" : "材料清单";
        StringBuilder sb = new StringBuilder(title).append("：\n");
        int i = 1;
        for (Map<String, Object> row : rows) {
            sb.append(i++).append(". ")
                    .append(nvlObj(row.get("name")))
                    .append("，规格/型号: ").append(nvlObj(row.get("spec")))
                    .append("，品牌: ").append(nvlObj(row.get("brand")))
                    .append("，单位: ").append(nvlObj(row.get("unit")))
                    .append("，数量: ").append(nvlObj(row.get("qty")))
                    .append("，单价: ").append(nvlObj(row.get("price")))
                    .append("\n");
        }
        return sb.toString().trim();
    }

    private String nvlObj(Object v) {
        return v == null ? "" : String.valueOf(v).trim();
    }

    private String renderTemplate(String content, Map<String, Object> fields) {
        String safe = content == null ? "" : content;
        Map<String, Object> f = fields == null ? Map.of() : fields;
        Matcher named = PLACEHOLDER_PATTERN.matcher(safe);
        StringBuilder namedSb = new StringBuilder();
        while (named.find()) {
            String key = named.group(1) == null ? "" : named.group(1).trim();
            String value = f.get(key) == null ? "______" : String.valueOf(f.get(key));
            named.appendReplacement(namedSb, Matcher.quoteReplacement("<span class=\"field\">" + escapeHtml(value) + "</span>"));
        }
        named.appendTail(namedSb);

        Matcher underline = UNDERLINE_PATTERN.matcher(namedSb.toString());
        StringBuilder finalSb = new StringBuilder();
        int idx = 1;
        while (underline.find()) {
            String key = "FIELD_" + idx++;
            String value = f.get(key) == null ? "______" : String.valueOf(f.get(key));
            underline.appendReplacement(finalSb, Matcher.quoteReplacement("<span class=\"field\">" + escapeHtml(value) + "</span>"));
        }
        underline.appendTail(finalSb);
        return finalSb.toString().replace("\n", "<br/>");
    }

    private String buildPrintHtml(String title, String body) {
        String watermark = "<span>MOCHU-OA 支出合同</span>";
        String marks = watermark.repeat(18);
        return "<!doctype html><html><head><meta charset=\"utf-8\"/><title>" + escapeHtml(title) + "</title>"
                + "<style>"
                + "body{font-family:-apple-system,BlinkMacSystemFont,\"Segoe UI\",sans-serif;margin:32px;color:#303133;}"
                + ".doc{position:relative;line-height:1.9;white-space:normal;}"
                + ".field{background:#fff;border-bottom:1px solid #303133;padding:0 6px;display:inline-block;min-width:90px}"
                + ".wm{position:fixed;inset:0;display:grid;grid-template-columns:repeat(3,1fr);opacity:.08;pointer-events:none;z-index:0}"
                + ".wm span{transform:rotate(-25deg);font-size:22px;color:#000;align-self:center;justify-self:center;user-select:none}"
                + ".doc-wrap{position:relative;z-index:1}"
                + "@media print{.no-print{display:none}}"
                + "</style></head><body>"
                + "<div class=\"wm\">" + marks + "</div>"
                + "<div class=\"doc-wrap\"><h2>" + escapeHtml(title) + "</h2><div class=\"doc\">" + body + "</div></div>"
                + "<script>window.print();</script>"
                + "</body></html>";
    }

    private String escapeHtml(String input) {
        if (input == null) {
            return "";
        }
        return input
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }

    @SuppressWarnings("unchecked")
    private void persistPrintArchive(BizExpenseContract contract, String relativePath, String previewUrl) {
        if (contract == null || contract.getId() == null) {
            return;
        }
        try {
            BizExpenseContract db = bizExpenseContractService.getById(contract.getId());
            if (db == null) {
                return;
            }
            Map<String, Object> payload = MAPPER.readValue(
                    db.getSignedFilePath() == null ? "{}" : db.getSignedFilePath(),
                    new TypeReference<>() {});
            List<Map<String, Object>> archives;
            Object history = payload.get("printArchives");
            if (history instanceof List<?> list) {
                archives = new ArrayList<>();
                for (Object item : list) {
                    if (item instanceof Map<?, ?> map) {
                        archives.add((Map<String, Object>) map);
                    }
                }
            } else {
                archives = new ArrayList<>();
            }
            archives.add(Map.of(
                    "filePath", relativePath,
                    "previewUrl", previewUrl,
                    "generatedAt", LocalDateTime.now().toString()
            ));
            payload.put("printArchives", archives);
            BizExpenseContract upd = new BizExpenseContract();
            upd.setId(db.getId());
            upd.setSignedFilePath(MAPPER.writeValueAsString(payload));
            bizExpenseContractService.updateById(upd);
        } catch (Exception ignored) {
            // 打印文件已生成，归档回写失败不影响主流程
        }
    }
}