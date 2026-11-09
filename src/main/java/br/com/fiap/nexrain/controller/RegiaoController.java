package br.com.fiap.nexrain.controller;

import br.com.fiap.nexrain.entities.Camera;
import br.com.fiap.nexrain.entities.Ocorrencia;
import br.com.fiap.nexrain.entities.Regiao;
import br.com.fiap.nexrain.services.CameraService;
import br.com.fiap.nexrain.services.OcorrenciaService;
import br.com.fiap.nexrain.services.RegiaoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.sql.SQLException;
import java.util.List;

@RestController
@RequestMapping("/api/regioes")
public class RegiaoController {

    private final RegiaoService regiaoService = new RegiaoService();
    private final CameraService cameraService = new CameraService();
    private final OcorrenciaService ocorrenciaService = new OcorrenciaService();

    // Selecionar
    @GetMapping
    public ResponseEntity<List<Regiao>> selecionarRs() throws SQLException, ClassNotFoundException {
        return ResponseEntity.ok(regiaoService.selecionar());
    }

    // Buscar por id
    @GetMapping("/{id}")
    public ResponseEntity<Regiao> buscarPorIdRs(@PathVariable int id) throws SQLException, ClassNotFoundException {
        return ResponseEntity.ok(regiaoService.buscarPorId(id));
    }

    @GetMapping("/{id}/cameras")
    public ResponseEntity<List<Camera>> selecionarCamerasRs(@PathVariable int id) throws SQLException, ClassNotFoundException {
        return ResponseEntity.ok(cameraService.selecionarPorRegiao(id));
    }

    @GetMapping("/{id}/ocorrencias")
    public ResponseEntity<List<Ocorrencia>> selecionarOcorrenciasRs(@PathVariable int id) throws SQLException, ClassNotFoundException {
        return ResponseEntity.ok(ocorrenciaService.selecionarPorRegiao(id));
    }

    // Inserir
    @PostMapping
    public ResponseEntity<Regiao> inserirRs(@RequestBody Regiao regiao) throws SQLException, ClassNotFoundException {
        regiaoService.inserir(regiao);
        URI location = URI.create("/api/regioes/" + regiao.getId());
        return ResponseEntity.created(location).body(regiao);
    }

    // Atualizar
    @PutMapping("/{id}")
    public ResponseEntity<Regiao> atualizarRs(@PathVariable int id, @RequestBody Regiao regiao) throws SQLException, ClassNotFoundException {
        return ResponseEntity.ok(regiaoService.atualizar(id, regiao));
    }

    // Deletar
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletarRs(@PathVariable int id) throws SQLException, ClassNotFoundException {
        regiaoService.deletar(id);
        return ResponseEntity.noContent().build();
    }
}
