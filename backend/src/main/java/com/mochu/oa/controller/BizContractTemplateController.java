package com.mochu.oa.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.mochu.oa.common.Result;
import com.mochu.oa.entity.BizContractTemplate;
import com.mochu.oa.entity.BizProject;
import com.mochu.oa.entity.BizSupplier;
import com.mochu.oa.service.BizContractTemplateService;
import com.mochu.oa.service.BizProjectService;
import com.mochu.oa.service.BizSupplierService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.security.MessageDigest;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@RestController
@RequestMapping("/api/contract/template")
@RequiredArgsConstructor
@Tag(name = "合同模板管理")
public class BizContractTemplateController {

    private static final Pattern PLACEHOLDER_PATTERN = Pattern.compile("\\{\\{\\s*([^{}\\s]+)\\s*}}");
    private static final Pattern UNDERLINE_PATTERN = Pattern.compile("_{3,}");

    private final BizContractTemplateService templateService;
    private final BizProjectService projectService;
    private final BizSupplierService supplierService;

    @GetMapping("/list")
    @Operation(summary = "模板列表")
    public Result<List<BizContractTemplate>> list(
            @RequestParam(required = false) Integer contractType,
            @RequestParam(required = false, defaultValue = "1") Integer onlyEnabled) {
        LambdaQueryWrapper<BizContractTemplate> w = new LambdaQueryWrapper<>();
        if (contractType != null) {
            w.eq(BizContractTemplate::getContractType, contractType);
        }
        if (onlyEnabled != null && onlyEnabled == 1) {
            w.eq(BizContractTemplate::getStatus, 1);
        }
        w.orderByDesc(BizContractTemplate::getUpdatedAt).orderByDesc(BizContractTemplate::getId);
        return Result.success(templateService.list(w));
    }

    @PostMapping("/import-docx")
    @Operation(summary = "导入合同模板(doc/docx)")
    public Result<BizContractTemplate> importDocx(
            @RequestParam("file") MultipartFile file,
            @RequestParam("templateName") String templateName,
            @RequestParam("contractType") Integer contractType) {
        if (file == null || file.isEmpty()) {
            return Result.badRequest("模板文件不能为空");
        }
        if (templateName == null || templateName.isBlank()) {
            return Result.badRequest("模板名称不能为空");
        }
        if (contractType == null || (contractType != 1 && contractType != 2)) {
            return Result.badRequest("contractType 仅支持 1(收入) 或 2(支出)");
        }
        String original = file.getOriginalFilename() == null ? "" : file.getOriginalFilename();
        String lower = original.toLowerCase(Locale.ROOT);
        if (!(lower.endsWith(".docx") || lower.endsWith(".doc") || lower.endsWith(".txt"))) {
            return Result.badRequest("仅支持 doc/docx/txt 模板导入");
        }
        try {
            String content = extractText(file, lower);
            if (content == null || content.isBlank()) {
                return Result.badRequest("模板正文为空，请检查文件内容");
            }
            List<String> fields = extractFieldKeys(content);
            BizContractTemplate t = new BizContractTemplate();
            t.setTemplateName(templateName.trim());
            t.setContractType(contractType);
            t.setTemplateContent(content);
            t.setTemplateVersion(1);
            t.setContentHash(sha256(content));
            t.setFieldKeys(String.join(",", fields));
            t.setSourceFileName(original);
            t.setStatus(1);
            t.setRemark("导入模板，系统自动识别可编辑区域");
            templateService.save(t);
            return Result.success(t);
        } catch (Exception e) {
            return Result.error("模板解析失败: " + e.getMessage());
        }
    }

