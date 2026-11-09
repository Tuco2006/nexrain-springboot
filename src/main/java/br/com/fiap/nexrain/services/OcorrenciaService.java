package br.com.fiap.nexrain.services;

import br.com.fiap.nexrain.dao.OcorrenciaDAO;
import br.com.fiap.nexrain.dto.ValidacaoOcorrencia;
import br.com.fiap.nexrain.entities.MonitorRisco;
import br.com.fiap.nexrain.entities.Ocorrencia;
import br.com.fiap.nexrain.entities.Regiao;
import br.com.fiap.nexrain.excecoes.RecursoNaoEncontradoException;
import br.com.fiap.nexrain.excecoes.RegraNegocioException;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

public class OcorrenciaService {

    private static final String[] STATUS = {"PENDENTE", "CONFIRMADA", "DESCARTADA", "RESOLVIDA"};
    private static final String[] ORIGENS = {"IA_CAMERA", "CIDADAO", "OPERADOR"};

    private final RegiaoService regiaoService = new RegiaoService();
    private final CameraService cameraService = new CameraService();
    private final AlertaService alertaService = new AlertaService();

    // Selecionar
    public List<Ocorrencia> selecionar() throws SQLException, ClassNotFoundException {
        try (OcorrenciaDAO ocorrenciaDAO = new OcorrenciaDAO()) {
            return ocorrenciaDAO.selecionar();
        }
    }

    public List<Ocorrencia> selecionarPorStatus(String status) throws SQLException, ClassNotFoundException {
        String valor = status.trim().toUpperCase();
        if (!contem(STATUS, valor)) {
            throw new RegraNegocioException("Status inválido. Use PENDENTE, CONFIRMADA, DESCARTADA ou RESOLVIDA");
        }

        try (OcorrenciaDAO ocorrenciaDAO = new OcorrenciaDAO()) {
            return ocorrenciaDAO.selecionarPorStatus(valor);
        }
    }

    public List<Ocorrencia> selecionarAtivas() throws SQLException, ClassNotFoundException {
        try (OcorrenciaDAO ocorrenciaDAO = new OcorrenciaDAO()) {
            return ocorrenciaDAO.selecionarAtivas();
        }
    }

    public List<Ocorrencia> selecionarPorRegiao(int idRegiao) throws SQLException, ClassNotFoundException {
        regiaoService.buscarPorId(idRegiao);

        try (OcorrenciaDAO ocorrenciaDAO = new OcorrenciaDAO()) {
            return ocorrenciaDAO.selecionarPorRegiao(idRegiao);
        }
    }

    // Buscar por id
    public Ocorrencia buscarPorId(int id) throws SQLException, ClassNotFoundException {
        try (OcorrenciaDAO ocorrenciaDAO = new OcorrenciaDAO()) {
            Ocorrencia ocorrencia = ocorrenciaDAO.buscarPorId(id);

            if (ocorrencia == null) {
                throw new RecursoNaoEncontradoException("Ocorrência " + id + " não encontrada");
            }
            return ocorrencia;
        }
    }

    public Ocorrencia inserir(Ocorrencia ocorrencia) throws SQLException, ClassNotFoundException {
        validar(ocorrencia);

        try (OcorrenciaDAO ocorrenciaDAO = new OcorrenciaDAO()) {
            ocorrenciaDAO.inserir(ocorrencia);
            return ocorrencia;
        }
    }

    // Atualizar
    public Ocorrencia atualizar(int id, Ocorrencia ocorrencia) throws SQLException, ClassNotFoundException {
        validar(ocorrencia);
        ocorrencia.setId(id);

        try (OcorrenciaDAO ocorrenciaDAO = new OcorrenciaDAO()) {
            if (ocorrenciaDAO.atualizar(ocorrencia) == 0) {
                throw new RecursoNaoEncontradoException("Ocorrência " + id + " não encontrada");
            }
        }

        if (!ocorrencia.ativa()) {
            alertaService.desativarPorOcorrencia(id);
        }
        return ocorrencia;
    }

    // Deletar
    public void deletar(int id) throws SQLException, ClassNotFoundException {
        try (OcorrenciaDAO ocorrenciaDAO = new OcorrenciaDAO()) {
            if (ocorrenciaDAO.deletar(id) == 0) {
                throw new RecursoNaoEncontradoException("Ocorrência " + id + " não encontrada");
            }
        }
    }

    public Ocorrencia validar(int id, ValidacaoOcorrencia validacao) throws SQLException, ClassNotFoundException {
        if (validacao == null || validacao.getAcao() == null) {
            throw new RegraNegocioException("Informe a ação: CONFIRMAR, DESCARTAR ou RESOLVER");
        }

        Ocorrencia ocorrencia = buscarPorId(id);

        switch (validacao.getAcao().trim().toUpperCase()) {
            case "CONFIRMAR":
                ocorrencia.confirmar(validacao.getResponsavel());
                break;
            case "DESCARTAR":
                ocorrencia.descartar(validacao.getResponsavel());
                break;
            case "RESOLVER":
                ocorrencia.resolver(validacao.getResponsavel());
                break;
            default:
                throw new RegraNegocioException("Ação inválida. Use CONFIRMAR, DESCARTAR ou RESOLVER");
        }

        try (OcorrenciaDAO ocorrenciaDAO = new OcorrenciaDAO()) {
            ocorrenciaDAO.atualizar(ocorrencia);
        }

        if ("CONFIRMADA".equals(ocorrencia.getStatus()) && ocorrencia.exigeAlerta()) {
            alertaService.gerarParaOcorrencia(ocorrencia, regiaoService.buscarPorId(ocorrencia.getIdRegiao()));
        } else if (!ocorrencia.ativa()) {
            alertaService.desativarPorOcorrencia(ocorrencia.getId());
        }
        return ocorrencia;
    }

