package com.mochu.oa.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mochu.oa.common.ImageWatermarkUtil;
import com.mochu.oa.common.Result;
import com.mochu.oa.common.WatermarkOcrResult;
import com.mochu.oa.entity.BizApprovalTodo;
import com.mochu.oa.entity.BizGantt;
import com.mochu.oa.entity.BizProject;
import com.mochu.oa.entity.SysRole;
import com.mochu.oa.entity.SysUser;
import com.mochu.oa.entity.SysUserRole;
import com.mochu.oa.mapper.BizApprovalTodoMapper;
import com.mochu.oa.service.BizProjectService;
import com.mochu.oa.service.BizGanttService;
import com.mochu.oa.service.SysRoleService;
import com.mochu.oa.service.SysUserRoleService;
import com.mochu.oa.service.SysUserService;
import com.mochu.oa.service.WatermarkOcrService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/gantt")
@RequiredArgsConstructor
@Tag(name = "甘特图管理")
public class BizGanttController {

    private static final int WATERMARK_PROGRESS_THRESHOLD = 60;
    private static final int MIN_VALID_WATERMARK_PHOTO_COUNT = 5;

    private final BizGanttService bizGanttService;
    private final BizProjectService bizProjectService;
    private final SysRoleService sysRoleService;
    private final SysUserService sysUserService;
    private final SysUserRoleService sysUserRoleService;
    private final BizApprovalTodoMapper bizApprovalTodoMapper;
    private final WatermarkOcrService watermarkOcrService;
    
    @GetMapping("/list")
    @Operation(summary = "获取甘特图列表")
    public Result<List<BizGantt>> list() {
        List<BizGantt> list = bizGanttService.list();
        return Result.success(list);
    }
    
    @GetMapping("/page")
    @Operation(summary = "分页查询甘特图")
    public Result<Page<BizGantt>> page(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer pageNum,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") Integer pageSize,
            @Parameter(description = "项目ID") @RequestParam(required = false) Long projectId,
            @Parameter(description = "状态") @RequestParam(required = false) Integer status) {
        Page<BizGantt> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<BizGantt> wrapper = new LambdaQueryWrapper<>();
        if (projectId != null) {
            wrapper.eq(BizGantt::getProjectId, projectId);
        }
        if (status != null) {
            wrapper.eq(BizGantt::getStatus, status);
        }
        Page<BizGantt> result = bizGanttService.page(page, wrapper);
        return Result.success(result);
    }
    
    @GetMapping("/{id}")
    @Operation(summary = "获取甘特图详情")
    public Result<BizGantt> getById(@Parameter(description = "甘特图ID") @PathVariable Long id) {
        BizGantt gantt = bizGanttService.getById(id);
        return Result.success(gantt);
    }
    
    @PostMapping
    @Operation(summary = "创建甘特图")
    public Result<Void> create(@RequestBody BizGantt gantt) {
        bizGanttService.save(gantt);
        return Result.success(null);
    }
    
    @PutMapping
    @Operation(summary = "更新甘特图")
    public Result<Void> update(@RequestBody BizGantt gantt) {
        bizGanttService.updateById(gantt);
        return Result.success(null);
    }
    
    @DeleteMapping("/{id}")
    @Operation(summary = "删除甘特图")
    public Result<Void> delete(@Parameter(description = "甘特图ID") @PathVariable Long id) {
        bizGanttService.removeById(id);
        return Result.success(null);
    }
    
    @PostMapping("/{id}/submit")
    @Operation(summary = "提交甘特图")
    public Result<Void> submit(@Parameter(description = "甘特图ID") @PathVariable Long id) {
        BizGantt gantt = new BizGantt();
        gantt.setId(id);
        gantt.setStatus(1);
        bizGanttService.updateById(gantt);
        return Result.success(null);
    }
    
    @GetMapping("/{id}/progress")
    @Operation(summary = "获取甘特图进度")
    public Result<Map<String, Object>> getProgress(@Parameter(description = "甘特图ID") @PathVariable Long id) {
        Map<String, Object> progress = new HashMap<>();
        progress.put("overallProgress", 65);
        progress.put("totalTasks", 10);
        progress.put("completedTasks", 6);
        progress.put("inProgressTasks", 3);
        progress.put("pendingTasks", 1);
        return Result.success(progress);
    }
    
    @PutMapping("/{id}/progress")
    @Operation(summary = "更新甘特图进度")
    public Result<Void> updateProgress(
            @Parameter(description = "甘特图ID") @PathVariable Long id,
            @RequestBody Map<String, Object> params) {
        return Result.success(null);
    }

