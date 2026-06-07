package com.cms.controller;

import cn.hutool.core.util.IdUtil;
import com.cms.common.result.Result;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;

@RestController
@RequestMapping("/api/common")
public class CommonController {

    @Value("${cms.upload.path}")
    private String uploadPath;

    @Value("${cms.upload.domain}")
    private String uploadDomain;

    @PostMapping("/upload")
    public Result<String> upload(@RequestParam("file") MultipartFile file,
                                  @RequestParam(defaultValue = "common") String biz) throws IOException {
        if (file.isEmpty()) {
            return Result.error("文件不能为空");
        }
        // 路径：/yyyy-MM-dd/biz/uuid.ext
        String dateDir = new java.text.SimpleDateFormat("yyyy-MM-dd").format(new java.util.Date());
        String dir = uploadPath + biz + "/" + dateDir + "/";
        new File(dir).mkdirs();
        String ext = file.getOriginalFilename().substring(file.getOriginalFilename().lastIndexOf("."));
        String filename = IdUtil.fastSimpleUUID() + ext;
        File dest = new File(dir + filename);
        file.transferTo(dest);
        String url = uploadDomain + biz + "/" + dateDir + "/" + filename;
        return Result.success(url);
    }
}
