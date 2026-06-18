package com.cms.controller;

import cn.hutool.core.util.IdUtil;
import com.cms.common.result.Result;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/api/common")
public class CommonController {

    @Value("${cms.upload.path}")
    private String uploadPath;

    @Value("${cms.upload.domain}")
    private String uploadDomain;

    private static final List<String> ALLOWED_BIZ = Arrays.asList("avatar", "competition", "attachment", "common", "banner", "cover");

    @PostMapping("/upload")
    public Result<String> upload(@RequestParam("file") MultipartFile file,
                                  @RequestParam(defaultValue = "common") String biz) throws IOException {
        if (file.isEmpty()) {
            return Result.error("文件不能为空");
        }
        // 校验 biz 参数，防止路径穿越
        if (biz == null || !ALLOWED_BIZ.contains(biz)) {
            biz = "common";
        }
        // 获取并校验原始文件名
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.isEmpty()) {
            originalFilename = "unknown";
        }
        // 提取扩展名，处理无扩展名的情况
        String ext = "";
        int dotIndex = originalFilename.lastIndexOf(".");
        if (dotIndex >= 0) {
            ext = originalFilename.substring(dotIndex);
        }
        // 路径：/biz/yyyy-MM-dd/uuid.ext
        String dateDir = new java.text.SimpleDateFormat("yyyy-MM-dd").format(new java.util.Date());
        String dir = uploadPath + biz + "/" + dateDir + "/";
        new File(dir).mkdirs();
        String filename = IdUtil.fastSimpleUUID() + ext;
        File dest = new File(dir + filename);
        file.transferTo(dest);
        String url = uploadDomain + biz + "/" + dateDir + "/" + filename;
        return Result.success(url);
    }
}