    @PostMapping("/task-photo-validate")
    @Operation(summary = "任务节点照片水印校验")
    public Result<Map<String, Object>> validateTaskPhotos(@RequestBody Map<String, Object> params) {
        Long ganttId = parseLong(params.get("ganttId"));
        Long taskId = parseLong(params.get("taskId"));
        Long handlerId = parseLong(params.get("handlerId"));
        String taskName = params.get("taskName") == null ? "" : String.valueOf(params.get("taskName"));
        int progress = 0;
        if (params.get("progress") != null) {
            try {
                progress = Integer.parseInt(String.valueOf(params.get("progress")));
            } catch (NumberFormatException ignored) {
            }
        }
        List<String> photos = List.of();
        Object photosObj = params.get("photos");
        if (photosObj instanceof List<?> rawList) {
            photos = rawList.stream().map(String::valueOf).toList();
        }

        int validCount = 0;
        int ocrSuccessCount = 0;
        for (String photo : photos) {
            WatermarkOcrResult ocrResult = watermarkOcrService.parsePhoto(photo, taskName);
            boolean valid = ocrResult.isSuccess() ? ocrResult.isValidWatermark()
                    : ImageWatermarkUtil.hasValidWatermarkHint(photo, taskName);
            if (ocrResult.isSuccess()) {
                ocrSuccessCount++;
            }
            if (valid) {
                validCount++;
            }
        }

        boolean shouldValidate = progress >= WATERMARK_PROGRESS_THRESHOLD;
        int missingCount = Math.max(0, MIN_VALID_WATERMARK_PHOTO_COUNT - validCount);

        Map<String, Object> result = new HashMap<>();
        result.put("shouldValidate", shouldValidate);
        result.put("progressThreshold", WATERMARK_PROGRESS_THRESHOLD);
        result.put("requiredCount", MIN_VALID_WATERMARK_PHOTO_COUNT);
        result.put("validCount", validCount);
        result.put("ocrSuccessCount", ocrSuccessCount);
        result.put("missingCount", missingCount);
        boolean pass = !shouldValidate || validCount >= MIN_VALID_WATERMARK_PHOTO_COUNT;
        result.put("pass", pass);
        result.put("message", shouldValidate && !pass
                ? String.format("当前任务进度已达%s%%，需至少上传%s张有效水印现场照片，当前有效%s张，还差%s张。",
                WATERMARK_PROGRESS_THRESHOLD, MIN_VALID_WATERMARK_PHOTO_COUNT, validCount, missingCount)
                : "校验通过");
        result.put("todoCreated", false);
        if (shouldValidate && !pass && ganttId != null && taskId != null && handlerId != null) {
            boolean created = createOrUpdateWatermarkTodo(ganttId, taskId, taskName, handlerId, progress, validCount, missingCount);
            result.put("todoCreated", created);
        }
        return Result.success(result);
    }

    private Long parseLong(Object value) {
        if (value == null) return null;
        try {
            return Long.parseLong(String.valueOf(value));
        } catch (NumberFormatException ignored) {
            return null;
        }
    }

    private boolean createOrUpdateWatermarkTodo(Long ganttId, Long taskId, String taskName, Long handlerId,
                                                int progress, int validCount, int missingCount) {
        String todoTitle = String.format("甘特节点现场照片缺失：%s", taskName == null || taskName.isBlank() ? ("任务#" + taskId) : taskName);
        String todoDetail = String.format("任务进度%d%%，有效水印照片%d张，还差%d张，请尽快补充上传。", progress, validCount, missingCount);
        LambdaQueryWrapper<BizApprovalTodo> query = new LambdaQueryWrapper<BizApprovalTodo>()
                .eq(BizApprovalTodo::getInstanceId, ganttId)
                .eq(BizApprovalTodo::getNodeOrder, taskId.intValue())
                .eq(BizApprovalTodo::getHandlerId, handlerId)
                .eq(BizApprovalTodo::getCategory, "TODO")
                .eq(BizApprovalTodo::getStatus, 1);
        BizApprovalTodo existing = bizApprovalTodoMapper.selectOne(query);
        if (existing != null) {
            BizApprovalTodo update = new BizApprovalTodo();
            update.setId(existing.getId());
            update.setNodeName(todoTitle + "｜" + todoDetail);
            update.setPriority(1);
            bizApprovalTodoMapper.updateById(update);
            return false;
        }
        BizApprovalTodo todo = new BizApprovalTodo();
        todo.setInstanceId(ganttId);
        todo.setNodeOrder(taskId.intValue());
        todo.setNodeName(todoTitle + "｜" + todoDetail);
        todo.setHandlerId(handlerId);
        todo.setHandlerName("项目经理");
        todo.setCategory("TODO");
        todo.setPriority(1);
        todo.setStatus(1);
        bizApprovalTodoMapper.insert(todo);
        return true;
    }
    
    @GetMapping("/income-split/{contractId}")
    @Operation(summary = "获取收入拆分数据")
    public Result<List<Map<String, Object>>> getIncomeSplit(@Parameter(description = "合同ID") @PathVariable Long contractId) {
        return Result.success(List.of());
    }

    @GetMapping("/ocr-metrics")
    @Operation(summary = "获取OCR识别指标")
    public Result<Map<String, Object>> getOcrMetrics() {
        return Result.success(watermarkOcrService.metricsSnapshot());
    }

