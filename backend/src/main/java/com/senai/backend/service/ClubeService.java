package com.senai.backend.service;

import com.senai.backend.model.Clube;
import com.senai.backend.repository.ClubeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ClubeService {

    @Autowired
    private ClubeRepository clubeRepository;

    public List<Clube> listarTodos() {
        return clubeRepository.findAll();
    }

    public Clube salvar(Clube clube) {

        if (clube.getId() == null) {
            clube.setCaixa(300.00);
        }

        return clubeRepository.save(clube);
    }
}