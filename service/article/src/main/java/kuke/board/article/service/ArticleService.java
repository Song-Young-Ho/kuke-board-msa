package kuke.board.article.service;

import kuke.board.article.entity.Article;
import kuke.board.article.entity.BoardArticleCount;
import kuke.board.article.repository.ArticleRepository;
import kuke.board.article.repository.BoardArticleCountRepository;
import kuke.board.article.service.request.ArticleCreateRequest;
import kuke.board.article.service.response.ArticlePageResponse;
import kuke.board.article.service.response.ArticleResponse;
import kuke.board.common.snowflake.Snowflake;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ArticleService {
    private final Snowflake snowflake = new Snowflake();
    private final ArticleRepository repository;
    private final BoardArticleCountRepository countRepository;

    @Transactional
    public ArticleResponse create(ArticleCreateRequest request) {
        Long id = snowflake.nextId();

        Article article = repository.save(
                Article.create(id, request.getTitle(), request.getContent(), request.getBoardId(), request.getWriterId())
        );

        int result = countRepository.increase(article.getArticleId());

        if (result == 0)
            countRepository.save(
                    BoardArticleCount.init(request.getBoardId(), 1L)
            );

        return ArticleResponse.from(article);
    }

    @Transactional
    public ArticleResponse update(Long articleId, ArticleCreateRequest request) {
        Article article = repository.findById(articleId).orElseThrow();
        article.update(request.getTitle(), request.getContent());

        return ArticleResponse.from(article);
    }

    public ArticleResponse read(Long articleId) {
        Article article = repository.findById(articleId).orElseThrow();
        return ArticleResponse.from(article);
    }

    @Transactional
    public void delete(Long articleId) {
        Article article = repository.findById(articleId).orElseThrow();
        repository.delete(article);

        countRepository.decrease(article.getBoardId());
    }

    public ArticlePageResponse readAll(Long boardId, Long page, Long pageSize) {
        return ArticlePageResponse.of(
                repository.findAll(boardId, (page - 1) * pageSize, pageSize)
                        .stream()
                        .map(ArticleResponse::from)
                        .toList(),
                repository.count(
                        boardId,
                        PageLimitCalculator.calculatePageLimit(page, pageSize, 10L)
                )
        );
    }

    public List<ArticleResponse> readAllInfiniteScroll(Long boardId, Long pageSize, Long lastArticleId) {
        List<Article> articles = lastArticleId == null ?
                repository.findAllInfiniteScroll(boardId, pageSize) :
                repository.findAllInfiniteScroll(boardId, pageSize, lastArticleId);

        return articles.stream().map(ArticleResponse::from).toList();
    }

    public Long count(Long boardId) {
        return countRepository.findById(boardId)
                .map(BoardArticleCount::getArticleCount)
                .orElse(0L);
    }
}