    @PostMapping("/hidden-work/photo-uploaded")
    @Operation(summary = "隐蔽工程照片上传后生成资料员待办")
    public Result<Map<String, Object>> onHiddenWorkPhotoUploaded(@RequestBody Map<String, Object> params) {
        Long ganttId = parseLong(params.get("ganttId"));
        Long projectId = parseLong(params.get("projectId"));
        Long taskId = parseLong(params.get("taskId"));
        Long fallbackHandlerId = parseLong(params.get("fallbackHandlerId"));
        String taskName = params.get("taskName") == null ? "" : String.valueOf(params.get("taskName"));
        String photoUrl = params.get("photoUrl") == null ? "" : String.valueOf(params.get("photoUrl"));
        if (ganttId == null || projectId == null || taskId == null || photoUrl.isBlank()) {
            return Result.error("参数不完整，无法生成资料员待办");
        }

        Long clerkId = resolveProjectDocumentClerk(projectId, fallbackHandlerId);
        if (clerkId == null) {
            return Result.error("未找到本项目资料员，且无可用回退处理人");
        }
        SysUser clerk = sysUserService.getById(clerkId);
        String clerkName = clerk != null ? (clerk.getRealName() != null ? clerk.getRealName() : clerk.getUsername()) : "资料员";
        String todoTitle = String.format("隐蔽工程资料归档：%s", taskName.isBlank() ? ("任务#" + taskId) : taskName);
        String todoDetail = "隐蔽工程现场照片已上传，请及时整理并归档相关资料。";
        String composedName = todoTitle + "｜" + todoDetail;

        LambdaQueryWrapper<BizApprovalTodo> query = new LambdaQueryWrapper<BizApprovalTodo>()
                .eq(BizApprovalTodo::getInstanceId, ganttId)
                .eq(BizApprovalTodo::getNodeOrder, taskId.intValue())
                .eq(BizApprovalTodo::getHandlerId, clerkId)
                .like(BizApprovalTodo::getNodeName, "隐蔽工程资料归档")
                .eq(BizApprovalTodo::getCategory, "TODO")
                .eq(BizApprovalTodo::getStatus, 1);
        BizApprovalTodo existing = bizApprovalTodoMapper.selectOne(query);
        boolean created = false;
        if (existing == null) {
            BizApprovalTodo todo = new BizApprovalTodo();
            todo.setInstanceId(ganttId);
            todo.setNodeOrder(taskId.intValue());
            todo.setNodeName(composedName);
            todo.setHandlerId(clerkId);
            todo.setHandlerName(clerkName);
            todo.setCategory("TODO");
            todo.setPriority(1);
            todo.setStatus(1);
            bizApprovalTodoMapper.insert(todo);
            created = true;
        }

        Map<String, Object> result = new HashMap<>();
        result.put("created", created);
        result.put("handlerId", clerkId);
        result.put("handlerName", clerkName);
        result.put("message", created ? "已生成资料员待办" : "资料员待办已存在，无需重复生成");
        return Result.success(result);
    }

    private Long resolveProjectDocumentClerk(Long projectId, Long fallbackHandlerId) {
        BizProject project = bizProjectService.getById(projectId);
        Long departmentId = project != null ? project.getDepartmentId() : null;

        List<SysRole> roles = sysRoleService.list(new LambdaQueryWrapper<SysRole>()
                .eq(SysRole::getStatus, 1));
        Set<Long> clerkRoleIds = roles.stream()
                .filter(r -> {
                    String roleName = r.getRoleName() == null ? "" : r.getRoleName();
                    String roleCode = r.getRoleCode() == null ? "" : r.getRoleCode().toLowerCase();
                    return roleName.contains("资料员")
                            || roleCode.contains("document")
                            || roleCode.contains("archive")
                            || roleCode.contains("data_clerk")
                            || roleCode.contains("ziliao");
                })
                .map(SysRole::getId)
                .collect(Collectors.toSet());
        if (!clerkRoleIds.isEmpty()) {
            List<Long> userIds = sysUserRoleService.list(new LambdaQueryWrapper<SysUserRole>()
                            .in(SysUserRole::getRoleId, clerkRoleIds))
                    .stream()
                    .map(SysUserRole::getUserId)
                    .distinct()
                    .collect(Collectors.toList());
            if (!userIds.isEmpty()) {
                LambdaQueryWrapper<SysUser> userQuery = new LambdaQueryWrapper<SysUser>()
                        .in(SysUser::getId, userIds)
                        .eq(SysUser::getStatus, 1)
                        .orderByAsc(SysUser::getId);
                if (departmentId != null) {
                    userQuery.eq(SysUser::getDepartmentId, departmentId);
                }
                List<SysUser> users = sysUserService.list(userQuery);
                if (!users.isEmpty()) {
                    return users.get(0).getId();
                }
            }
        }
        if (fallbackHandlerId != null) {
            return fallbackHandlerId;
        }
        return project != null ? project.getProjectManagerId() : null;
    }
}