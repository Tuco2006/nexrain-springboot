package br.com.fiap.nexrain.services;

import br.com.fiap.nexrain.dao.CameraDAO;
import br.com.fiap.nexrain.entities.Camera;
import br.com.fiap.nexrain.entities.Coordenada;
import br.com.fiap.nexrain.excecoes.RecursoNaoEncontradoException;
import br.com.fiap.nexrain.excecoes.RegraNegocioException;

import java.sql.SQLException;
import java.util.List;

public class CameraService {

    private final RegiaoService regiaoService = new RegiaoService();

    // Selecionar
    public List<Camera> selecionar() throws SQLException, ClassNotFoundException {
        try (CameraDAO cameraDAO = new CameraDAO()) {
            return cameraDAO.selecionar();
        }
    }

    // Cameras de uma regiao
    public List<Camera> selecionarPorRegiao(int idRegiao) throws SQLException, ClassNotFoundException {
        regiaoService.buscarPorId(idRegiao);

        try (CameraDAO cameraDAO = new CameraDAO()) {
            return cameraDAO.selecionarPorRegiao(idRegiao);
        }
    }

    // Buscar por id
    public Camera buscarPorId(int id) throws SQLException, ClassNotFoundException {
        try (CameraDAO cameraDAO = new CameraDAO()) {
            Camera camera = cameraDAO.buscarPorId(id);

            if (camera == null) {
                throw new RecursoNaoEncontradoException("Câmera " + id + " não encontrada");
            }
            return camera;
        }
    }

    // Inserir
    public Camera inserir(Camera camera) throws SQLException, ClassNotFoundException {
        validar(camera);

        try (CameraDAO cameraDAO = new CameraDAO()) {
            cameraDAO.inserir(camera);
            return camera;
        }
    }

    // Atualizar
    public Camera atualizar(int id, Camera camera) throws SQLException, ClassNotFoundException {
        validar(camera);
        camera.setId(id);

        try (CameraDAO cameraDAO = new CameraDAO()) {
            if (cameraDAO.atualizar(camera) == 0) {
                throw new RecursoNaoEncontradoException("Câmera " + id + " não encontrada");
            }
            return camera;
        }
    }

    // Deletar
    public void deletar(int id) throws SQLException, ClassNotFoundException {
        try (CameraDAO cameraDAO = new CameraDAO()) {
            if (cameraDAO.deletar(id) == 0) {
                throw new RecursoNaoEncontradoException("Câmera " + id + " não encontrada");
            }
        }
    }

    // Regra de negocios
    private void validar(Camera camera) throws SQLException, ClassNotFoundException {
        if (camera == null) {
            throw new RegraNegocioException("Dados da câmera não informados");
        }
        if (vazio(camera.getNome()) || camera.getNome().trim().length() < 3) {
            throw new RegraNegocioException("O nome da câmera deve ter pelo menos 3 caracteres");
        }
        if (camera.getNome().trim().length() > 80) {
            throw new RegraNegocioException("O nome da câmera deve ter no máximo 80 caracteres");
        }
        if (vazio(camera.getEndereco())) {
            throw new RegraNegocioException("Informe o endereço da câmera");
        }
        if (camera.getEndereco().trim().length() > 150) {
            throw new RegraNegocioException("O endereço deve ter no máximo 150 caracteres");
        }
        if (!new Coordenada(camera.getLatitude(), camera.getLongitude()).valida()) {
            throw new RegraNegocioException("Latitude e longitude da câmera são inválidas");
        }
        if (!vazio(camera.getUrlImagem())) {
            if (!camera.getUrlImagem().trim().startsWith("http")) {
                throw new RegraNegocioException("A URL da imagem deve começar com http ou https");
            }
            if (camera.getUrlImagem().trim().length() > 400) {
                throw new RegraNegocioException("A URL da imagem deve ter no máximo 400 caracteres");
            }
        }

        String status = vazio(camera.getStatus()) ? "ATIVA" : camera.getStatus().trim().toUpperCase();
        if (!status.equals("ATIVA") && !status.equals("INATIVA") && !status.equals("MANUTENCAO")) {
            throw new RegraNegocioException("O status da câmera deve ser ATIVA, INATIVA ou MANUTENCAO");
        }

        // a regiao precisa existir
        regiaoService.buscarPorId(camera.getIdRegiao());

        camera.setNome(camera.getNome().trim());
        camera.setEndereco(camera.getEndereco().trim());
        camera.setUrlImagem(vazio(camera.getUrlImagem()) ? null : camera.getUrlImagem().trim());
        camera.setStatus(status);
    }

    private boolean vazio(String valor) {
        return valor == null || valor.isBlank();
    }
}