    @PutMapping
    @Operation(summary = "更新模板")
    public Result<Void> update(@RequestBody BizContractTemplate body) {
        if (body == null || body.getId() == null) {
            return Result.badRequest("模板ID不能为空");
        }
        if (body.getTemplateContent() != null) {
            List<String> fields = extractFieldKeys(body.getTemplateContent());
            body.setFieldKeys(String.join(",", fields));
            BizContractTemplate old = templateService.getById(body.getId());
            int nextVersion = old == null || old.getTemplateVersion() == null ? 1 : old.getTemplateVersion() + 1;
            body.setTemplateVersion(nextVersion);
            body.setContentHash(sha256(body.getTemplateContent()));
        }
        templateService.updateById(body);
        return Result.success(null);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除模板")
    public Result<Void> delete(@PathVariable Long id) {
        templateService.removeById(id);
        return Result.success(null);
    }

    @GetMapping("/draft/{templateId}")
    @Operation(summary = "获取智能签订草稿")
    public Result<Map<String, Object>> getDraft(
            @PathVariable Long templateId,
            @RequestParam(required = false) Long projectId,
            @RequestParam(required = false) Long supplierId) {
        BizContractTemplate t = templateService.getById(templateId);
        if (t == null || t.getStatus() == null || t.getStatus() != 1) {
            return Result.notFound("模板不存在或已停用");
        }
        Map<String, String> defaults = buildDefaults(projectId, supplierId);
        Map<String, Object> data = new HashMap<>();
        data.put("templateId", t.getId());
        data.put("templateName", t.getTemplateName());
        data.put("contractType", t.getContractType());
        data.put("templateVersion", t.getTemplateVersion());
        data.put("templateContentHash", t.getContentHash());
        data.put("templateContent", t.getTemplateContent());
        data.put("fieldKeys", extractFieldKeys(t.getTemplateContent()));
        data.put("defaults", defaults);
        return Result.success(data);
    }

    private String extractText(MultipartFile file, String lowerName) throws Exception {
        if (lowerName.endsWith(".txt")) {
            return new String(file.getBytes(), StandardCharsets.UTF_8);
        }
        if (lowerName.endsWith(".docx")) {
            try (ZipInputStream zis = new ZipInputStream(file.getInputStream(), StandardCharsets.UTF_8)) {
                ZipEntry entry;
                while ((entry = zis.getNextEntry()) != null) {
                    if (!"word/document.xml".equals(entry.getName())) {
                        continue;
                    }
                    String xml = new String(zis.readAllBytes(), StandardCharsets.UTF_8);
                    String text = xml
                            .replaceAll("</w:p>", "\n")
                            .replaceAll("<[^>]+>", "")
                            .replaceAll("&amp;", "&")
                            .replaceAll("&lt;", "<")
                            .replaceAll("&gt;", ">")
                            .replaceAll("&quot;", "\"")
                            .replaceAll("&#39;", "'");
                    return text;
                }
            }
            return "";
        }
        // .doc 暂按文本兜底，建议使用 docx 获取更好解析结果
        return new String(file.getBytes(), StandardCharsets.UTF_8);
    }

    private List<String> extractFieldKeys(String content) {
        LinkedHashSet<String> fields = new LinkedHashSet<>();
        Matcher m = PLACEHOLDER_PATTERN.matcher(content);
        while (m.find()) {
            String key = m.group(1) == null ? "" : m.group(1).trim();
            if (!key.isEmpty()) {
                fields.add(key);
            }
        }
        if (!fields.isEmpty()) {
            return new ArrayList<>(fields);
        }
        Matcher underline = UNDERLINE_PATTERN.matcher(content);
        int i = 1;
        while (underline.find()) {
            fields.add("FIELD_" + i++);
        }
        return new ArrayList<>(fields);
    }

    private Map<String, String> buildDefaults(Long projectId, Long supplierId) {
        Map<String, String> defaults = new HashMap<>();
        defaults.put("签订日期", LocalDate.now().toString());
        defaults.put("合同签订日期", LocalDate.now().toString());
        if (projectId != null) {
            BizProject p = projectService.getById(projectId);
            if (p != null) {
                defaults.put("项目名称", nvl(p.getProjectName()));
                defaults.put("项目编号", nvl(p.getProjectNo()));
                defaults.put("甲方", nvl(p.getOwnerName()));
            }
        }
        if (supplierId != null) {
            BizSupplier s = supplierService.getById(supplierId);
            if (s != null) {
                defaults.put("乙方", nvl(s.getSupplierName()));
                defaults.put("供应商", nvl(s.getSupplierName()));
                defaults.put("品牌", nvl(s.getSupplierName()));
                defaults.put("品牌名称", nvl(s.getSupplierName()));
                defaults.put("联系人", nvl(s.getContactName()));
                defaults.put("联系电话", nvl(s.getContactPhone()));
            }
        }
        return defaults;
    }

    private String nvl(String v) {
        return v == null ? "" : v;
    }

    private String sha256(String text) {
        if (text == null) return "";
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] bytes = digest.digest(text.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(bytes.length * 2);
            for (byte b : bytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            return "";
        }
    }
}
