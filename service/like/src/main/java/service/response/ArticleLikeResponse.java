package service.response;

import entity.ArticleLike;
import lombok.Getter;
import lombok.ToString;

import java.time.LocalDateTime;

@Getter
@ToString
public class ArticleLikeResponse {
    private Long articleLikeId;
    private Long articleId;
    private Long userId;
    private LocalDateTime createdAt;

    public static ArticleLikeResponse from(ArticleLike articleLike) {
        ArticleLikeResponse response = new ArticleLikeResponse();

        response.articleId = articleLike.getArticleId();
        response.articleLikeId = articleLike.getId();
        response.createdAt = articleLike.getCreatedAt();
        response.userId = articleLike.getUserId();

        return response;
    }
}
