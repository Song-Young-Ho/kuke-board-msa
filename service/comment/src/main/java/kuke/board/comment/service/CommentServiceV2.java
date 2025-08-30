package kuke.board.comment.service;

import jakarta.transaction.Transactional;
import kuke.board.comment.entity.ArticleCommentCount;
import kuke.board.comment.entity.Comment;
import kuke.board.comment.entity.CommentPath;
import kuke.board.comment.entity.CommentV2;
import kuke.board.comment.repository.ArticleCommentCountRepository;
import kuke.board.comment.repository.CommentRepositoryV2;
import kuke.board.comment.service.request.CommentCreateRequestV2;
import kuke.board.comment.service.response.CommentPageResponse;
import kuke.board.comment.service.response.CommentResponse;
import kuke.board.common.snowflake.Snowflake;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.function.Predicate;

import static java.util.function.Predicate.not;

@Service
@RequiredArgsConstructor
public class CommentServiceV2 {
    private final CommentRepositoryV2 repository;
    private final ArticleCommentCountRepository countRepository;
    private final Snowflake snowflake = new Snowflake();

    @Transactional
    public CommentResponse create(CommentCreateRequestV2 request) {
        CommentV2 parent = findParent(request);
        CommentPath parentCommentPath = parent == null ? CommentPath.create("") : parent.getCommentPath();
        CommentV2 comment = repository.save(
                CommentV2.create(
                        snowflake.nextId(),
                        request.getContent(),
                        request.getArticleId(),
                        request.getWriterId(),
                        parentCommentPath.createChildCommentPath(
                                repository.findDescendantTopPath(request.getArticleId(), parentCommentPath.getPath())
                                        .orElse(null)
                        )
                )
        );

        int result = countRepository.increase(request.getArticleId());
        if (result == 0) {
            countRepository.save(
                    ArticleCommentCount.init(request.getArticleId(), 1L)
            );
        }

        return CommentResponse.from(comment);
    }

    public CommentResponse read(Long commentId) {
        return CommentResponse.from(
                repository.findById(commentId).orElseThrow()
        );
    }

    @Transactional
    public void delete(Long commentId) {
        repository.findById(commentId)
                .filter(CommentV2::getDeleted)
                .ifPresent(comment -> {
                    if (hasChildren(comment)) {
                        comment.delete();
                    } else {
                        delete(comment);
                    }
                });
    }

    private boolean hasChildren(CommentV2 comment) {
        return repository.findDescendantTopPath(
                comment.getArticleId(),
                comment.getCommentPath().getParentPath()
        ).isPresent();
    }

    private void delete(CommentV2 comment) {
        repository.delete(comment);
        countRepository.decrease(comment.getArticleId());
        if (!comment.isRoot()) {
            repository.findByPath(comment.getCommentPath().getParentPath())
                    .filter(CommentV2::getDeleted)
                    .filter(not(this::hasChildren))
                    .ifPresent(this::delete);
        }
    }

    public CommentPageResponse readAll(Long articleId, Long page, Long pageSize) {
        return CommentPageResponse.of(
                repository.findAll(articleId, (page - 1) * pageSize, pageSize).stream()
                        .map(CommentResponse::from)
                        .toList(),
                repository.count(articleId, PageLimitCalculator.calculatePageLimit(page, pageSize, 10L))
        );
    }

    public List<CommentResponse> readAllInfiniteScroll(Long articleId, String lastPath, Long pageSize) {
        List<CommentV2> comments = lastPath == null ?
                repository.findAllInfiniteScroll(articleId, pageSize) :
                repository.findAllInfiniteScroll(articleId, lastPath, pageSize);

        return comments.stream()
                .map(CommentResponse::from)
                .toList();
    }

    private CommentV2 findParent(CommentCreateRequestV2 request) {
        String parentPath = request.getParentPath();
        if (parentPath == null) return null;

        return repository.findByPath(parentPath)
                .filter(not(CommentV2::getDeleted))
                .orElseThrow();
    }
}
