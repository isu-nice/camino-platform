package com.camino.albergue.service;

import com.camino.albergue.domain.Albergue;
import com.camino.albergue.domain.AlbergueRepository;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class AlbergueQueryService {

    private final AlbergueRepository albergueRepository;
    private final CacheManager cacheManager;
    private final RedissonClient redissonClient;

    public AlbergueQueryService(AlbergueRepository albergueRepository,
                                CacheManager cacheManager,
                                RedissonClient redissonClient) {
        this.albergueRepository = albergueRepository;
        this.cacheManager = cacheManager;
        this.redissonClient = redissonClient;
    }

    /*

    핵심 로직 설명
    1. 먼저 캐시를 직접 확인 — 있으면 바로 반환 (대부분의 요청은 여기서 끝남)
    2. 캐시 미스면 락을 시도 — 이 순간 동시에 여러 요청이 몰려도,
                            락은 한 번에 하나만 획득 가능
    3. 락을 획득한 요청만 실제 DB 조회 + 캐시 저장 — 이게 딱 1번만 일어남
    4. 락을 못 얻은 나머지 요청들 — "누군가 채우고 있겠지"라고 판단하고 짧게 대기(100ms) 후 캐시를 다시 확인.
                                이미 채워졌으면 그걸 가져다 씀
    5. 더블 체크(double-checked locking) 패턴: 락을 획득한 직후에도 캐시를 한 번 더 확인하는 이유는,
                                락 대기 중에 이미 다른 스레드가 채웠을 수도 있기 때문
                                (완전히 동시에 도착한 게 아니라 살짝 시차가 있는 경우)
     */

    public Albergue getById(Long id) {
        Cache cache = cacheManager.getCache("albergue");
        Cache.ValueWrapper cached = cache.get(id);
        if (cached != null) {
            return (Albergue) cached.get(); // 캐시 히트 - 바로 반환
        }

        // 캐시 미스 - 락을 걸어서 한 요청만 DB를 조회하게 함
        String lockKey = "lock:cache:albergue:" + id;
        RLock lock = redissonClient.getLock(lockKey);

        try {
            boolean acquired = lock.tryLock(2, 1, TimeUnit.SECONDS);
            if (!acquired) {
                // 락 획득 실패 - 다른 요청이 이미 채우는 중이므로 짧게 기다렸다가 캐시 재확인
                Thread.sleep(100);
                Cache.ValueWrapper retry = cache.get(id);
                if (retry != null) {
                    return (Albergue) retry.get();
                }
                // 그래도 없으면 어쩔 수 없이 직접 조회 (최후 수단)
                return fetchAndCache(id, cache);
            }

            // 락 획득한 요청만 여기 도달 - 다시 한번 캐시 확인 (더블 체크)
            Cache.ValueWrapper doubleCheck = cache.get(id);
            if (doubleCheck != null) {
                return (Albergue) doubleCheck.get();
            }

            return fetchAndCache(id, cache);

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("캐시 조회 중 인터럽트 발생");
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    private Albergue fetchAndCache(Long id, Cache cache) {
        try {
            Thread.sleep(50); // 실제로는 복잡한 조회/집계 쿼리라고 가정
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        Albergue albergue = albergueRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 알베르게입니다"));
        cache.put(id, albergue);
        return albergue;
    }
}