package br.com.fiap.nexrain.dao;

import br.com.fiap.nexrain.conexoes.ConexaoFactory;
import br.com.fiap.nexrain.entities.Regiao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class RegiaoDAO implements AutoCloseable {

    private static final String COLUNAS = "ID_REGIAO, NOME, CIDADE, LATITUDE, LONGITUDE, SUSCETIBILIDADE, LIMITE_CHUVA_MM";

    public Connection minhaConexao;

    public RegiaoDAO() throws SQLException, ClassNotFoundException {
        super();
        this.minhaConexao = new ConexaoFactory().conexao();
    }

    // Insert
    public int inserir(Regiao regiao) throws SQLException {
        PreparedStatement stmt = minhaConexao.prepareStatement
                ("Insert into T_FIAP_REGIAO_NR (NOME, CIDADE, LATITUDE, LONGITUDE, SUSCETIBILIDADE, LIMITE_CHUVA_MM) values (?, ?, ?, ?, ?, ?)", new String[]{"ID_REGIAO"});
        stmt.setString(1, regiao.getNome());
        stmt.setString(2, regiao.getCidade());
        stmt.setDouble(3, regiao.getLatitude());
        stmt.setDouble(4, regiao.getLongitude());
        stmt.setString(5, regiao.getSuscetibilidade());
        stmt.setDouble(6, regiao.getLimiteChuvaMm());

        stmt.executeUpdate();

        ResultSet rs = stmt.getGeneratedKeys();
        if (rs.next()) {
            regiao.setId(rs.getInt(1));
        }
        rs.close();
        stmt.close();

        return regiao.getId();
    }

    // Delete
    public int deletar(int id) throws SQLException {
        PreparedStatement stmt = minhaConexao.prepareStatement
                ("Delete from T_FIAP_REGIAO_NR where ID_REGIAO = ?");
        stmt.setInt(1, id);

        int linhas = stmt.executeUpdate();
        stmt.close();

        return linhas;
    }

    // Update
    public int atualizar(Regiao regiao) throws SQLException {
        PreparedStatement stmt = minhaConexao.prepareStatement
                ("Update T_FIAP_REGIAO_NR set NOME = ?, CIDADE = ?, LATITUDE = ?, LONGITUDE = ?, SUSCETIBILIDADE = ?, LIMITE_CHUVA_MM = ? where ID_REGIAO = ?");
        stmt.setString(1, regiao.getNome());
        stmt.setString(2, regiao.getCidade());
        stmt.setDouble(3, regiao.getLatitude());
        stmt.setDouble(4, regiao.getLongitude());
        stmt.setString(5, regiao.getSuscetibilidade());
        stmt.setDouble(6, regiao.getLimiteChuvaMm());
        stmt.setInt(7, regiao.getId());

        int linhas = stmt.executeUpdate();
        stmt.close();

        return linhas;
    }

    // Select
    public List<Regiao> selecionar() throws SQLException {
        List<Regiao> listaRegioes = new ArrayList<Regiao>();
        PreparedStatement stmt = minhaConexao.prepareStatement
                ("select " + COLUNAS + " from T_FIAP_REGIAO_NR order by NOME");
        ResultSet rs = stmt.executeQuery();

        while (rs.next()) {
            listaRegioes.add(montarRegiao(rs));
        }
        rs.close();
        stmt.close();

        return listaRegioes;
    }

    // Buscar por id
    public Regiao buscarPorId(int id) throws SQLException {
        Regiao regiao = null;

        PreparedStatement stmt = minhaConexao.prepareStatement
                ("select " + COLUNAS + " from T_FIAP_REGIAO_NR where ID_REGIAO = ?");
        stmt.setInt(1, id);

        ResultSet rs = stmt.executeQuery();

        if (rs.next()) {
            regiao = montarRegiao(rs);
        }
        rs.close();
        stmt.close();

        return regiao;
    }

    private Regiao montarRegiao(ResultSet rs) throws SQLException {
        Regiao regiao = new Regiao();
        regiao.setId(rs.getInt(1));
        regiao.setNome(rs.getString(2));
        regiao.setCidade(rs.getString(3));
        regiao.setLatitude(rs.getDouble(4));
        regiao.setLongitude(rs.getDouble(5));
        regiao.setSuscetibilidade(rs.getString(6));
        regiao.setLimiteChuvaMm(rs.getDouble(7));
        return regiao;
    }

    @Override
    public void close() throws SQLException {
        if (minhaConexao != null && !minhaConexao.isClosed()) {
            minhaConexao.close();
        }
    }
}
