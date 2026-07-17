# 카미노 알베르게 실시간 예약 플랫폼

산티아고 순례길(카미노 데 산티아고)의 알베르게(순례자 숙소)는 대부분 선착순 배정이라, 성수기나 인기 구간에서는 오픈 시간에 예약이 몰리는 문제가 있습니다. 3개월간 카미노+유럽을 여행하며 직접 겪은 이 문제를, 백엔드+데브옵스 기술로 풀어보는 개인 프로젝트입니다.

> 🚧 현재 개발 진행 중입니다. 아래 진행 상황을 계속 업데이트합니다.

## 왜 만들었나

기능 구현 자체보다, **"트래픽이 몰리는 상황에서 시스템을 어떻게 설계하고 검증하는가"**를 직접 겪어보고 싶어서 시작했습니다. 그래서 기능을 먼저 만들고 나중에 인프라를 붙이는 대신, 처음부터 Docker/Kubernetes 배포 파이프라인을 뼈대로 두고 그 위에 기능을 얹는 방식(Walking Skeleton)으로 진행하고 있습니다.

## 아키텍처

```
                Client
                  │
        ┌─────────┴─────────┐
        │   GKE 클러스터     │
        │                   │
   ┌────▼────┐   ┌─────────▼──────┐
   │  Auth   │   │   Albergue     │
   │ Service │   │   Service      │
   │(Spring) │   │  (Spring)      │
   └────┬────┘   └────────┬───────┘
        │                 │
        └────────┬────────┘
                  │
        ┌─────────┴─────────┐
        │   PostgreSQL       │
        │   Redis            │
        └─────────────────────┘

(추후 추가 예정: Notification Service, GKE 배포, Terraform, CI/CD 완성)
```

## 기술 스택

| 분류 | 기술 |
|---|---|
| Backend | Java 17, Spring Boot 3, Spring Data JPA, Spring Security |
| Notification | Python 3, FastAPI, Redis Pub/Sub |
| Auth | JWT (jjwt) |
| Database | PostgreSQL, Redis |
| Infra | Docker (멀티스테이지 빌드), Kubernetes (kind → GKE 예정) |
| CI/CD | GitHub Actions |
| 부하테스트 | k6 |
| 로컬 개발 | docker-compose |

## 진행 상황

- [x] Docker/kind 로컬 k8s 배포 파이프라인 뼈대 구축
- [x] GitHub Actions CI 구성 (build + test 자동화)
- [x] Auth Service — 회원가입, 로그인, JWT 발급
- [x] Albergue Service — 예약 API (기본 버전)
- [x] k6 부하테스트로 동시 예약 충돌 재현 → [트러블슈팅 로그](./TROUBLESHOOTING.md)
- [x] 동시성 충돌 처리 (409 응답, 재시도 로직)
- [x] Redis 분산 락 도입 및 비교 (낙관적 락+재시도 vs 분산 락)
- [x] CI matrix 전략으로 멀티서비스 병렬 빌드
- [x] DB 커넥션 풀 고갈 시나리오 (25.75% → 100% 성공률)
- [x] N+1 쿼리 최적화 (9회 → 1회 쿼리)
- [x] 캐시 스탬피드 시나리오 (분산 락 재사용, DB 쿼리 상수화)
- [x] k8s 리소스 최적화 (request/limit, OOMKilled 재현/해결)
- [x] Notification Service (FastAPI) — Redis Pub/Sub으로 예약 이벤트 구독
- [ ] GKE 배포 + Terraform
- [ ] Prometheus + Grafana 모니터링
- [ ] HPA (오토스케일링)

## 프로젝트 구조

```
camino-platform/
├── auth-service/         # 인증 (회원가입, 로그인, JWT)
├── albergue-service/     # 알베르게 예약 (동시성 처리 핵심)
├── notification-service/ # 알림 (FastAPI, Redis Pub/Sub 구독)
├── k8s/                  # Kubernetes 매니페스트
├── load-tests/           # k6 부하테스트 스크립트
├── docker-compose.yml    # 로컬 개발 환경
├── TROUBLESHOOTING.md    # 트러블슈팅 로그
└── .github/workflows/    # CI 파이프라인
```

## 로컬 실행 방법

### 요구사항
- Docker Desktop
- JDK 17
- kind, kubectl (k8s 배포 테스트용)
- k6 (부하테스트용)

### 1. 환경변수 설정

```bash
cp .env.example .env
# 필요 시 .env 값 수정 (로컬 개발은 기본값으로 충분)
```

### 2. 전체 스택 실행 (docker-compose)

```bash
docker-compose up --build
```

- Auth Service: `http://localhost:8080`
- Albergue Service: `http://localhost:8081`

### 3. 헬스체크

```bash
curl localhost:8080/health
curl localhost:8081/health
```

### 4. (선택) 로컬 Kubernetes 배포 검증

```bash
kind create cluster --name camino-local
kind load docker-image camino-auth:0.1 --name camino-local
kubectl apply -f k8s/
```

## API 예시

**회원가입**
```bash
curl -X POST localhost:8080/api/auth/signup \
  -H "Content-Type: application/json" \
  -d '{"email":"pilgrim@camino.com","password":"password123","nickname":"산티아고"}'
```

**로그인**
```bash
curl -X POST localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"pilgrim@camino.com","password":"password123"}'
```

**예약**
```bash
curl -X POST localhost:8081/api/reservations \
  -H "Content-Type: application/json" \
  -d '{"albergueId":1,"pilgrimId":1,"reservationDate":"2026-08-01"}'
```

## 부하테스트

```bash
k6 run load-tests/reservation-race.js
```

동시 예약 요청을 재현해 침대 재고보다 많은 예약이 발생하는지, 응답 시간/성공률이 어떻게 변하는지 확인합니다. 자세한 결과와 해결 과정은 [TROUBLESHOOTING.md](./TROUBLESHOOTING.md) 참고.

## 트러블슈팅 로그

문제를 어떻게 재현하고, 원인을 어떻게 분석하고, 어떻게 해결했는지는 별도 문서에 정리하고 있습니다.
→ [TROUBLESHOOTING.md](./TROUBLESHOOTING.md)

---

*이 프로젝트는 진행 중이며, 이 README와 트러블슈팅 로그는 계속 업데이트됩니다.*