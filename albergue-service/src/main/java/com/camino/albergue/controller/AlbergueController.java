package com.camino.albergue.controller;

import com.camino.albergue.domain.Albergue;
import com.camino.albergue.domain.AlbergueRepository;
import com.camino.albergue.service.AlbergueQueryService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class AlbergueController {

    private final AlbergueRepository albergueRepository;
    private final AlbergueQueryService albergueQueryService;

    public AlbergueController(AlbergueRepository albergueRepository, AlbergueQueryService albergueQueryService) {
        this.albergueRepository = albergueRepository;
        this.albergueQueryService = albergueQueryService;
    }

    @GetMapping("/api/albergues")
    public List<Albergue> getAll() {
        return albergueQueryService.getAllSlow();
    }
}