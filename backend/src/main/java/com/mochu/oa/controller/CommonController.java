package com.mochu.oa.controller;

import com.mochu.oa.common.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/common")
@RequiredArgsConstructor
@Tag(name = "通用接口")
public class CommonController {

    @PostMapping("/upload")
    @Operation(summary = "文件上传")
    public Result<Map<String, Object>> upload(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return Result.error("文件不能为空");
        }

        String originalFilename = file.getOriginalFilename();
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }

        String dateStr = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String saveDir = "/tmp/mochu-uploads/" + dateStr;
        File dir = new File(saveDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        String newFilename = UUID.randomUUID().toString().replace("-", "") + extension;
        String savePath = saveDir + "/" + newFilename;

        try {
            file.transferTo(new File(savePath));
        } catch (IOException e) {
            return Result.error("文件保存失败: " + e.getMessage());
        }

        Map<String, Object> result = new HashMap<>();
        result.put("id", System.currentTimeMillis());
        result.put("fileName", originalFilename);
        result.put("filePath", "/uploads/" + dateStr + "/" + newFilename);
        result.put("previewUrl", "/api/common/file?path=" + dateStr + "/" + newFilename);
        result.put("url", "/api/common/file?path=" + dateStr + "/" + newFilename);

        return Result.success(result);
    }

    @GetMapping("/file")
    @Operation(summary = "获取文件")
    public Result<Map<String, Object>> getFile(@RequestParam String path) {
        String fullPath = "/tmp/mochu-uploads/" + path;
        File file = new File(fullPath);
        if (!file.exists()) {
            return Result.error("文件不存在");
        }

        Map<String, Object> result = new HashMap<>();
        result.put("url", "/api/common/file?path=" + path);
        return Result.success(result);
    }
}