package entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDateTime;

@Table(name = "article_like")
@Getter
@Entity
@ToString
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ArticleLike {
    @Id
    private Long id;
    private Long articleId;
    private Long userId;
    private LocalDateTime createdAt;

    public static ArticleLike create(Long id, Long articleId, Long userId) {
        ArticleLike articleLike = new ArticleLike();

        articleLike.id = id;
        articleLike.articleId = articleId;
        articleLike.userId = userId;
        articleLike.createdAt = LocalDateTime.now();

        return articleLike;
    }
}
