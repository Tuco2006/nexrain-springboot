package br.com.fiap.nexrain.dao;

import br.com.fiap.nexrain.conexoes.ConexaoFactory;
import br.com.fiap.nexrain.entities.Alerta;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class AlertaDAO implements AutoCloseable {

    private static final String COLUNAS = "ID_ALERTA, ID_REGIAO, ID_OCORRENCIA, TITULO, MENSAGEM, NIVEL, ORIGEM, DATA_EMISSAO, DATA_EXPIRACAO, ATIVO";

    public Connection minhaConexao;

    public AlertaDAO() throws SQLException, ClassNotFoundException {
        super();
        this.minhaConexao = new ConexaoFactory().conexao();
    }

    // Insert
    public int inserir(Alerta alerta) throws SQLException {
        PreparedStatement stmt = minhaConexao.prepareStatement
                ("Insert into T_FIAP_ALERTA_NR (ID_REGIAO, ID_OCORRENCIA, TITULO, MENSAGEM, NIVEL, ORIGEM, DATA_EMISSAO, DATA_EXPIRACAO, ATIVO) " +
                        "values (?, ?, ?, ?, ?, ?, ?, ?, ?)", new String[]{"ID_ALERTA"});
        preencher(stmt, alerta);

        stmt.executeUpdate();

        ResultSet rs = stmt.getGeneratedKeys();
        if (rs.next()) {
            alerta.setId(rs.getInt(1));
        }
        rs.close();
        stmt.close();

        return alerta.getId();
    }

    // Delete
    public int deletar(int id) throws SQLException {
        PreparedStatement stmt = minhaConexao.prepareStatement
                ("Delete from T_FIAP_ALERTA_NR where ID_ALERTA = ?");
        stmt.setInt(1, id);

        int linhas = stmt.executeUpdate();
        stmt.close();

        return linhas;
    }

    // Update
    public int atualizar(Alerta alerta) throws SQLException {
        PreparedStatement stmt = minhaConexao.prepareStatement
                ("Update T_FIAP_ALERTA_NR set ID_REGIAO = ?, ID_OCORRENCIA = ?, TITULO = ?, MENSAGEM = ?, NIVEL = ?, ORIGEM = ?, " +
                        "DATA_EMISSAO = ?, DATA_EXPIRACAO = ?, ATIVO = ? where ID_ALERTA = ?");
        preencher(stmt, alerta);
        stmt.setInt(10, alerta.getId());

        int linhas = stmt.executeUpdate();
        stmt.close();

        return linhas;
    }

    public int desativarPorOcorrencia(int idOcorrencia) throws SQLException {
        PreparedStatement stmt = minhaConexao.prepareStatement
                ("Update T_FIAP_ALERTA_NR set ATIVO = 'N' where ID_OCORRENCIA = ?");
        stmt.setInt(1, idOcorrencia);

        int linhas = stmt.executeUpdate();
        stmt.close();

        return linhas;
    }

    // Select
    public List<Alerta> selecionar() throws SQLException {
        PreparedStatement stmt = minhaConexao.prepareStatement
                ("select " + COLUNAS + " from T_FIAP_ALERTA_NR order by DATA_EMISSAO desc");
        return listar(stmt);
    }

    public List<Alerta> selecionarVigentes(LocalDateTime agora) throws SQLException {
        PreparedStatement stmt = minhaConexao.prepareStatement
                ("select " + COLUNAS + " from T_FIAP_ALERTA_NR where ATIVO = 'S' and DATA_EXPIRACAO > ? order by DATA_EMISSAO desc");
        stmt.setTimestamp(1, Timestamp.valueOf(agora));
        return listar(stmt);
    }

    public boolean existeVigente(int idRegiao, String origem, LocalDateTime agora) throws SQLException {
        PreparedStatement stmt = minhaConexao.prepareStatement
                ("select count(*) from T_FIAP_ALERTA_NR where ID_REGIAO = ? and ORIGEM = ? and ATIVO = 'S' and DATA_EXPIRACAO > ?");
        stmt.setInt(1, idRegiao);
        stmt.setString(2, origem);
        stmt.setTimestamp(3, Timestamp.valueOf(agora));

        ResultSet rs = stmt.executeQuery();
        boolean existe = rs.next() && rs.getInt(1) > 0;
        rs.close();
        stmt.close();

        return existe;
    }

    // Buscar por id
    public Alerta buscarPorId(int id) throws SQLException {
        Alerta alerta = null;

        PreparedStatement stmt = minhaConexao.prepareStatement
                ("select " + COLUNAS + " from T_FIAP_ALERTA_NR where ID_ALERTA = ?");
        stmt.setInt(1, id);

        ResultSet rs = stmt.executeQuery();

        if (rs.next()) {
            alerta = montarAlerta(rs);
        }
        rs.close();
        stmt.close();

        return alerta;
    }

    private void preencher(PreparedStatement stmt, Alerta alerta) throws SQLException {
        stmt.setInt(1, alerta.getIdRegiao());
        if (alerta.getIdOcorrencia() != null) {
            stmt.setInt(2, alerta.getIdOcorrencia());
        } else {
            stmt.setNull(2, Types.INTEGER);
        }
        stmt.setString(3, alerta.getTitulo());
        stmt.setString(4, alerta.getMensagem());
        stmt.setString(5, alerta.getNivel());
        stmt.setString(6, alerta.getOrigem());
        stmt.setTimestamp(7, Timestamp.valueOf(alerta.getDataEmissao()));
        stmt.setTimestamp(8, Timestamp.valueOf(alerta.getDataExpiracao()));
        stmt.setString(9, alerta.isAtivo() ? "S" : "N");
    }

    private List<Alerta> listar(PreparedStatement stmt) throws SQLException {
        List<Alerta> listaAlertas = new ArrayList<Alerta>();
        ResultSet rs = stmt.executeQuery();

        while (rs.next()) {
            listaAlertas.add(montarAlerta(rs));
        }
        rs.close();
        stmt.close();

        return listaAlertas;
    }

    private Alerta montarAlerta(ResultSet rs) throws SQLException {
        Alerta alerta = new Alerta();
        alerta.setId(rs.getInt(1));
        alerta.setIdRegiao(rs.getInt(2));
        int idOcorrencia = rs.getInt(3);
        alerta.setIdOcorrencia(rs.wasNull() ? null : idOcorrencia);
        alerta.setTitulo(rs.getString(4));
        alerta.setMensagem(rs.getString(5));
        alerta.setNivel(rs.getString(6));
        alerta.setOrigem(rs.getString(7));
        alerta.setDataEmissao(dataHora(rs.getTimestamp(8)));
        alerta.setDataExpiracao(dataHora(rs.getTimestamp(9)));
        alerta.setAtivo("S".equals(rs.getString(10)));
        return alerta;
    }

    private LocalDateTime dataHora(Timestamp timestamp) {
        return timestamp != null ? timestamp.toLocalDateTime().withNano(0) : null;
    }

    @Override
    public void close() throws SQLException {
        if (minhaConexao != null && !minhaConexao.isClosed()) {
            minhaConexao.close();
        }
    }
}
