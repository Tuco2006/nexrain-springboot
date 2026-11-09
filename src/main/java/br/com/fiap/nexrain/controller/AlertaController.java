package br.com.fiap.nexrain.controller;

import br.com.fiap.nexrain.entities.Alerta;
import br.com.fiap.nexrain.services.AlertaService;
import br.com.fiap.nexrain.services.RiscoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.sql.SQLException;
import java.util.List;

@RestController
@RequestMapping("/api/alertas")
public class AlertaController {

    private final AlertaService alertaService = new AlertaService();
    private final RiscoService riscoService = new RiscoService();

    // Selecionar (?vigentes=true traz so os que a populacao deve ver agora)
    @GetMapping
    public ResponseEntity<List<Alerta>> selecionarRs(@RequestParam(required = false, defaultValue = "false") boolean vigentes) throws SQLException, ClassNotFoundException {
        return ResponseEntity.ok(vigentes ? alertaService.selecionarVigentes() : alertaService.selecionar());
    }

    // Buscar por id
    @GetMapping("/{id}")
    public ResponseEntity<Alerta> buscarPorIdRs(@PathVariable int id) throws SQLException, ClassNotFoundException {
        return ResponseEntity.ok(alertaService.buscarPorId(id));
    }

    // Inserir (alerta manual do operador)
    @PostMapping
    public ResponseEntity<Alerta> inserirRs(@RequestBody Alerta alerta) throws SQLException, ClassNotFoundException {
        alertaService.inserir(alerta);
        URI location = URI.create("/api/alertas/" + alerta.getId());
        return ResponseEntity.created(location).body(alerta);
    }

    // Gerar alertas automaticos a partir da previsao do tempo
    @PostMapping("/previsao")
    public ResponseEntity<List<Alerta>> gerarPorPrevisaoRs(@RequestParam(required = false, defaultValue = "false") boolean simulacao) throws SQLException, ClassNotFoundException {
        List<Alerta> gerados = riscoService.gerarAlertasPrevisao(simulacao);
        return ResponseEntity.status(gerados.isEmpty() ? 200 : 201).body(gerados);
    }

    // Atualizar
    @PutMapping("/{id}")
    public ResponseEntity<Alerta> atualizarRs(@PathVariable int id, @RequestBody Alerta alerta) throws SQLException, ClassNotFoundException {
        return ResponseEntity.ok(alertaService.atualizar(id, alerta));
    }

    // Deletar
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletarRs(@PathVariable int id) throws SQLException, ClassNotFoundException {
        alertaService.deletar(id);
        return ResponseEntity.noContent().build();
    }
}
