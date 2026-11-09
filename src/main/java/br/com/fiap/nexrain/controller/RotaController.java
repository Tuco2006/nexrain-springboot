package br.com.fiap.nexrain.controller;

import br.com.fiap.nexrain.dto.ResultadoRota;
import br.com.fiap.nexrain.dto.SolicitacaoRota;
import br.com.fiap.nexrain.entities.Rota;
import br.com.fiap.nexrain.services.RotaService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.sql.SQLException;
import java.util.List;

@RestController
@RequestMapping("/api/rotas")
public class RotaController {

    private final RotaService rotaService = new RotaService();

    // Selecionar (historico de rotas calculadas)
    @GetMapping
    public ResponseEntity<List<Rota>> selecionarRs() throws SQLException, ClassNotFoundException {
        return ResponseEntity.ok(rotaService.selecionar());
    }

    // Buscar por id (com o trajeto pra desenhar no mapa)
    @GetMapping("/{id}")
    public ResponseEntity<Rota> buscarPorIdRs(@PathVariable int id) throws SQLException, ClassNotFoundException {
        return ResponseEntity.ok(rotaService.buscarPorId(id));
    }

    // Calcular rota segura (desviando dos alagamentos)
    @PostMapping
    public ResponseEntity<ResultadoRota> calcularRs(@RequestBody SolicitacaoRota solicitacao) throws SQLException, ClassNotFoundException {
        ResultadoRota resultado = rotaService.calcular(solicitacao);
        URI location = URI.create("/api/rotas/" + resultado.getRota().getId());
        return ResponseEntity.created(location).body(resultado);
    }

    // Recalcular uma rota salva
    @PutMapping("/{id}")
    public ResponseEntity<ResultadoRota> recalcularRs(@PathVariable int id, @RequestBody(required = false) SolicitacaoRota solicitacao) throws SQLException, ClassNotFoundException {
        return ResponseEntity.ok(rotaService.recalcular(id, solicitacao));
    }

    // Deletar
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletarRs(@PathVariable int id) throws SQLException, ClassNotFoundException {
        rotaService.deletar(id);
        return ResponseEntity.noContent().build();
    }
}
