package kuke.board.comment.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ArticleCommentCount {
    @Id
    private Long articleId;
    private Long commentCount;

    public static ArticleCommentCount init(Long articleId, Long commentCount) {
        ArticleCommentCount articleCommentCount = new ArticleCommentCount();

        articleCommentCount.articleId = articleId;
        articleCommentCount.commentCount = commentCount;

        return articleCommentCount;
    }
}
