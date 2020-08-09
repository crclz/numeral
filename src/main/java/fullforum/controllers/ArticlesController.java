package fullforum.controllers;

import fullforum.data.models.Article;
import fullforum.data.models.User;
import fullforum.data.repos.ArticleRepository;
import fullforum.data.repos.UserRepository;
import fullforum.dto.in.CreateArticleModel;
import fullforum.dto.in.PatchArticleModel;
import fullforum.dto.out.IdDto;
import fullforum.dto.out.QArticle;
import fullforum.dto.out.Quser;
import fullforum.errhand.ForbidException;
import fullforum.errhand.NotFoundException;
import fullforum.errhand.UnauthorizedException;
import fullforum.services.IAuth;
import fullforum.services.Snowflake;
import org.hibernate.cfg.NotYetImplementedException;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.validation.Valid;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.websocket.server.PathParam;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/articles")
@Validated// PathVariable and params auto validation
public class ArticlesController {
    @Autowired
    Snowflake snowflake;

    @Autowired
    UserRepository userRepository;

    @Autowired
    ArticleRepository articleRepository;

    @Autowired
    IAuth auth;

    @PostMapping
    public IdDto createArticle(@RequestBody @Valid CreateArticleModel model) {
        if (!auth.isLoggedIn()) {
            throw new UnauthorizedException();
        }

        var article = new Article(snowflake.nextId(), model.title, model.text, auth.userId());
        articleRepository.save(article);

        return new IdDto(article.getId());
    }

    @DeleteMapping("{id}")
    public void removeArticle(@PathVariable long id) {
        if (!auth.isLoggedIn()) {
            throw new UnauthorizedException();
        }

        var article = articleRepository.findById(id).orElse(null);
        if (article == null) {
            throw new NotFoundException();
        }
        if (article.getUserId() != auth.userId()) {
            throw new ForbidException();
        }

        // ok
        articleRepository.delete(article);
    }

    @PatchMapping("{id}")
    public void patchArticle(@PathVariable long id, PatchArticleModel model) {
        if (!auth.isLoggedIn()) {
            throw new UnauthorizedException();
        }

        var article = articleRepository.findById(id).orElse(null);
        if (article == null) {
            throw new NotFoundException();
        }
        if (article.getUserId() != auth.userId()) {
            throw new ForbidException();
        }

        // ok
        if (model.title != null) {
            article.setTitle(model.title);
        }
        if (model.text != null) {
            article.setText(model.text);
        }

        articleRepository.save(article);
    }

    @GetMapping("{id}")
    public QArticle getArticleById(@PathVariable long id) {
        var article = articleRepository.findById(id).orElse(null);
        if (article == null) {
            return null;
        }

        return QArticle.convert(article, null, modelMapper);
    }


    @PersistenceContext
    EntityManager entityManager;

    @Autowired
    ModelMapper modelMapper;

    // TODO: @RequestParam默认required=true, 所以包含可选参数的地方必须要设置required=false
    @GetMapping
    public List<QArticle> getArticles(
            @RequestParam Long userId,
            @RequestParam String keyword,
            @RequestParam @Min(0) int pageNo,
            @RequestParam @Min(1) @Max(10) int pageSize
    ) {
        var query = entityManager.createQuery(
                "select a, u from Article a join User u" +
                        " on a.userId = u.id" +
                        " where :userId is null OR a.userId = :userId" +
                        " AND :keyword is null OR a.title like :expr")
                .setParameter("userId", userId)
                .setParameter("keyword", keyword)
                .setParameter("expr", "%" + keyword + "%")
                .setFirstResult(pageNo * pageSize)
                .setMaxResults(pageSize);

        var result = query.getResultList();
        var data = new ArrayList<QArticle>();

        for (var item : result) {
            var objs = (Object[]) item;
            var article = (Article) objs[0];
            var user = (User) objs[1];

            var qarticle = QArticle.convert(article, Quser.convert(user, modelMapper), modelMapper);
            data.add(qarticle);
        }

        return data;
    }
}
