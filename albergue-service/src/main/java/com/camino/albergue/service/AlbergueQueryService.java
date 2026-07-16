package com.camino.albergue.service;

import com.camino.albergue.domain.Albergue;
import com.camino.albergue.domain.AlbergueRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AlbergueQueryService {

    private final AlbergueRepository albergueRepository;

    public AlbergueQueryService(AlbergueRepository albergueRepository) {
        this.albergueRepository = albergueRepository;
    }

    @Transactional(readOnly = true)
    public List<Albergue> getAllSlow() {
        List<Albergue> result = albergueRepository.findAll();
        try {
            // NOTE: 커넥션 풀 고갈 시나리오 재현을 위한 인위적 지연
            // 실제 운영 로직에서는 사용하지 않으며, 트러블슈팅 실험용으로만 남겨둠
            // 관련 문서: TROUBLESHOOTING.md #2
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return result;
    }
}