package com.senai.backend.controller;

import com.senai.backend.model.Clube;
import com.senai.backend.service.ClubeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/clubes")
public class ClubeController {

    @Autowired
    private ClubeService clubeService;

    @GetMapping
    public ResponseEntity<List<Clube>> listar() {
        List<Clube> clubes = clubeService.listarTodos();
        return ResponseEntity.ok(clubes);
    }

    @PostMapping
    public ResponseEntity<Clube> criar(@RequestBody Clube clube) {
        Clube novoClube = clubeService.salvar(clube);
        return ResponseEntity.status(HttpStatus.CREATED).body(novoClube);
    }
}