package com.senai.backend.domain.calculator;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.math.BigDecimal;
import java.math.RoundingMode;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CalculatorTest {

    @ParameterizedTest(name = "Overall para {0} com atributos ({1}, {2}, {3}, {4}, {5}, {6}) deve ser {7}")
    @CsvSource({
            "MEI, 68, 65, 80, 78, 52, 60, 73",
            "ATA, 78, 75, 73, 82, 48, 68, 76",
            "LAT, 72, 70, 78, 76, 70, 66, 72",
            "ZAG, 50, 90, 60, 40, 80, 75, 73",
            "GOL, 55, 30, 65, 40, 72, 68, 68"
    })
    @DisplayName("Teste parametrizado de Overall (RN06)")
    void testOverallCalculator(String posicao, int vel, int fin, int pas, int drib, int def, int fis, int esperado) {
        int resultado = OverallCalculator.calcular(posicao, vel, fin, pas, drib, def, fis);
        assertEquals(esperado, resultado);
    }

    @ParameterizedTest(name = "Valor Base para Overall {0}, Potencial {1}, Idade {2} deve ser {3}")
    @CsvSource({
            "75, 86, 25, 40.00",
            "60, 70, 30, 10.00",
            "61, 70, 30, 12.00",
            "76, 80, 22, 46.00",
            "85, 92, 27, 100.00",
            "70, 84, 21, 34.20",
            "70, 84, 22, 30.00",
            "45, 90, 18, 3.63",
            "40, 66, 36, 1.00"
    })
    @DisplayName("Teste parametrizado de Valor Base (RN06)")
    void testValorBaseCalculator(int overall, int potencial, int idade, String esperado) {
        BigDecimal resultado = MarketValueCalculator.calcularValorBase(overall, potencial, idade);
        BigDecimal esperadoBd = new BigDecimal(esperado).setScale(2, RoundingMode.HALF_UP);
        assertEquals(esperadoBd, resultado);
    }

    @ParameterizedTest(name = "Valor de Mercado para Base {0}, Condição {1}, Agilidade {2}, Idade {3} deve ser {4}")
    @CsvSource({
            "82.00, APTO, 20, 25, 82.00",
            "82.00, APTO, 50, 19, 90.20",
            "82.00, MAL_CONDICIONADO, 50, 25, 69.70",
            "82.00, MAL_CONDICIONADO, 80, 21, 84.05",
            "82.00, MAL_CONDICIONADO, 30, 33, 53.30",
            "82.00, DOENTE, 90, 25, 69.70",
            "82.00, DOENTE, 30, 25, 49.20",
            "82.00, DOENTE, 10, 40, 49.20",
            "9.50, MAL_CONDICIONADO, 85, 25, 8.79",
            "1.00, MAL_CONDICIONADO, 50, 35, 1.00"
    })
    @DisplayName("Teste parametrizado de Valor de Mercado (RN02 a RN05)")
    void testValorMercadoCalculator(String valorBase, String condicao, int agilidade, int idade, String esperado) {
        BigDecimal base = new BigDecimal(valorBase);
        BigDecimal resultado = MarketValueCalculator.calcularValorMercado(base, condicao, idade, agilidade);
        BigDecimal esperadoBd = new BigDecimal(esperado).setScale(2, RoundingMode.HALF_UP);
        assertEquals(esperadoBd, resultado);
    }
}