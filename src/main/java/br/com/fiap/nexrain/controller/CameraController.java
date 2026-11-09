package br.com.fiap.nexrain.controller;

import br.com.fiap.nexrain.entities.Camera;
import br.com.fiap.nexrain.services.CameraService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.sql.SQLException;
import java.util.List;

@RestController
@RequestMapping("/api/cameras")
public class CameraController {

    private final CameraService cameraService = new CameraService();

    // Selecionar
    @GetMapping
    public ResponseEntity<List<Camera>> selecionarRs() throws SQLException, ClassNotFoundException {
        return ResponseEntity.ok(cameraService.selecionar());
    }

    // Buscar por id
    @GetMapping("/{id}")
    public ResponseEntity<Camera> buscarPorIdRs(@PathVariable int id) throws SQLException, ClassNotFoundException {
        return ResponseEntity.ok(cameraService.buscarPorId(id));
    }

    // Inserir
    @PostMapping
    public ResponseEntity<Camera> inserirRs(@RequestBody Camera camera) throws SQLException, ClassNotFoundException {
        cameraService.inserir(camera);
        URI location = URI.create("/api/cameras/" + camera.getId());
        return ResponseEntity.created(location).body(camera);
    }

    // Atualizar
    @PutMapping("/{id}")
    public ResponseEntity<Camera> atualizarRs(@PathVariable int id, @RequestBody Camera camera) throws SQLException, ClassNotFoundException {
        return ResponseEntity.ok(cameraService.atualizar(id, camera));
    }

    // Deletar
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletarRs(@PathVariable int id) throws SQLException, ClassNotFoundException {
        cameraService.deletar(id);
        return ResponseEntity.noContent().build();
    }
}
