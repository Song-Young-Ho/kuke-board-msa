package kuke.board.article.repository;

import kuke.board.article.entity.Article;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
@SpringBootTest
class ArticleRepositoryTest {
    @Autowired ArticleRepository repository;

    @Test
    void findAll() {
        List<Article> articles = repository.findAll(1L, 1499970L, 30L);
        log.info("articles.size = {}", articles.size());
        for (Article article : articles) {
            log.info("article = {}", article);
        }
    }

    @Test
    void count() {
        Long count = repository.count(1L, 10000L);
        log.info("count = {}", count);
    }

    @Test
    void findInfiniteScroll() {
        List<Article> articles = repository.findAllInfiniteScroll(1L, 30L);
        log.info("articles.size = {}", articles.size());
        for (Article article : articles) {
            log.info("article = {}", article.getArticleId());
        }

        Long lastArticleId = articles.getLast().getArticleId();
        List<Article> articles2 = repository.findAllInfiniteScroll(1L, 30L, lastArticleId);

        for (Article article : articles2) {
            log.info("article = {}", article.getArticleId());
        }
    }
}