-- Users table
CREATE TABLE IF NOT EXISTS users
(
    id           SERIAL PRIMARY KEY,
    device_token VARCHAR(255) NOT NULL UNIQUE,
    created_at   TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at   TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Categories table
CREATE TABLE IF NOT EXISTS categories
(
    id         SERIAL PRIMARY KEY,
    name       VARCHAR(255) NOT NULL UNIQUE,
    created_at TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- User-Category mappings table
CREATE TABLE IF NOT EXISTS user_category_mappings
(
    id          SERIAL PRIMARY KEY,
    user_id     BIGINT    NOT NULL,
    category_id BIGINT    NOT NULL,
    created_at  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (user_id, category_id),
    FOREIGN KEY (user_id) REFERENCES users (id),
    FOREIGN KEY (category_id) REFERENCES categories (id)
);

-- Reserved keywords table
CREATE TABLE IF NOT EXISTS reserved_keywords
(
    id         SERIAL PRIMARY KEY,
    name       VARCHAR(255) NOT NULL UNIQUE,
    created_at TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Candidate keywords table
CREATE TABLE IF NOT EXISTS candidate_keywords
(
    id         SERIAL PRIMARY KEY,
    name       VARCHAR(255) NOT NULL UNIQUE,
    created_at TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Contents table
CREATE TABLE IF NOT EXISTS contents
(
    id                   SERIAL PRIMARY KEY,
    newsletter_source_id VARCHAR(255),
    title                VARCHAR(255) NOT NULL,
    content              TEXT         NOT NULL,
    newsletter_name      VARCHAR(255) NOT NULL,
    original_url         VARCHAR(255) NOT NULL,
    created_at           TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at           TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Summaries table
CREATE TABLE IF NOT EXISTS summaries
(
    id                 SERIAL PRIMARY KEY,
    content_id         BIGINT       NOT NULL,
    title              VARCHAR(255) NOT NULL,
    summarized_content TEXT         NOT NULL,
    summarized_at      TIMESTAMP    NOT NULL,
    model              VARCHAR(50)  NOT NULL,
    created_at         TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at         TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (content_id) REFERENCES contents (id)
);

-- Category-Keyword mappings table
CREATE TABLE IF NOT EXISTS category_keyword_mappings
(
    id          SERIAL PRIMARY KEY,
    category_id BIGINT           NOT NULL,
    keyword_id  BIGINT           NOT NULL,
    weight      DOUBLE PRECISION NOT NULL,
    created_at  TIMESTAMP        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (category_id, keyword_id),
    FOREIGN KEY (category_id) REFERENCES categories (id),
    FOREIGN KEY (keyword_id) REFERENCES reserved_keywords (id)
);

-- Reserved-Candidate keyword mappings table
CREATE TABLE IF NOT EXISTS keyword_mappings
(
    reserved_keyword_id  BIGINT NOT NULL,
    candidate_keyword_id BIGINT NOT NULL,
    PRIMARY KEY (reserved_keyword_id, candidate_keyword_id),
    FOREIGN KEY (reserved_keyword_id) REFERENCES reserved_keywords (id),
    FOREIGN KEY (candidate_keyword_id) REFERENCES candidate_keywords (id)
);

-- Content-Keyword mappings table
CREATE TABLE IF NOT EXISTS content_keyword_mappings
(
    id         SERIAL PRIMARY KEY,
    content_id BIGINT    NOT NULL,
    keyword_id BIGINT    NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (content_id, keyword_id),
    FOREIGN KEY (content_id) REFERENCES contents (id),
    FOREIGN KEY (keyword_id) REFERENCES reserved_keywords (id)
);

-- Newsletter sources table
CREATE TABLE IF NOT EXISTS newsletter_sources
(
    id         SERIAL PRIMARY KEY,
    name       VARCHAR(255) NOT NULL UNIQUE,
    email      VARCHAR(255) NOT NULL UNIQUE,
    created_at TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS exposure_contents
(
    id                   BIGSERIAL PRIMARY KEY,
    content_id           BIGINT       NOT NULL,
    provocative_keyword  VARCHAR(255) NOT NULL,
    provocative_headline VARCHAR(255) NOT NULL,
    summary_content      TEXT         NOT NULL,
    created_at           TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at           TIMESTAMP    NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_exposure_content_content FOREIGN KEY (content_id) REFERENCES contents (id)
);

CREATE INDEX IF NOT EXISTS idx_exposure_content_content_id ON exposure_contents (content_id);
