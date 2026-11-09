package br.com.fiap.nexrain.controller;

import br.com.fiap.nexrain.dto.ValidacaoOcorrencia;
import br.com.fiap.nexrain.entities.Ocorrencia;
import br.com.fiap.nexrain.services.OcorrenciaService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.sql.SQLException;
import java.util.List;

@RestController
@RequestMapping("/api/ocorrencias")
public class OcorrenciaController {

    private final OcorrenciaService ocorrenciaService = new OcorrenciaService();

    // Selecionar (filtros opcionais: ?status=PENDENTE ou ?ativas=true)
    @GetMapping
    public ResponseEntity<List<Ocorrencia>> selecionarRs(@RequestParam(required = false) String status,
                                                         @RequestParam(required = false, defaultValue = "false") boolean ativas) throws SQLException, ClassNotFoundException {
        if (ativas) {
            return ResponseEntity.ok(ocorrenciaService.selecionarAtivas());
        }
        if (status != null && !status.isBlank()) {
            return ResponseEntity.ok(ocorrenciaService.selecionarPorStatus(status));
        }
        return ResponseEntity.ok(ocorrenciaService.selecionar());
    }

    // Buscar por id
    @GetMapping("/{id}")
    public ResponseEntity<Ocorrencia> buscarPorIdRs(@PathVariable int id) throws SQLException, ClassNotFoundException {
        return ResponseEntity.ok(ocorrenciaService.buscarPorId(id));
    }

    // Inserir (relato de alagamento)
    @PostMapping
    public ResponseEntity<Ocorrencia> inserirRs(@RequestBody Ocorrencia ocorrencia) throws SQLException, ClassNotFoundException {
        ocorrenciaService.inserir(ocorrencia);
        URI location = URI.create("/api/ocorrencias/" + ocorrencia.getId());
        return ResponseEntity.created(location).body(ocorrencia);
    }

    // Atualizar
    @PutMapping("/{id}")
    public ResponseEntity<Ocorrencia> atualizarRs(@PathVariable int id, @RequestBody Ocorrencia ocorrencia) throws SQLException, ClassNotFoundException {
        return ResponseEntity.ok(ocorrenciaService.atualizar(id, ocorrencia));
    }

    // Validar (CONFIRMAR, DESCARTAR ou RESOLVER)
    @PutMapping("/{id}/validacao")
    public ResponseEntity<Ocorrencia> validarRs(@PathVariable int id, @RequestBody ValidacaoOcorrencia validacao) throws SQLException, ClassNotFoundException {
        return ResponseEntity.ok(ocorrenciaService.validar(id, validacao));
    }

    // Deletar
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletarRs(@PathVariable int id) throws SQLException, ClassNotFoundException {
        ocorrenciaService.deletar(id);
        return ResponseEntity.noContent().build();
    }
}
