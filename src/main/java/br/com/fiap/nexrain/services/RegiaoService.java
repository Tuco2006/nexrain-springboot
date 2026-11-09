package br.com.fiap.nexrain.services;

import br.com.fiap.nexrain.dao.RegiaoDAO;
import br.com.fiap.nexrain.entities.Coordenada;
import br.com.fiap.nexrain.entities.Regiao;
import br.com.fiap.nexrain.excecoes.RecursoNaoEncontradoException;
import br.com.fiap.nexrain.excecoes.RegraNegocioException;

import java.sql.SQLException;
import java.text.Normalizer;
import java.util.List;

public class RegiaoService {

    // Selecionar
    public List<Regiao> selecionar() throws SQLException, ClassNotFoundException {
        try (RegiaoDAO regiaoDAO = new RegiaoDAO()) {
            return regiaoDAO.selecionar();
        }
    }

    // Buscar por id
    public Regiao buscarPorId(int id) throws SQLException, ClassNotFoundException {
        try (RegiaoDAO regiaoDAO = new RegiaoDAO()) {
            Regiao regiao = regiaoDAO.buscarPorId(id);

            if (regiao == null) {
                throw new RecursoNaoEncontradoException("Região " + id + " não encontrada");
            }
            return regiao;
        }
    }

    // Inserir
    public Regiao inserir(Regiao regiao) throws SQLException, ClassNotFoundException {
        validar(regiao);

        try (RegiaoDAO regiaoDAO = new RegiaoDAO()) {
            regiaoDAO.inserir(regiao);
            return regiao;
        }
    }

    // Atualizar
    public Regiao atualizar(int id, Regiao regiao) throws SQLException, ClassNotFoundException {
        validar(regiao);
        regiao.setId(id);

        try (RegiaoDAO regiaoDAO = new RegiaoDAO()) {
            if (regiaoDAO.atualizar(regiao) == 0) {
                throw new RecursoNaoEncontradoException("Região " + id + " não encontrada");
            }
            return regiao;
        }
    }

    // Deletar
    public void deletar(int id) throws SQLException, ClassNotFoundException {
        try (RegiaoDAO regiaoDAO = new RegiaoDAO()) {
            if (regiaoDAO.deletar(id) == 0) {
                throw new RecursoNaoEncontradoException("Região " + id + " não encontrada");
            }
        }
    }

    public Regiao regiaoMaisProxima(Coordenada ponto) throws SQLException, ClassNotFoundException {
        Regiao maisProxima = null;
        double menorDistancia = Double.MAX_VALUE;

        for (Regiao regiao : selecionar()) {
            double distancia = regiao.coordenada().distanciaMetros(ponto);
            if (distancia < menorDistancia) {
                menorDistancia = distancia;
                maisProxima = regiao;
            }
        }

        if (maisProxima == null) {
            throw new RegraNegocioException("Nenhuma região cadastrada para vincular a ocorrência");
        }
        return maisProxima;
    }

    // Regra de negocios
    private void validar(Regiao regiao) {
        if (regiao == null) {
            throw new RegraNegocioException("Dados da região não informados");
        }
        if (vazio(regiao.getNome()) || regiao.getNome().trim().length() < 3) {
            throw new RegraNegocioException("O nome da região deve ter pelo menos 3 caracteres");
        }
        if (regiao.getNome().trim().length() > 80) {
            throw new RegraNegocioException("O nome da região deve ter no máximo 80 caracteres");
        }
        if (vazio(regiao.getCidade())) {
            throw new RegraNegocioException("Informe a cidade da região");
        }
        if (regiao.getCidade().trim().length() > 60) {
            throw new RegraNegocioException("A cidade deve ter no máximo 60 caracteres");
        }
        if (!regiao.coordenada().valida()) {
            throw new RegraNegocioException("Latitude e longitude da região são inválidas");
        }
        if (regiao.getLimiteChuvaMm() <= 0 || regiao.getLimiteChuvaMm() > 200) {
            throw new RegraNegocioException("O limite de chuva deve ficar entre 1 e 200 mm/h");
        }

        String suscetibilidade = vazio(regiao.getSuscetibilidade()) ? "MEDIA" : semAcento(regiao.getSuscetibilidade()).toUpperCase().trim();
        if (!suscetibilidade.equals("BAIXA") && !suscetibilidade.equals("MEDIA") && !suscetibilidade.equals("ALTA")) {
            throw new RegraNegocioException("A suscetibilidade deve ser BAIXA, MEDIA ou ALTA");
        }

        regiao.setNome(regiao.getNome().trim());
        regiao.setCidade(regiao.getCidade().trim());
        regiao.setSuscetibilidade(suscetibilidade);
    }

    private boolean vazio(String valor) {
        return valor == null || valor.isBlank();
    }

    private String semAcento(String texto) {
        return Normalizer.normalize(texto, Normalizer.Form.NFD).replaceAll("\\p{M}", "");
    }
}
