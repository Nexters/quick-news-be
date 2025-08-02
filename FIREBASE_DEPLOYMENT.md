# Firebase 환경 변수 설정 가이드 (단일 환경)

## 1. JSON 파일을 환경 변수로 변환

```bash
# 다운로드받은 firebase-service-account.json 파일을 한 줄로 변환
cat firebase-service-account.json | jq -c .
```

## 2. 환경 변수 설정

### 로컬 개발
```bash
# 환경 변수 설정
export FIREBASE_SERVICE_ACCOUNT_KEY='{"type":"service_account","project_id":"news-letter-da24c",...}'
export FIREBASE_PROJECT_ID='news-letter-da24c'

# 앱 실행
./gradlew bootRun
```

### 서버 배포
```bash
# 환경 변수 설정 후 실행
export FIREBASE_SERVICE_ACCOUNT_KEY='{"type":"service_account",...}'
export FIREBASE_PROJECT_ID='news-letter-da24c'
java -jar api.jar
```

### Docker 실행
```bash
docker run \
  -e FIREBASE_SERVICE_ACCOUNT_KEY='{"type":"service_account",...}' \
  -e FIREBASE_PROJECT_ID='news-letter-da24c' \
  your-app:latest
```

### .env 파일 사용 (권장)
```bash
# .env 파일 생성
cat > .env << 'EOF'
FIREBASE_SERVICE_ACCOUNT_KEY={"type":"service_account","project_id":"news-letter-da24c","private_key_id":"...","private_key":"-----BEGIN PRIVATE KEY-----\n...\n-----END PRIVATE KEY-----\n","client_email":"..."}
FIREBASE_PROJECT_ID=news-letter-da24c
EOF

# .env 파일 로드 후 실행
set -a && source .env && set +a
./gradlew bootRun
```

## 3. 성공 로그 확인

애플리케이션 시작시 다음 로그가 나타나야 합니다:
```
INFO  c.n.a.config.FirebaseConfig - Firebase 초기화 중...
INFO  c.n.a.config.FirebaseConfig - Firebase Admin SDK 초기화 완료 - 프로젝트: news-letter-da24c
```

## 4. 트러블슈팅

### 환경 변수 확인
```bash
echo $FIREBASE_SERVICE_ACCOUNT_KEY | jq .
echo $FIREBASE_PROJECT_ID
```

### JSON 형식 검증
```bash
# JSON 유효성 검사
echo $FIREBASE_SERVICE_ACCOUNT_KEY | jq . > /dev/null && echo "✅ 유효" || echo "❌ 오류"
```

## 5. 주의사항

- 환경 변수는 **반드시** 설정해야 함 (설정 안 하면 앱 시작 실패)
- JSON 파일은 서버에 올리지 말고 환경 변수로만 사용
- `.env` 파일은 Git에 커밋하지 말 것
