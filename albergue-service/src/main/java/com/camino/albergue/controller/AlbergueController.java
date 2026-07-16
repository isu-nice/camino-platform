package com.camino.albergue.controller;

import com.camino.albergue.domain.Albergue;
import com.camino.albergue.domain.AlbergueRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class AlbergueController {

    private final AlbergueRepository albergueRepository;

    public AlbergueController(AlbergueRepository albergueRepository) {
        this.albergueRepository = albergueRepository;
    }

    @GetMapping("/api/albergues")
    public List<Albergue> getAll() {
        return albergueRepository.findAll();
    }
}