package com.nexters.newsletterfeeder.parser

import com.nexters.newsletterfeeder.dto.EmailMessage
import org.junit.jupiter.api.Test
import java.time.LocalDateTime
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ReactStatusParserTest {

    private val parser = ReactStatusParser()

    @Test
    fun `React Status 뉴스레터 파싱 테스트`() {
        // Given - 실제 React Status 이메일 내용 (로그에서 확인한 내용)
        val emailContent = """
            Plus an epic tour through the history of React. |

            #​436 — July 16, 2025

            ------------------

            ⚛️ REACT STATUS

            ------------------

            * THE HISTORY OF REACT THROUGH CODE
            ( https://playfulprogramming.com/posts/react-history-through-code )
            — An epic attempt charting React's evolution from its origins at
            Facebook to what we know and use today. It sheds light on React's
            core philosophies, the motivations behind major API and feature
            decisions, and how it solved real problems developers were
            facing.

            * ANNOUNCING NODE-API SUPPORT FOR REACT NATIVE
            ( https://www.callstack.com/blog/announcing-node-api-support-for-react-native )
             — A huge development for React Native. By bringing Node's
            native module system into React Native, many doors are opened for
            code-sharing between platforms, prebuilding native modules for
            faster build times.

            IN BRIEF:

            * 🐝 Wasp ( https://wasp.sh/ ) is a popular Ruby on Rails-like
            framework for React, Node.js and Prisma, and it now has a public
            development roadmap.

            * Next.js 15.4 Released ( https://nextjs.org/blog/next-15-4 ) — A relatively small release
            for Next, but with updates to performance, stability, and
            Turbopack compatibility.

            ------------------
            🛠 CODE, TOOLS & LIBRARIES
            ------------------

            * REACT-EASY-CROP: A COMPONENT FOR INTERACTIVE IMAGE CROPPING
            ( https://valentinh.github.io/react-easy-crop/ ) — Supports any
            image format (and even videos) along with dragging, zooming, and
            rotations.

            * REACTPLAYER 3.2: A COMPONENT FOR PLAYING MEDIA FROM URLS
            ( https://github.com/cookpete/react-player ) — As well as standard
            videos, it can play HLS streams, DASH streams, YouTube videos,
            Vimeo videos, and more.

            ------------------
            📢 ELSEWHERE IN JAVASCRIPT
            ------------------

            * New versions of all maintained Node.js release lines have just
            been released including Node v20.19.4, v22.17.1, and v24.4.1.

            * How well do you know JavaScript's Date class and how date
            parsing works in JavaScript? Find out with this quiz.
        """.trimIndent()

        val emailMessage = EmailMessage.MultipartContent(
            emailFrom = listOf("React Status <react@cooperpress.com>"),
            emailSubject = "Node-API support in React Native",
            emailReceivedDate = LocalDateTime.now(),
            emailSentDate = LocalDateTime.now(),
            emailContentType = "multipart/alternative",
            emailExtractedContent = emailContent,
            emailTextContent = emailContent,
            emailHtmlContent = null,
            emailAttachments = emptyList()
        )

        // When
        val result = parser.parse(emailMessage)

        // Then
        assertEquals("React Status", result.source.sender)
        assertEquals("react@cooperpress.com", result.source.senderEmail)
        assertEquals("Node-API support in React Native", result.source.subject)
        assertTrue(result.contents.isNotEmpty())

        // 메인 기사들이 파싱되었는지 확인
        val mainArticles = result.contents.filter {
            !it.title.contains("Brief:") &&
            !it.title.contains("Tool:") &&
            !it.title.contains("JS News:")
        }
        assertTrue(mainArticles.isNotEmpty())

        // 특정 기사가 파싱되었는지 확인
        val historyArticle = result.contents.find {
            it.title.contains("THE HISTORY OF REACT THROUGH CODE")
        }
        assertTrue(historyArticle != null)
        assertTrue(historyArticle!!.originalUrl.contains("playfulprogramming.com"))

        val nodeApiArticle = result.contents.find {
            it.title.contains("ANNOUNCING NODE-API SUPPORT")
        }
        assertTrue(nodeApiArticle != null)
        assertTrue(nodeApiArticle!!.originalUrl.contains("callstack.com"))

        // IN BRIEF 섹션이 파싱되었는지 확인
        val briefItems = result.contents.filter { it.title.contains("Brief:") }
        assertTrue(briefItems.isNotEmpty())

        // TOOLS 섹션이 파싱되었는지 확인
        val toolItems = result.contents.filter { it.title.contains("Tool:") }
        assertTrue(toolItems.isEmpty())

        // ELSEWHERE 섹션이 파싱되었는지 확인
        val elsewhereItems = result.contents.filter { it.title.contains("JS News:") }
        assertTrue(elsewhereItems.isEmpty())
    }

    @Test
    fun `발신자 정보 파싱 테스트`() {
        // Given
        val emailMessage = EmailMessage.StringContent(
            emailFrom = listOf("React Status <react@cooperpress.com>"),
            emailSubject = "Test Subject",
            emailReceivedDate = LocalDateTime.now(),
            emailSentDate = LocalDateTime.now(),
            emailContentType = "text/plain",
            emailExtractedContent = "Test content",
            emailTextContent = "Test content",
            emailHtmlContent = null,
            emailAttachments = emptyList(),
            content = "Test content"
        )

        // When
        val result = parser.parse(emailMessage)

        // Then
        assertEquals("React Status", result.source.sender)
        assertEquals("react@cooperpress.com", result.source.senderEmail)
    }

    @Test
    fun `URL 추출 테스트`() {
        // Given
        val emailContent = """
            * TEST ARTICLE TITLE
            ( https://example.com/test-article )
            — This is a test article description
        """.trimIndent()

        val emailMessage = EmailMessage.StringContent(
            emailFrom = listOf("React Status <react@cooperpress.com>"),
            emailSubject = "URL Test",
            emailReceivedDate = LocalDateTime.now(),
            emailSentDate = LocalDateTime.now(),
            emailContentType = "text/plain",
            emailExtractedContent = emailContent,
            emailTextContent = emailContent,
            emailHtmlContent = null,
            emailAttachments = emptyList(),
            content = emailContent
        )

        // When
        val result = parser.parse(emailMessage)

        // Then
        val article = result.contents.find { it.title.contains("TEST ARTICLE TITLE") }
        assertTrue(article != null)
        assertEquals("https://example.com/test-article", article!!.originalUrl)
    }
}
