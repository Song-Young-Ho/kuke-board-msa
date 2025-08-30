package kuke.board.like.service;

import kuke.board.like.entity.ArticleLike;
import kuke.board.common.snowflake.Snowflake;
import kuke.board.like.entity.ArticleLikeCount;
import kuke.board.like.repository.ArticleLikeCountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import kuke.board.like.repository.ArticleLikeRepository;
import kuke.board.like.service.response.ArticleLikeResponse;

@Service
@RequiredArgsConstructor
public class ArticleLikeService {
    private final Snowflake snowflake = new Snowflake();
    private final ArticleLikeRepository repository;
    private final ArticleLikeCountRepository countRepository;

    public ArticleLikeResponse read(Long articleId, Long userId) {
        return repository.findByArticleIdAndUserId(articleId, userId)
                .map(ArticleLikeResponse::from)
                .orElseThrow();
    }

    @Transactional
    public void likePessimisticLock1(Long articleId, Long userId) {
        repository.save(
                ArticleLike.create(
                        snowflake.nextId(),
                        articleId,
                        userId
                )
        );

        int result = countRepository.increase(articleId);

        if (result == 0) countRepository.save(ArticleLikeCount.init(articleId, userId));
    }

    @Transactional
    public void unlikePessimisticLock1(Long articleId, Long userId) {
        repository.findByArticleIdAndUserId(articleId, userId)
                .ifPresent(entity -> {
                    repository.delete(entity);
                    countRepository.decrease(articleId);
                });
    }

    @Transactional
    public void likePessimisticLock2(Long articleId, Long userId) {
        repository.save(
                ArticleLike.create(
                        snowflake.nextId(),
                        articleId,
                        userId
                )
        );

        ArticleLikeCount articleLikeCount = countRepository.findLockedByArticleId(articleId)
                .orElseGet(() -> ArticleLikeCount.init(articleId, 0L));

        articleLikeCount.increase();
        countRepository.save(articleLikeCount);
    }

    @Transactional
    public void unlikePessimisticLock2(Long articleId, Long userId) {
        repository.findByArticleIdAndUserId(articleId, userId)
                .ifPresent(entity -> {
                    repository.delete(entity);
                    ArticleLikeCount articleLikeCount = countRepository.findLockedByArticleId(articleId).orElseThrow();
                    articleLikeCount.decrease();
                });
    }

    @Transactional
    public void likeOptimisticLock(Long articleId, Long userId) {
        repository.save(
                ArticleLike.create(
                        snowflake.nextId(),
                        articleId,
                        userId
                )
        );

        ArticleLikeCount articleLikeCount
                = countRepository.findById(articleId).orElseGet(() -> ArticleLikeCount.init(articleId, 0L));

        articleLikeCount.increase();
        countRepository.save(articleLikeCount);
    }

    @Transactional
    public void unlikeOptimisticLock(Long articleId, Long userId) {
        repository.findByArticleIdAndUserId(articleId, userId)
                .ifPresent(entity -> {
                    repository.delete(entity);

                    ArticleLikeCount articleLikeCount = countRepository.findById(articleId).orElseThrow();
                    articleLikeCount.decrease();
                });
    }

    public Long count(Long articleId) {
        return countRepository.findById(articleId)
                .map(ArticleLikeCount::getLikeCount)
                .orElse(0L);
    }
}
