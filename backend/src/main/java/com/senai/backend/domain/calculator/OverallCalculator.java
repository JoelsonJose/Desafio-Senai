package com.senai.backend.domain.calculator;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class OverallCalculator {

    public static int calcular(String posicao, int velocidade, int finalizacao, int passe, int drible, int defesa, int fisico) {
        BigDecimal v = BigDecimal.valueOf(velocidade);
        BigDecimal f = BigDecimal.valueOf(finalizacao);
        BigDecimal p = BigDecimal.valueOf(passe);
        BigDecimal d = BigDecimal.valueOf(drible);
        BigDecimal df = BigDecimal.valueOf(defesa);
        BigDecimal fis = BigDecimal.valueOf(fisico);
        
        BigDecimal somaProdutos;

        switch (posicao != null ? posicao.toUpperCase() : "") {
            case "GOL": 
                // GOL: 10% vel, 20% passe, 40% defesa, 30% físico (finalização e drible são 0)
                somaProdutos = v.multiply(BigDecimal.valueOf(10))
                        .add(p.multiply(BigDecimal.valueOf(20)))
                        .add(df.multiply(BigDecimal.valueOf(40)))
                        .add(fis.multiply(BigDecimal.valueOf(30)));
                break;
            case "ZAG": 
                // ZAG: 10% vel, 15% passe, 45% defesa, 30% físico
                somaProdutos = v.multiply(BigDecimal.valueOf(10))
                        .add(p.multiply(BigDecimal.valueOf(15)))
                        .add(df.multiply(BigDecimal.valueOf(45)))
                        .add(fis.multiply(BigDecimal.valueOf(30)));
                break;
            case "LAT": 
                // LAT: 25% vel, 20% passe, 15% drible, 25% defesa, 15% físico
                somaProdutos = v.multiply(BigDecimal.valueOf(25))
                        .add(p.multiply(BigDecimal.valueOf(20)))
                        .add(d.multiply(BigDecimal.valueOf(15)))
                        .add(df.multiply(BigDecimal.valueOf(25)))
                        .add(fis.multiply(BigDecimal.valueOf(15)));
                break;
            case "MEI": 
                // MEI: 10% vel, 15% finalização, 35% passe, 25% drible, 5% defesa, 10% físico
                somaProdutos = v.multiply(BigDecimal.valueOf(10))
                        .add(f.multiply(BigDecimal.valueOf(15)))
                        .add(p.multiply(BigDecimal.valueOf(35)))
                        .add(d.multiply(BigDecimal.valueOf(25)))
                        .add(df.multiply(BigDecimal.valueOf(5)))
                        .add(fis.multiply(BigDecimal.valueOf(10)));
                break;
            case "ATA": 
            default:
                // ATA: 20% vel, 40% finalização, 10% passe, 20% drible, 10% físico
                somaProdutos = v.multiply(BigDecimal.valueOf(20))
                        .add(f.multiply(BigDecimal.valueOf(40)))
                        .add(p.multiply(BigDecimal.valueOf(10)))
                        .add(d.multiply(BigDecimal.valueOf(20)))
                        .add(fis.multiply(BigDecimal.valueOf(10)));
                break;
        }

        // Divide por 100 (porque os pesos somam 100%) e arredonda para o inteiro mais próximo (HALF_UP)
        return somaProdutos.divide(BigDecimal.valueOf(100), 0, RoundingMode.HALF_UP).intValue();
    }
}