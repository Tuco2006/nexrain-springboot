package br.com.fiap.nexrain.controller;

import br.com.fiap.nexrain.api.PrevisaoClima;
import br.com.fiap.nexrain.dto.EntradaAnaliseImagem;
import br.com.fiap.nexrain.dto.LocalEncontrado;
import br.com.fiap.nexrain.dto.PainelResumo;
import br.com.fiap.nexrain.dto.ResultadoAnaliseImagem;
import br.com.fiap.nexrain.dto.RiscoRegiao;
import br.com.fiap.nexrain.services.ClimaService;
import br.com.fiap.nexrain.services.MapaService;
import br.com.fiap.nexrain.services.MonitoramentoService;
import br.com.fiap.nexrain.services.RiscoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class MonitoramentoController {

    private final MonitoramentoService monitoramentoService = new MonitoramentoService();
    private final RiscoService riscoService = new RiscoService();
    private final ClimaService climaService = new ClimaService();
    private final MapaService mapaService = new MapaService();

    // Analisar a imagem de uma camera (ou foto do cidadao) com visao computacional
    @PostMapping("/monitoramento/analises")
    public ResponseEntity<ResultadoAnaliseImagem> analisarRs(@RequestBody EntradaAnaliseImagem entrada) throws SQLException, ClassNotFoundException {
        ResultadoAnaliseImagem resultado = monitoramentoService.analisar(entrada);
        return ResponseEntity.status(resultado.getOcorrencia() != null ? 201 : 200).body(resultado);
    }

    // Risco de alagamento de cada regiao (previsao real da Open-Meteo, ou temporal simulado)
    @GetMapping("/riscos")
    public ResponseEntity<List<RiscoRegiao>> riscosRs(@RequestParam(required = false, defaultValue = "false") boolean simulacao) throws SQLException, ClassNotFoundException {
        return ResponseEntity.ok(riscoService.calcularRiscos(simulacao));
    }

    // Clima atual e previsao de chuva de um ponto do mapa
    @GetMapping("/clima")
    public ResponseEntity<PrevisaoClima> climaRs(@RequestParam double latitude, @RequestParam double longitude) {
        return ResponseEntity.ok(climaService.buscarPrevisao(latitude, longitude));
    }

    // Busca de endereco
    @GetMapping("/geocodificacao")
    public ResponseEntity<List<LocalEncontrado>> geocodificarRs(@RequestParam String endereco) {
        return ResponseEntity.ok(mapaService.geocodificar(endereco));
    }

    // Numeros do painel
    @GetMapping("/painel")
    public ResponseEntity<PainelResumo> painelRs() throws SQLException, ClassNotFoundException {
        return ResponseEntity.ok(riscoService.montarPainel());
    }

    // Verificar se a API esta no ar
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> healthRs() {
        return ResponseEntity.ok(Map.of("status", "ok", "ia", monitoramentoService.iaConfigurada() ? "openai" : "motor-local"));
    }
}
