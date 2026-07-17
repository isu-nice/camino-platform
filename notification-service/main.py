import asyncio
import json
import redis.asyncio as redis
from fastapi import FastAPI
from contextlib import asynccontextmanager

REDIS_HOST = "localhost"
REDIS_PORT = 6379
CHANNEL = "reservation-events"


async def listen_to_reservations():
    """Redis 채널을 계속 듣고 있다가, 메시지가 오면 처리하는 함수"""
    r = redis.Redis(host=REDIS_HOST, port=REDIS_PORT, decode_responses=True)
    pubsub = r.pubsub()
    await pubsub.subscribe(CHANNEL)
    print(f"[구독 시작] 채널: {CHANNEL}")

    async for message in pubsub.listen():
        if message["type"] == "message":
            data = json.loads(message["data"])
            await send_notification(data)


async def send_notification(data: dict):
    """실제로는 이메일/푸시를 보내겠지만, 지금은 콘솔 로그로 흉내"""
    print(f"[알림 발송] 순례자 {data['pilgrimId']}님, "
          f"{data['albergueName']} 예약이 완료되었습니다! "
          f"(예약일: {data['reservationDate']}, 예약번호: {data['reservationId']})")


@asynccontextmanager
async def lifespan(app: FastAPI):
    # 서버 시작 시: 구독 작업을 백그라운드로 띄움
    task = asyncio.create_task(listen_to_reservations())
    yield
    # 서버 종료 시: 작업 정리
    task.cancel()


app = FastAPI(lifespan=lifespan)


@app.get("/health")
def health():
    return {"status": "OK"}