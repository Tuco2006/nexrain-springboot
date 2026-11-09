package br.com.fiap.nexrain.services;

import br.com.fiap.nexrain.dao.AlertaDAO;
import br.com.fiap.nexrain.entities.Alerta;
import br.com.fiap.nexrain.entities.MonitorRisco;
import br.com.fiap.nexrain.entities.Ocorrencia;
import br.com.fiap.nexrain.entities.Regiao;
import br.com.fiap.nexrain.excecoes.RecursoNaoEncontradoException;
import br.com.fiap.nexrain.excecoes.RegraNegocioException;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

public class AlertaService {

    private final RegiaoService regiaoService = new RegiaoService();

    // Selecionar
    public List<Alerta> selecionar() throws SQLException, ClassNotFoundException {
        try (AlertaDAO alertaDAO = new AlertaDAO()) {
            return alertaDAO.selecionar();
        }
    }

    // Alertas que a populacao deve ver agora
    public List<Alerta> selecionarVigentes() throws SQLException, ClassNotFoundException {
        try (AlertaDAO alertaDAO = new AlertaDAO()) {
            return alertaDAO.selecionarVigentes(agora());
        }
    }

    // Buscar por id
    public Alerta buscarPorId(int id) throws SQLException, ClassNotFoundException {
        try (AlertaDAO alertaDAO = new AlertaDAO()) {
            Alerta alerta = alertaDAO.buscarPorId(id);

            if (alerta == null) {
                throw new RecursoNaoEncontradoException("Alerta " + id + " não encontrado");
            }
            return alerta;
        }
    }

    // Inserir
    public Alerta inserir(Alerta alerta) throws SQLException, ClassNotFoundException {
        validar(alerta);

        try (AlertaDAO alertaDAO = new AlertaDAO()) {
            alertaDAO.inserir(alerta);
            return alerta;
        }
    }

    // Atualizar
    public Alerta atualizar(int id, Alerta alerta) throws SQLException, ClassNotFoundException {
        validar(alerta);
        alerta.setId(id);

        try (AlertaDAO alertaDAO = new AlertaDAO()) {
            if (alertaDAO.atualizar(alerta) == 0) {
                throw new RecursoNaoEncontradoException("Alerta " + id + " não encontrado");
            }
            return alerta;
        }
    }

    // Deletar
    public void deletar(int id) throws SQLException, ClassNotFoundException {
        try (AlertaDAO alertaDAO = new AlertaDAO()) {
            if (alertaDAO.deletar(id) == 0) {
                throw new RecursoNaoEncontradoException("Alerta " + id + " não encontrado");
            }
        }
    }

    // Alerta automatico quando uma ocorrencia grave e confirmada
    public Alerta gerarParaOcorrencia(Ocorrencia ocorrencia, Regiao regiao) throws SQLException, ClassNotFoundException {
        LocalDateTime agora = agora();

        Alerta alerta = new Alerta();
        alerta.setIdRegiao(regiao.getId());
        alerta.setIdOcorrencia(ocorrencia.getId());
        alerta.setNivel(ocorrencia.getNivelRisco());
        alerta.setOrigem("OCORRENCIA");
        alerta.setTitulo(cortar("Alagamento " + ("CRITICO".equals(ocorrencia.getNivelRisco()) ? "crítico" : "") + " em " + ocorrencia.getEndereco(), 120)
                .replace("  ", " "));
        alerta.setMensagem(cortar("Água com cerca de " + ocorrencia.getNivelAguaCm() + " cm em " + ocorrencia.getEndereco() + " (" + regiao.getNome() + "). " +
                (ocorrencia.getNivelAguaCm() >= 30 ? "Via intransitável para carros de passeio. " : "") +
                "Evite a região e use a rota segura do NexRain.", 500));
        alerta.setDataEmissao(agora);
        alerta.setDataExpiracao(agora.plusHours(3));
        alerta.setAtivo(true);

        try (AlertaDAO alertaDAO = new AlertaDAO()) {
            alertaDAO.inserir(alerta);
        }
        return alerta;
    }

    // Alerta automatico gerado pela previsao do tempo (nao repete se a regiao ja tem um vigente)
    public Alerta gerarParaPrevisao(MonitorRisco monitor) throws SQLException, ClassNotFoundException {
        LocalDateTime agora = agora();

        try (AlertaDAO alertaDAO = new AlertaDAO()) {
            if (alertaDAO.existeVigente(monitor.getRegiao().getId(), "PREVISAO", agora)) {
                return null;
            }
            Alerta alerta = monitor.gerarAlertaPrevisao(agora);
            alertaDAO.inserir(alerta);
            return alerta;
        }
    }

    public void desativarPorOcorrencia(int idOcorrencia) throws SQLException, ClassNotFoundException {
        try (AlertaDAO alertaDAO = new AlertaDAO()) {
            alertaDAO.desativarPorOcorrencia(idOcorrencia);
        }
    }

    // Regra de negocios
    private void validar(Alerta alerta) throws SQLException, ClassNotFoundException {
        if (alerta == null) {
            throw new RegraNegocioException("Dados do alerta não informados");
        }
        if (vazio(alerta.getTitulo()) || alerta.getTitulo().trim().length() < 5) {
            throw new RegraNegocioException("O título do alerta deve ter pelo menos 5 caracteres");
        }
        if (alerta.getTitulo().trim().length() > 120) {
            throw new RegraNegocioException("O título do alerta deve ter no máximo 120 caracteres");
        }
        if (vazio(alerta.getMensagem()) || alerta.getMensagem().trim().length() < 10) {
            throw new RegraNegocioException("A mensagem do alerta deve ter pelo menos 10 caracteres");
        }
        if (alerta.getMensagem().trim().length() > 500) {
            throw new RegraNegocioException("A mensagem do alerta deve ter no máximo 500 caracteres");
        }

        String nivel = vazio(alerta.getNivel()) ? "" : alerta.getNivel().trim().toUpperCase().replace("MÉDIO", "MEDIO").replace("CRÍTICO", "CRITICO");
        if (!MonitorRisco.nivelValido(nivel)) {
            throw new RegraNegocioException("O nível do alerta deve ser BAIXO, MEDIO, ALTO ou CRITICO");
        }

        String origem = vazio(alerta.getOrigem()) ? "MANUAL" : alerta.getOrigem().trim().toUpperCase();
        if (!origem.equals("MANUAL") && !origem.equals("OCORRENCIA") && !origem.equals("PREVISAO")) {
            throw new RegraNegocioException("A origem do alerta deve ser MANUAL, OCORRENCIA ou PREVISAO");
        }

        if (alerta.getDataEmissao() == null) {
            alerta.setDataEmissao(agora());
        }
        if (alerta.getDataExpiracao() == null) {
            alerta.setDataExpiracao(alerta.getDataEmissao().plusHours(6));
        }
        if (!alerta.getDataExpiracao().isAfter(alerta.getDataEmissao())) {
            throw new RegraNegocioException("A data de expiração deve ser depois da data de emissão");
        }

        regiaoService.buscarPorId(alerta.getIdRegiao());

        alerta.setTitulo(alerta.getTitulo().trim());
        alerta.setMensagem(alerta.getMensagem().trim());
        alerta.setNivel(nivel);
        alerta.setOrigem(origem);
    }

    private LocalDateTime agora() {
        return LocalDateTime.now(Ocorrencia.FUSO).withNano(0);
    }

    private String cortar(String texto, int tamanho) {
        return texto.length() > tamanho ? texto.substring(0, tamanho - 3) + "..." : texto;
    }

    private boolean vazio(String valor) {
        return valor == null || valor.isBlank();
    }
}
