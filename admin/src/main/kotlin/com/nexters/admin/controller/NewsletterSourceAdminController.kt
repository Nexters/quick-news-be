package com.nexters.admin.controller

import com.nexters.external.entity.Content
import com.nexters.external.entity.NewsletterSource
import com.nexters.external.parser.MailContent
import com.nexters.external.parser.MailParserFactory
import com.nexters.external.repository.ContentRepository
import com.nexters.external.repository.NewsletterSourceRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDate
import java.time.LocalDateTime

@Controller
@RequestMapping("/newsletter-sources")
class NewsletterSourceAdminController(
    private val newsletterSourceRepository: NewsletterSourceRepository,
    private val contentRepository: ContentRepository
) {
    @GetMapping
    fun getNewsletterSourcesPage(model: Model): String = "newsletter-sources"
}

@RestController
@RequestMapping("/api/newsletter-sources")
class NewsletterSourceApiController(
    private val newsletterSourceRepository: NewsletterSourceRepository,
    private val contentRepository: ContentRepository
) {
    private val mailParserFactory = MailParserFactory()

    @GetMapping
    fun getAllNewsletterSources(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int,
        @RequestParam(required = false) senderEmail: String?,
        @RequestParam(required = false) subject: String?
    ): ResponseEntity<Page<NewsletterSource>> {
        val pageable =
            PageRequest.of(
                page,
                size,
                Sort.by(Sort.Direction.DESC, "receivedDate")
            )

        val newsletterSources =
            when {
                senderEmail != null -> {
                    val sources = newsletterSourceRepository.findBySenderEmail(senderEmail)
                    // Page로 변환
                    val start = page * size
                    val end = minOf(start + size, sources.size)
                    val content = sources.subList(start, end)
                    PageImpl(content, pageable, sources.size.toLong())
                }
                subject != null -> {
                    val sources = newsletterSourceRepository.findBySubjectContaining(subject)
                    // Page로 변환
                    val start = page * size
                    val end = minOf(start + size, sources.size)
                    val content = sources.subList(start, end)
                    PageImpl(content, pageable, sources.size.toLong())
                }
                else -> newsletterSourceRepository.findAll(pageable)
            }

        return ResponseEntity.ok(newsletterSources)
    }

    @GetMapping("/{id}")
    fun getNewsletterSourceById(
        @PathVariable id: String
    ): ResponseEntity<NewsletterSource> {
        val newsletterSource =
            newsletterSourceRepository
                .findById(id)
                .orElseThrow { NoSuchElementException("NewsletterSource not found with id: $id") }
        return ResponseEntity.ok(newsletterSource)
    }

    @PostMapping("/{id}/create-content")
    fun createContentFromNewsletterSource(
        @PathVariable id: String,
        @RequestBody request: CreateContentFromNewsletterSourceRequest
    ): ResponseEntity<Content> {
        val newsletterSource =
            newsletterSourceRepository
                .findById(id)
                .orElseThrow { NoSuchElementException("NewsletterSource not found with id: $id") }

        val newContent =
            Content(
                newsletterSourceId = newsletterSource.id,
                title = request.title,
                content = request.content,
                newsletterName = request.newsletterName,
                originalUrl = request.originalUrl,
                publishedAt = request.publishedAt,
                createdAt = LocalDateTime.now(),
                updatedAt = LocalDateTime.now()
            )

        val savedContent = contentRepository.save(newContent)
        return ResponseEntity.ok(savedContent)
    }

    @GetMapping("/sender-emails")
    fun getSenderEmails(): ResponseEntity<List<String>> {
        val senderEmails =
            newsletterSourceRepository
                .findAll()
                .map { it.senderEmail }
                .distinct()
                .sorted()
        return ResponseEntity.ok(senderEmails)
    }

    @GetMapping("/subjects")
    fun getSubjects(): ResponseEntity<List<String>> {
        val subjects =
            newsletterSourceRepository
                .findAll()
                .mapNotNull { it.subject }
                .distinct()
                .sorted()
        return ResponseEntity.ok(subjects)
    }

    @PutMapping("/{id}")
    fun updateNewsletterSource(
        @PathVariable id: String,
        @RequestBody request: UpdateNewsletterSourceRequest
    ): ResponseEntity<NewsletterSource> {
        val newsletterSource =
            newsletterSourceRepository
                .findById(id)
                .orElseThrow { NoSuchElementException("NewsletterSource not found with id: $id") }

        val updatedNewsletterSource =
            newsletterSource.copy(
                subject = request.subject,
                content = request.content,
                sender = request.sender,
                senderEmail = request.senderEmail,
                recipient = request.recipient,
                recipientEmail = request.recipientEmail,
                contentType = request.contentType,
                updatedAt = LocalDateTime.now()
            )

        val savedNewsletterSource = newsletterSourceRepository.save(updatedNewsletterSource)
        return ResponseEntity.ok(savedNewsletterSource)
    }

    @PostMapping("/{id}/test-parser")
    fun testParser(
        @PathVariable id: String
    ): ResponseEntity<ParserTestResult> {
        val newsletterSource =
            newsletterSourceRepository
                .findById(id)
                .orElseThrow { NoSuchElementException("NewsletterSource not found with id: $id") }

        val parser = mailParserFactory.findParser(newsletterSource.senderEmail)
        val isTarget = parser != null

        val parsedContents =
            if (isTarget && parser != null) {
                try {
                    parser.parse(newsletterSource.content)
                } catch (e: Exception) {
                    emptyList()
                }
            } else {
                emptyList()
            }

        val result =
            ParserTestResult(
                isTarget = isTarget,
                parsedContents = parsedContents,
                originalContent = newsletterSource.content,
                senderEmail = newsletterSource.senderEmail,
                parserName = parser?.javaClass?.simpleName ?: "None"
            )

        return ResponseEntity.ok(result)
    }

    @PostMapping("/{id}/add-parsed-content")
    fun addParsedContent(
        @PathVariable id: String,
        @RequestBody request: AddParsedContentRequest
    ): ResponseEntity<Content> {
        val newsletterSource =
            newsletterSourceRepository
                .findById(id)
                .orElseThrow { NoSuchElementException("NewsletterSource not found with id: $id") }

        val newContent =
            Content(
                newsletterSourceId = newsletterSource.id,
                title = request.title,
                content = request.content,
                newsletterName = request.newsletterName,
                originalUrl = request.originalUrl,
                publishedAt = request.publishedAt,
                createdAt = LocalDateTime.now(),
                updatedAt = LocalDateTime.now()
            )

        val savedContent = contentRepository.save(newContent)
        return ResponseEntity.ok(savedContent)
    }
}

data class CreateContentFromNewsletterSourceRequest(
    val title: String,
    val content: String,
    val newsletterName: String,
    val originalUrl: String,
    val publishedAt: LocalDate
)

data class UpdateNewsletterSourceRequest(
    val subject: String?,
    val content: String,
    val sender: String,
    val senderEmail: String,
    val recipient: String,
    val recipientEmail: String,
    val contentType: String
)

data class ParserTestResult(
    val isTarget: Boolean,
    val parsedContents: List<MailContent>,
    val originalContent: String,
    val senderEmail: String,
    val parserName: String
)

data class AddParsedContentRequest(
    val title: String,
    val content: String,
    val newsletterName: String,
    val originalUrl: String,
    val publishedAt: LocalDate
)
