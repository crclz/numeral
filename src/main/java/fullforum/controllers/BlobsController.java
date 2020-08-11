package fullforum.controllers;

import fullforum.data.models.Blob2;
import fullforum.data.repos.BlobRepository;
import fullforum.errhand.BadRequestException;
import fullforum.errhand.ErrorCode;
import fullforum.services.IAuth;
import fullforum.services.Snowflake;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.transaction.Transactional;
import java.io.IOException;

@Transactional
@RestController
@RequestMapping("/api/blobs")
@Validated// PathVariable and params auto validation
public class BlobsController {

    @Autowired
    Snowflake snowflake;

    @Autowired
    IAuth auth;

    @Autowired
    BlobRepository blobRepository;

    // Note: spring默认限制1MB，所以如果要上传超过1mb，还要查阅文档去设置
    @PostMapping
    public String uploadFile(@RequestParam MultipartFile file) throws IOException {
        if (file.getSize() > 1024 * 1024 * 10) {
            // Bigger than 10MB
            throw new BadRequestException(ErrorCode.FileTooLarge, "文件超过10MB了");
        }

        var blob = new Blob2(snowflake.nextId(), file.getBytes());
        blobRepository.save(blob);

        // 返回文件url：/api/blobs/123
        return "/api/blobs/" + blob.getId().toString();
    }

    @GetMapping("{id}")
    public ResponseEntity<byte[]> getFile(@PathVariable long id) {
        var blob = blobRepository.findById(id).orElse(null);
        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_JPEG)
                .body(blob.getData());
    }

}
