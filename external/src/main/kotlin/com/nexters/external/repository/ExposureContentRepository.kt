package com.nexters.external.repository

import com.nexters.external.entity.Content
import com.nexters.external.entity.ExposureContent
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface ExposureContentRepository : JpaRepository<ExposureContent, Long> {
    fun findByContent(content: Content): ExposureContent?

    @Query(
        """
        SELECT c FROM Content c
        WHERE c.id IN (
            SELECT DISTINCT e.content.id FROM ExposureContent e
        )
    """
    )
    fun findContentsWithExposure(pageable: Pageable): Page<Content>

    @Query(
        """
        SELECT c FROM Content c
        WHERE c.id NOT IN (
            SELECT DISTINCT e.content.id FROM ExposureContent e
        )
    """
    )
    fun findContentsWithoutExposure(pageable: Pageable): Page<Content>

    @Query(
        """
        SELECT DISTINCT c FROM Content c
        JOIN ContentKeywordMapping ckm ON c.id = ckm.content.id
        JOIN CategoryKeywordMapping catkm ON ckm.keyword.id = catkm.keyword.id
        WHERE catkm.category.id = :categoryId
        AND c.id IN (
            SELECT DISTINCT e.content.id FROM ExposureContent e
        )
    """
    )
    fun findContentsByCategoryWithExposure(
        @Param("categoryId") categoryId: Long,
        pageable: Pageable
    ): Page<Content>

    @Query(
        """
        SELECT DISTINCT c FROM Content c
        JOIN ContentKeywordMapping ckm ON c.id = ckm.content.id
        JOIN CategoryKeywordMapping catkm ON ckm.keyword.id = catkm.keyword.id
        WHERE catkm.category.id = :categoryId
        AND c.id NOT IN (
            SELECT DISTINCT e.content.id FROM ExposureContent e
        )
    """
    )
    fun findContentsByCategoryWithoutExposure(
        @Param("categoryId") categoryId: Long,
        pageable: Pageable
    ): Page<Content>

    // 뉴스레터 이름으로 필터링하는 메서드 추가
    @Query(
        """
        SELECT c FROM Content c
        WHERE c.newsletterName = :newsletterName
        AND c.id IN (
            SELECT DISTINCT e.content.id FROM ExposureContent e
        )
    """
    )
    fun findContentsWithExposureByNewsletterName(
        @Param("newsletterName") newsletterName: String,
        pageable: Pageable
    ): Page<Content>

    @Query(
        """
        SELECT c FROM Content c
        WHERE c.newsletterName = :newsletterName
        AND c.id NOT IN (
            SELECT DISTINCT e.content.id FROM ExposureContent e
        )
    """
    )
    fun findContentsWithoutExposureByNewsletterName(
        @Param("newsletterName") newsletterName: String,
        pageable: Pageable
    ): Page<Content>

    @Query(
        """
        SELECT DISTINCT c FROM Content c
        JOIN ContentKeywordMapping ckm ON c.id = ckm.content.id
        JOIN CategoryKeywordMapping catkm ON ckm.keyword.id = catkm.keyword.id
        WHERE catkm.category.id = :categoryId
        AND c.newsletterName = :newsletterName
        AND c.id IN (
            SELECT DISTINCT e.content.id FROM ExposureContent e
        )
    """
    )
    fun findContentsByCategoryWithExposureAndNewsletterName(
        @Param("categoryId") categoryId: Long,
        @Param("newsletterName") newsletterName: String,
        pageable: Pageable
    ): Page<Content>

    @Query(
        """
        SELECT DISTINCT c FROM Content c
        JOIN ContentKeywordMapping ckm ON c.id = ckm.content.id
        JOIN CategoryKeywordMapping catkm ON ckm.keyword.id = catkm.keyword.id
        WHERE catkm.category.id = :categoryId
        AND c.newsletterName = :newsletterName
        AND c.id NOT IN (
            SELECT DISTINCT e.content.id FROM ExposureContent e
        )
    """
    )
    fun findContentsByCategoryWithoutExposureAndNewsletterName(
        @Param("categoryId") categoryId: Long,
        @Param("newsletterName") newsletterName: String,
        pageable: Pageable
    ): Page<Content>
}
