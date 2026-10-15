package com.senai.backend.domain.calculator;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class MarketValueCalculator {

    // RN06: Cálculo do Valor Base no cadastro (faixas progressivas + jovem promessa + piso)
    public static BigDecimal calcularValorBase(int overall, int potencial, int idade) {
        BigDecimal parteOverall = BigDecimal.ZERO;

        if (overall > 40 && overall <= 60) {
            parteOverall = BigDecimal.valueOf(overall - 40).multiply(BigDecimal.valueOf(0.50));
        } else if (overall > 60 && overall <= 75) {
            parteOverall = BigDecimal.valueOf(20).multiply(BigDecimal.valueOf(0.50))
                    .add(BigDecimal.valueOf(overall - 60).multiply(BigDecimal.valueOf(2.00)));
        } else if (overall > 75) {
            parteOverall = BigDecimal.valueOf(20).multiply(BigDecimal.valueOf(0.50))
                    .add(BigDecimal.valueOf(15).multiply(BigDecimal.valueOf(2.00)))
                    .add(BigDecimal.valueOf(overall - 75).multiply(BigDecimal.valueOf(6.00)));
        }

        // Jovem promessa (até 21 anos no cadastro)
        BigDecimal valorBase = parteOverall;
        if (idade <= 21) {
            BigDecimal fatorJovem = BigDecimal.valueOf(100)
                    .add(BigDecimal.valueOf(potencial))
                    .subtract(BigDecimal.valueOf(overall))
                    .divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP);
            valorBase = valorBase.multiply(fatorJovem);
        }

        // Arredondamento único para 2 casas e piso de 1.00
        valorBase = valorBase.setScale(2, RoundingMode.HALF_UP);
        if (valorBase.compareTo(BigDecimal.ONE) < 0) {
            return BigDecimal.ONE.setScale(2, RoundingMode.HALF_UP);
        }
        return valorBase;
    }

    // RN02 a RN05: Cálculo do Valor de Mercado atual
    public static BigDecimal calcularValorMercado(BigDecimal valorBase, String condicao, int idade, int agilidade) {
        // 1. Desconto por condição (RN02)
        BigDecimal descontoCondicao = BigDecimal.ZERO;
        if (condicao != null) {
            switch (condicao.toUpperCase()) {
                case "MAL_CONDICIONADO":
                    descontoCondicao = BigDecimal.valueOf(15);
                    break;
                case "DOENTE":
                    descontoCondicao = BigDecimal.valueOf(30);
                    break;
                case "APTO":
                default:
                    descontoCondicao = BigDecimal.ZERO;
                    break;
            }
        }

        // 2. Efeito da agilidade no desconto (RN03)
        if (descontoCondicao.compareTo(BigDecimal.ZERO) > 0) {
            if (agilidade >= 80 && agilidade <= 100) {
                descontoCondicao = descontoCondicao.divide(BigDecimal.valueOf(2), 4, RoundingMode.HALF_UP);
            } else if (agilidade >= 1 && agilidade <= 30) {
                descontoCondicao = descontoCondicao.add(BigDecimal.valueOf(10));
            }
        }

        // 3. Ajuste por idade (RN04)
        BigDecimal ajusteIdade = BigDecimal.ZERO;
        if (idade <= 21) {
            ajusteIdade = BigDecimal.valueOf(10); // Jovem promessa +10%
        } else if (idade >= 32) {
            ajusteIdade = BigDecimal.valueOf(-10); // 32 anos ou mais -10%
        }

        // 4. Percentual total e teto de desconto (RN05)
        BigDecimal percentualTotal = ajusteIdade.subtract(descontoCondicao);
        if (percentualTotal.compareTo(BigDecimal.valueOf(-40)) < 0) {
            percentualTotal = BigDecimal.valueOf(-40); // Teto de -40%
        }

        // 5. Cálculo do valor de mercado = valorBase * (100 + percentualTotal) / 100
        BigDecimal fator = BigDecimal.valueOf(100).add(percentualTotal).divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP);
        BigDecimal valorMercado = valorBase.multiply(fator).setScale(2, RoundingMode.HALF_UP);

        // 6. Piso de 1.00 (RN05)
        if (valorMercado.compareTo(BigDecimal.ONE) < 0) {
            return BigDecimal.ONE.setScale(2, RoundingMode.HALF_UP);
        }

        return valorMercado;
    }
}