package kuke.board.like.repository;

import kuke.board.like.entity.ArticleLike;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ArticleLikeRepository extends JpaRepository<ArticleLike, Integer> {
    Optional<ArticleLike> findByArticleIdAndUserId(Long articleId, Long userId);
}
