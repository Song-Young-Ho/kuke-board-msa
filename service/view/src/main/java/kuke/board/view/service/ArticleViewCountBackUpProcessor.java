package kuke.board.view.service;

import kuke.board.view.entity.ArticleViewCount;
import kuke.board.view.repository.ArticleViewCountBackUpRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class ArticleViewCountBackUpProcessor {
    private final ArticleViewCountBackUpRepository repository;
    @Transactional
    public void backUp(Long articleId, Long viewCount) {
        int result = repository.updateViewCount(articleId, viewCount);

        if (result == 0) {
            repository.findById(articleId)
                    .ifPresentOrElse(ignored -> {},
                            () -> repository.save(ArticleViewCount.init(articleId, viewCount)));
        }
    }
}