    public Ocorrencia registrarDeteccao(Ocorrencia deteccao) throws SQLException, ClassNotFoundException {
        if (deteccao.getIdCamera() != null) {
            for (Ocorrencia existente : selecionarAtivas()) {
                if (deteccao.getIdCamera().equals(existente.getIdCamera())) {
                    existente.setNivelAguaCm(deteccao.getNivelAguaCm());
                    existente.setNivelRisco(deteccao.getNivelRisco());
                    existente.setConfianca(deteccao.getConfianca());
                    existente.setDescricao(deteccao.getDescricao());
                    existente.setDataHora(deteccao.getDataHora());
                    try (OcorrenciaDAO ocorrenciaDAO = new OcorrenciaDAO()) {
                        ocorrenciaDAO.atualizar(existente);
                    }
                    return existente;
                }
            }
        }
        return inserir(deteccao);
    }

    // Regra de negocios
    private void validar(Ocorrencia ocorrencia) throws SQLException, ClassNotFoundException {
        if (ocorrencia == null) {
            throw new RegraNegocioException("Dados da ocorrência não informados");
        }
        if (!ocorrencia.coordenada().valida()) {
            throw new RegraNegocioException("Informe a latitude e a longitude do alagamento");
        }
        if (vazio(ocorrencia.getEndereco())) {
            throw new RegraNegocioException("Informe o endereço do alagamento");
        }
        if (ocorrencia.getEndereco().trim().length() > 150) {
            throw new RegraNegocioException("O endereço deve ter no máximo 150 caracteres");
        }
        if (ocorrencia.getNivelAguaCm() < 0 || ocorrencia.getNivelAguaCm() > 300) {
            throw new RegraNegocioException("O nível da água deve ficar entre 0 e 300 cm");
        }
        if (ocorrencia.getConfianca() < 0 || ocorrencia.getConfianca() > 100) {
            throw new RegraNegocioException("A confiança deve ficar entre 0 e 100");
        }
        if (ocorrencia.getDescricao() != null && ocorrencia.getDescricao().length() > 400) {
            throw new RegraNegocioException("A descrição deve ter no máximo 400 caracteres");
        }

        String nivel = vazio(ocorrencia.getNivelRisco())
                ? MonitorRisco.classificarPorNivelAgua(ocorrencia.getNivelAguaCm())
                : ocorrencia.getNivelRisco().trim().toUpperCase().replace("MÉDIO", "MEDIO").replace("CRÍTICO", "CRITICO");
        if (!MonitorRisco.nivelValido(nivel)) {
            throw new RegraNegocioException("O nível de risco deve ser BAIXO, MEDIO, ALTO ou CRITICO");
        }

        String origem = vazio(ocorrencia.getOrigem()) ? "CIDADAO" : ocorrencia.getOrigem().trim().toUpperCase();
        if (!contem(ORIGENS, origem)) {
            throw new RegraNegocioException("A origem deve ser IA_CAMERA, CIDADAO ou OPERADOR");
        }

        String status = vazio(ocorrencia.getStatus()) ? "PENDENTE" : ocorrencia.getStatus().trim().toUpperCase();
        if (!contem(STATUS, status)) {
            throw new RegraNegocioException("O status deve ser PENDENTE, CONFIRMADA, DESCARTADA ou RESOLVIDA");
        }

        if (ocorrencia.getIdCamera() != null) {
            cameraService.buscarPorId(ocorrencia.getIdCamera());
        }

        if (ocorrencia.getIdRegiao() <= 0) {
            Regiao regiao = regiaoService.regiaoMaisProxima(ocorrencia.coordenada());
            ocorrencia.setIdRegiao(regiao.getId());
        } else {
            regiaoService.buscarPorId(ocorrencia.getIdRegiao());
        }

        if (ocorrencia.getDataHora() == null) {
            ocorrencia.setDataHora(LocalDateTime.now(Ocorrencia.FUSO).withNano(0));
        }

        ocorrencia.setEndereco(ocorrencia.getEndereco().trim());
        ocorrencia.setNivelRisco(nivel);
        ocorrencia.setOrigem(origem);
        ocorrencia.setStatus(status);
    }

    private boolean contem(String[] lista, String valor) {
        for (String item : lista) {
            if (item.equals(valor)) {
                return true;
            }
        }
        return false;
    }

    private boolean vazio(String valor) {
        return valor == null || valor.isBlank();
    }
}
