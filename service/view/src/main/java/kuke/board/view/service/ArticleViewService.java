package kuke.board.view.service;

import kuke.board.view.repository.ArticleViewCountRepository;
import kuke.board.view.repository.ArticleViewDistributedLockRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class ArticleViewService {
    private final ArticleViewCountRepository repository;
    private final ArticleViewDistributedLockRepository lockRepository;
    private final ArticleViewCountBackUpProcessor processor;

    private static final int BACK_UP_BATCH_SIZE = 100;
    private static final Duration TTL = Duration.ofMinutes(10);

    public Long increase(Long articleId, Long userId) {
        if (!lockRepository.lock(articleId, userId, TTL)) {
            return repository.read(articleId);
        }

        Long count = repository.increase(articleId);

        if (count % BACK_UP_BATCH_SIZE == 0)
            processor.backUp(articleId, userId);

        return count;
    }

    public Long count(Long articleId) {
        return repository.read(articleId);
    }
}
