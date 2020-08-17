package fullforum.controllers;

import fullforum.data.models.ELock;
import fullforum.data.repos.DocumentRepository;
import fullforum.data.repos.ELockRepository;
import fullforum.data.repos.UserRepository;
import fullforum.dto.out.AcquireLockResult;
import fullforum.dto.out.Quser;
import fullforum.errhand.BadRequestException;
import fullforum.errhand.ErrorCode;
import fullforum.errhand.UnauthorizedException;
import fullforum.services.IAuth;
import fullforum.services.Snowflake;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.transaction.Transactional;

@Transactional
@RestController
@RequestMapping("/api/e-lock")
@Validated
public class ELockController {
    @Autowired
    Snowflake snowflake;

    @Autowired
    IAuth auth;

    @Autowired
    ELockRepository elockRepository;

    @Autowired
    DocumentRepository documentRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    ModelMapper modelMapper;

    // TODO: 写测试
    @PostMapping("acquire")
    public AcquireLockResult acquireLock(@RequestParam Long documentId) {
        if (!auth.isLoggedIn()) {
            throw new UnauthorizedException();
        }

        var document = documentRepository.findById(documentId).orElse(null);

        if (document == null) {
            throw new BadRequestException(ErrorCode.EntityNotExist, "文档不存在");
        }

        // 为了省事，不确认用户是否有权限修改这个文档

        var lock = elockRepository.findELockByDocumentId(documentId);

        if (lock == null) {
            lock = new ELock(snowflake.nextId(), documentId);
        }

        if (lock.tryAcquire(auth.userId())) {
            // success
            elockRepository.save(lock);
            var result = new AcquireLockResult(true, null);
            return result;
        } else {
            // failure. return the owner info
            var owner = userRepository.findById(lock.getLastOwnerId()).orElseThrow();
            var result = new AcquireLockResult(false, Quser.convert(owner, modelMapper));
            return result;
        }
    }

    @GetMapping("get-owner")
    public Quser getOwner(@RequestParam Long documentId) {
        if (!auth.isLoggedIn()) {
            throw new UnauthorizedException();
        }

        var document = documentRepository.findById(documentId).orElse(null);

        if (document == null) {
            throw new BadRequestException(ErrorCode.EntityNotExist, "文档不存在");
        }

        var lock = elockRepository.findELockByDocumentId(documentId);
        if (lock == null) {
            return null;
        }

        var ownerId = lock.getRealOwnerId();

        if (ownerId == null) {
            return null;
        }

        var owner = userRepository.findById(ownerId).orElseThrow();

        return Quser.convert(owner, modelMapper);
    }

    // TODO: 写测试
    @PostMapping("release")
    public void releaseLock(@RequestParam Long documentId) {
        if (!auth.isLoggedIn()) {
            throw new UnauthorizedException();
        }

        var lock = elockRepository.findELockByDocumentId(documentId);

        if (lock == null) {
            return;
        }

        lock.tryRelease(auth.userId());
        elockRepository.save(lock);
    }

}
