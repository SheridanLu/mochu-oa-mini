package com.mochu.oa.controller;

import com.mochu.oa.common.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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

    private static final Path UPLOAD_ROOT = Paths.get("/tmp/mochu-uploads").toAbsolutePath().normalize();

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
    @Operation(summary = "读取已上传文件（图片/附件预览）")
    public ResponseEntity<Resource> getFile(@RequestParam("path") String path) throws IOException {
        if (path == null || path.isBlank()) {
            return ResponseEntity.badRequest().build();
        }
        if (path.contains("..") || path.startsWith("/") || path.startsWith("\\")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        Path resolved = UPLOAD_ROOT.resolve(path).normalize();
        if (!resolved.startsWith(UPLOAD_ROOT)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        Resource resource = new FileSystemResource(resolved.toFile());
        if (!resource.exists() || !resource.isReadable()) {
            return ResponseEntity.notFound().build();
        }
        String ct = Files.probeContentType(resolved);
        MediaType mediaType = MediaType.APPLICATION_OCTET_STREAM;
        if (ct != null && !ct.isBlank()) {
            try {
                mediaType = MediaType.parseMediaType(ct);
            } catch (Exception ignored) {
                // keep OCTET_STREAM
            }
        }
        String filename = resolved.getFileName().toString();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + filename + "\"")
                .contentType(mediaType)
                .body(resource);
    }
}