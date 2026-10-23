# DolDol — 비트즙 쇼핑몰

| 구분 | 스택 |
| --- | --- |
| BE | Spring Boot 4.1 · Java 21 (LTS) · Gradle · JPA · Flyway · Spring Security |
| FE | React 19 · TypeScript · Vite · React Router |
| DB | PostgreSQL 17 |
| 인프라 | Docker / Docker Compose · Kubernetes(Kustomize) · AWS Lightsail |

저장소는 둘로 나뉜다. **두 저장소를 같은 폴더 안에 나란히 clone** 한다(docker-compose 가 `../FE` 를 찾는다).

```
doldol/
├─ BE/   https://github.com/doldolred/BE   ← 이 저장소. API + 배포 설정 + 문서
└─ FE/   https://github.com/doldolred/FE      React 웹
```

```
BE/
├─ src/                Spring Boot API (com.doldol.api)
├─ k8s/                Kubernetes 매니페스트 (kustomize)
├─ docs/api.md         FE·BE 공통 API 명세
├─ docs/film-prompt.md 메인 영상 생성 프롬프트
├─ docker-compose.yml  db + backend + frontend 전체 스택
└─ .env.example        환경변수 템플릿
```

## 로컬 개발

```bash
mkdir doldol && cd doldol
git clone https://github.com/doldolred/BE.git
git clone https://github.com/doldolred/FE.git

# 1) 환경변수 파일 만들고 DB만 띄우기
cd BE && cp .env.example .env
docker compose up -d db

# 2) 백엔드 (JDK 21 없으면 Gradle 이 자동 다운로드) → http://localhost:8080
./gradlew bootRun

# 3) 프론트 → http://localhost:5173  (/api 는 8080 으로 프록시)
cd ../FE && npm install && npm run dev
```

- 테스트: `./gradlew test` (Testcontainers 로 Postgres 자동 기동, Docker 필요)
- DB 스키마는 Flyway 로만 변경: `src/main/resources/db/migration/V{번호}__설명.sql`
  (JPA 는 `ddl-auto: validate`)

## Docker 로 전체 실행

```bash
cp .env.example .env   # 비밀번호 등 수정
docker compose up -d --build
# http://localhost  (WEB_PORT 로 변경 가능)
```

## AWS Lightsail 배포

운영은 **[deploy/README.md](deploy/README.md)** — 1대에 nginx(HTTPS) · API · 웹 · Postgres · 자동 백업을 docker compose 로.
이미지는 GitHub Actions 가 만들어 GHCR 에 올리고, 서버는 `deploy/deploy.sh` 로 받아서 띄운다.

k8s(`k8s/`, k3s 용 매니페스트)는 규모가 커졌을 때를 위해 남겨 둔다.

## 도메인 확정 후 할 일

- [ ] Lightsail DNS 또는 외부 DNS 에 A 레코드 → 고정 IP
- [ ] HTTPS — `deploy/init-letsencrypt.sh` (k3s 로 갈 때는 cert-manager)
- [ ] `CORS_ALLOWED_ORIGINS`, `k8s/ingress.yaml` host 갱신
