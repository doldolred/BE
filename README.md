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
├─ .env.example        환경변수 템플릿
└─ BE-타이핑용-*.md    단계별 java 코드 (직접 타이핑용)
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

인스턴스 1대 기준. 추천: Ubuntu 24.04, **2GB RAM 이상** (JVM + Postgres + nginx).

### A. Docker Compose (초기 추천)

```bash
# 인스턴스에서
sudo apt-get update && sudo apt-get install -y docker.io docker-compose-v2 git
sudo usermod -aG docker $USER   # 재접속
mkdir doldol && cd doldol
git clone https://github.com/doldolred/BE.git && git clone https://github.com/doldolred/FE.git
cd BE && cp .env.example .env && vi .env
docker compose up -d --build
```

- Lightsail 네트워킹 탭에서 HTTP(80)/HTTPS(443) 열기, 5432 는 열지 않기
- 고정 IP(Static IP) 붙이기 → 도메인 확정 시 A 레코드 연결
- 운영에서는 `.env` 의 `DB_PORT` 바인딩을 제거하거나 `127.0.0.1:5432:5432` 로 제한 권장

### B. Kubernetes (k3s)

Lightsail 단일 인스턴스에 k3s 를 올려 같은 매니페스트를 사용.

```bash
curl -sfL https://get.k3s.io | sh -
cp k8s/secret.example.yaml k8s/secret.yaml   # 값 수정 후 kustomization.yaml 에서 주석 해제
# 이미지 레지스트리(ECR / GHCR 등) push 후 k8s/kustomization.yaml 의 images 수정
sudo k3s kubectl apply -k k8s
```

## 도메인 확정 후 할 일

- [ ] Lightsail DNS 또는 외부 DNS 에 A 레코드 → 고정 IP
- [ ] HTTPS (Compose: certbot / Caddy, k3s: cert-manager)
- [ ] `CORS_ALLOWED_ORIGINS`, `k8s/ingress.yaml` host 갱신
