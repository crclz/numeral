package fullforum.controllers;

import fullforum.data.models.Blob2;
import fullforum.data.repos.BlobRepository;
import fullforum.errhand.BadRequestException;
import fullforum.errhand.ErrorCode;
import fullforum.services.IAuth;
import fullforum.services.Snowflake;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

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

}
