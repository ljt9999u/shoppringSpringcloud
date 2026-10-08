package org.example.Controller;

import org.example.Product.ProductImage;
import org.example.Service.ProductImageService;
import org.example.common.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * 商品图片控制器（商品详情轮播多图）
 * 挂在 /api/product/image 下，走网关 /api/product/** 路由
 */
@RestController
@RequestMapping("/api/product/image")
@CrossOrigin
public class ProductImageController {

    @Autowired
    private ProductImageService productImageService;

    /**
     * 查询某商品的全部图片
     * GET /api/product/image/list/{productId}
     */
    @GetMapping("/list/{productId}")
    public Result<List<ProductImage>> list(@PathVariable Long productId) {
        return Result.success(productImageService.listByProductId(productId));
    }

    /**
     * 新增单张图片
     * POST /api/product/image/add
     */
    @PostMapping("/add")
    public Result<ProductImage> add(@RequestBody ProductImage image) {
        int rows = productImageService.add(image);
        if (rows <= 0) {
            return Result.fail("新增图片失败");
        }
        return Result.success(image);
    }

    /**
     * 批量保存商品相册
     * POST /api/product/image/batch  body: [ {productId,imageUrl,sort}, ... ]
     */
    @PostMapping("/batch")
    public Result<Boolean> addBatch(@RequestBody List<ProductImage> list) {
        int rows = productImageService.addBatch(list);
        if (rows <= 0) {
            return Result.fail("批量保存图片失败");
        }
        return Result.success(true);
    }

    /**
     * 更新图片
     * PUT /api/product/image/update
     */
    @PutMapping("/update")
    public Result<Boolean> update(@RequestBody ProductImage image) {
        int rows = productImageService.update(image);
        if (rows <= 0) {
            return Result.fail("更新图片失败");
        }
        return Result.success(true);
    }

    /**
     * 删除单张图片
     * DELETE /api/product/image/{id}
     */
    @DeleteMapping("/{id}")
    public Result<Boolean> delete(@PathVariable Long id) {
        boolean ok = productImageService.delete(id);
        if (!ok) {
            return Result.fail("删除图片失败");
        }
        return Result.success(true);
    }

    /**
     * 删除某商品全部图片
     * DELETE /api/product/image/product/{productId}
     */
    @DeleteMapping("/product/{productId}")
    public Result<Boolean> deleteByProduct(@PathVariable Long productId) {
        productImageService.deleteByProductId(productId);
        return Result.success(true);
    }

    /* ==================== 图片上传（后端中转，存本地磁盘） ==================== */

    @Value("${upload.dir:D:/weifuwu/demo2/uploads}")
    private String uploadDir;

    private static final List<String> ALLOWED_DIRS = Arrays.asList("product", "avatar");
    private static final List<String> ALLOWED_EXTS = Arrays.asList(".jpg", ".jpeg", ".png", ".webp", ".gif");
    private static final long MAX_SIZE = 5 * 1024 * 1024;

    /**
     * 图片上传：前端 FormData 中转，服务器保存到本地磁盘，返回可访问 URL
     * POST /api/product/image/upload  form-data: file, dir=product|avatar
     */
    @PostMapping("/upload")
    public Result<String> upload(@RequestParam("file") MultipartFile file,
                                 @RequestParam(value = "dir", defaultValue = "product") String dir) {
        if (file == null || file.isEmpty()) {
            return Result.fail("请选择要上传的图片");
        }
        final String safeDir = ALLOWED_DIRS.contains(dir) ? dir : "product";
        if (file.getSize() > MAX_SIZE) {
            return Result.fail("图片大小不能超过 5MB");
        }
        String ext = "";
        String original = file.getOriginalFilename();
        if (original != null) {
            int dot = original.lastIndexOf('.');
            if (dot >= 0) {
                ext = original.substring(dot).toLowerCase();
            }
        }
        if (!ALLOWED_EXTS.contains(ext)) {
            return Result.fail("仅支持 JPG / PNG / WEBP / GIF 格式图片");
        }
        try {
            Path dirPath = Paths.get(uploadDir, safeDir);
            Files.createDirectories(dirPath);
            String filename = UUID.randomUUID().toString().replace("-", "") + ext;
            file.transferTo(dirPath.resolve(filename).toFile());
            return Result.success("/api/product/image/file/" + safeDir + "/" + filename);
        } catch (IOException e) {
            return Result.fail("保存图片失败：" + e.getMessage());
        }
    }

    /**
     * 图片访问：网关 GET 白名单内，未登录可直接展示
     * GET /api/product/image/file/{dir}/{filename}
     */
    @GetMapping("/file/{dir}/{filename:.+}")
    public ResponseEntity<Resource> file(@PathVariable String dir, @PathVariable String filename) {
        // 文件名白名单校验，防目录穿越
        if (!ALLOWED_DIRS.contains(dir)
                || !filename.matches("[a-f0-9]{32}\\.(jpg|jpeg|png|webp|gif)")) {
            return ResponseEntity.badRequest().build();
        }
        Path path = Paths.get(uploadDir, dir, filename).normalize();
        if (!path.startsWith(Paths.get(uploadDir).normalize()) || !Files.exists(path)) {
            return ResponseEntity.notFound().build();
        }
        String ext = filename.substring(filename.lastIndexOf('.'));
        MediaType mediaType;
        switch (ext) {
            case ".png":
                mediaType = MediaType.IMAGE_PNG;
                break;
            case ".webp":
                mediaType = MediaType.parseMediaType("image/webp");
                break;
            case ".gif":
                mediaType = MediaType.IMAGE_GIF;
                break;
            default:
                mediaType = MediaType.IMAGE_JPEG;
        }
        return ResponseEntity.ok()
                .contentType(mediaType)
                .cacheControl(CacheControl.maxAge(30, TimeUnit.DAYS))
                .body(new FileSystemResource(path));
    }
}
