package br.com.fiap.nexrain.dao;

import br.com.fiap.nexrain.conexoes.ConexaoFactory;
import br.com.fiap.nexrain.entities.Ocorrencia;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class OcorrenciaDAO implements AutoCloseable {

    private static final String COLUNAS = "ID_OCORRENCIA, ID_REGIAO, ID_CAMERA, ENDERECO, LATITUDE, LONGITUDE, DATA_HORA, NIVEL_AGUA_CM, " +
            "NIVEL_RISCO, CONFIANCA, ORIGEM, STATUS, DESCRICAO, VALIDADO_POR, DATA_VALIDACAO";

    public Connection minhaConexao;

    public OcorrenciaDAO() throws SQLException, ClassNotFoundException {
        super();
        this.minhaConexao = new ConexaoFactory().conexao();
    }

    // Insert
    public int inserir(Ocorrencia ocorrencia) throws SQLException {
        PreparedStatement stmt = minhaConexao.prepareStatement
                ("Insert into T_FIAP_OCORRENCIA_NR (ID_REGIAO, ID_CAMERA, ENDERECO, LATITUDE, LONGITUDE, DATA_HORA, NIVEL_AGUA_CM, NIVEL_RISCO, " +
                        "CONFIANCA, ORIGEM, STATUS, DESCRICAO, VALIDADO_POR, DATA_VALIDACAO) values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)", new String[]{"ID_OCORRENCIA"});
        preencher(stmt, ocorrencia);

        stmt.executeUpdate();

        ResultSet rs = stmt.getGeneratedKeys();
        if (rs.next()) {
            ocorrencia.setId(rs.getInt(1));
        }
        rs.close();
        stmt.close();

        return ocorrencia.getId();
    }

    // Delete
    public int deletar(int id) throws SQLException {
        PreparedStatement stmt = minhaConexao.prepareStatement
                ("Delete from T_FIAP_OCORRENCIA_NR where ID_OCORRENCIA = ?");
        stmt.setInt(1, id);

        int linhas = stmt.executeUpdate();
        stmt.close();

        return linhas;
    }

    // Update
    public int atualizar(Ocorrencia ocorrencia) throws SQLException {
        PreparedStatement stmt = minhaConexao.prepareStatement
                ("Update T_FIAP_OCORRENCIA_NR set ID_REGIAO = ?, ID_CAMERA = ?, ENDERECO = ?, LATITUDE = ?, LONGITUDE = ?, DATA_HORA = ?, " +
                        "NIVEL_AGUA_CM = ?, NIVEL_RISCO = ?, CONFIANCA = ?, ORIGEM = ?, STATUS = ?, DESCRICAO = ?, VALIDADO_POR = ?, DATA_VALIDACAO = ? " +
                        "where ID_OCORRENCIA = ?");
        preencher(stmt, ocorrencia);
        stmt.setInt(15, ocorrencia.getId());

        int linhas = stmt.executeUpdate();
        stmt.close();

        return linhas;
    }

    // Select
    public List<Ocorrencia> selecionar() throws SQLException {
        PreparedStatement stmt = minhaConexao.prepareStatement
                ("select " + COLUNAS + " from T_FIAP_OCORRENCIA_NR order by DATA_HORA desc");
        return listar(stmt);
    }

    public List<Ocorrencia> selecionarPorStatus(String status) throws SQLException {
        PreparedStatement stmt = minhaConexao.prepareStatement
                ("select " + COLUNAS + " from T_FIAP_OCORRENCIA_NR where STATUS = ? order by DATA_HORA desc");
        stmt.setString(1, status);
        return listar(stmt);
    }

    public List<Ocorrencia> selecionarAtivas() throws SQLException {
        PreparedStatement stmt = minhaConexao.prepareStatement
                ("select " + COLUNAS + " from T_FIAP_OCORRENCIA_NR where STATUS in ('PENDENTE', 'CONFIRMADA') order by DATA_HORA desc");
        return listar(stmt);
    }

    public List<Ocorrencia> selecionarPorRegiao(int idRegiao) throws SQLException {
        PreparedStatement stmt = minhaConexao.prepareStatement
                ("select " + COLUNAS + " from T_FIAP_OCORRENCIA_NR where ID_REGIAO = ? order by DATA_HORA desc");
        stmt.setInt(1, idRegiao);
        return listar(stmt);
    }

    // Buscar por id
    public Ocorrencia buscarPorId(int id) throws SQLException {
        Ocorrencia ocorrencia = null;

        PreparedStatement stmt = minhaConexao.prepareStatement
                ("select " + COLUNAS + " from T_FIAP_OCORRENCIA_NR where ID_OCORRENCIA = ?");
        stmt.setInt(1, id);

        ResultSet rs = stmt.executeQuery();

        if (rs.next()) {
            ocorrencia = montarOcorrencia(rs);
        }
        rs.close();
        stmt.close();

        return ocorrencia;
    }

    private void preencher(PreparedStatement stmt, Ocorrencia ocorrencia) throws SQLException {
        stmt.setInt(1, ocorrencia.getIdRegiao());
        if (ocorrencia.getIdCamera() != null) {
            stmt.setInt(2, ocorrencia.getIdCamera());
        } else {
            stmt.setNull(2, Types.INTEGER);
        }
        stmt.setString(3, ocorrencia.getEndereco());
        stmt.setDouble(4, ocorrencia.getLatitude());
        stmt.setDouble(5, ocorrencia.getLongitude());
        stmt.setTimestamp(6, Timestamp.valueOf(ocorrencia.getDataHora()));
        stmt.setInt(7, ocorrencia.getNivelAguaCm());
        stmt.setString(8, ocorrencia.getNivelRisco());
        stmt.setDouble(9, ocorrencia.getConfianca());
        stmt.setString(10, ocorrencia.getOrigem());
        stmt.setString(11, ocorrencia.getStatus());
        stmt.setString(12, ocorrencia.getDescricao());
        stmt.setString(13, ocorrencia.getValidadoPor());
        if (ocorrencia.getDataValidacao() != null) {
            stmt.setTimestamp(14, Timestamp.valueOf(ocorrencia.getDataValidacao()));
        } else {
            stmt.setNull(14, Types.TIMESTAMP);
        }
    }

    private List<Ocorrencia> listar(PreparedStatement stmt) throws SQLException {
        List<Ocorrencia> listaOcorrencias = new ArrayList<Ocorrencia>();
        ResultSet rs = stmt.executeQuery();

        while (rs.next()) {
            listaOcorrencias.add(montarOcorrencia(rs));
        }
        rs.close();
        stmt.close();

        return listaOcorrencias;
    }

    private Ocorrencia montarOcorrencia(ResultSet rs) throws SQLException {
        Ocorrencia ocorrencia = new Ocorrencia();
        ocorrencia.setId(rs.getInt(1));
        ocorrencia.setIdRegiao(rs.getInt(2));
        int idCamera = rs.getInt(3);
        ocorrencia.setIdCamera(rs.wasNull() ? null : idCamera);
        ocorrencia.setEndereco(rs.getString(4));
        ocorrencia.setLatitude(rs.getDouble(5));
        ocorrencia.setLongitude(rs.getDouble(6));
        ocorrencia.setDataHora(dataHora(rs.getTimestamp(7)));
        ocorrencia.setNivelAguaCm(rs.getInt(8));
        ocorrencia.setNivelRisco(rs.getString(9));
        ocorrencia.setConfianca(rs.getDouble(10));
        ocorrencia.setOrigem(rs.getString(11));
        ocorrencia.setStatus(rs.getString(12));
        ocorrencia.setDescricao(rs.getString(13));
        ocorrencia.setValidadoPor(rs.getString(14));
        ocorrencia.setDataValidacao(dataHora(rs.getTimestamp(15)));
        return ocorrencia;
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
