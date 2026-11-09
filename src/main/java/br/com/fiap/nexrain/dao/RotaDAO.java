package br.com.fiap.nexrain.dao;

import br.com.fiap.nexrain.conexoes.ConexaoFactory;
import br.com.fiap.nexrain.entities.Coordenada;
import br.com.fiap.nexrain.entities.Rota;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class RotaDAO implements AutoCloseable {

    private static final String COLUNAS = "ID_ROTA, ORIGEM, ORIGEM_LAT, ORIGEM_LON, DESTINO, DESTINO_LAT, DESTINO_LON, DISTANCIA_KM, DURACAO_MIN, " +
            "DISTANCIA_DIRETA_KM, DURACAO_DIRETA_MIN, ALAGAMENTOS_EVITADOS, ALAGAMENTOS_NO_TRAJETO, SITUACAO, DATA_CALCULO";

    public Connection minhaConexao;

    public RotaDAO() throws SQLException, ClassNotFoundException {
        super();
        this.minhaConexao = new ConexaoFactory().conexao();
    }

    // Insert
    public int inserir(Rota rota) throws SQLException {
        PreparedStatement stmt = minhaConexao.prepareStatement
                ("Insert into T_FIAP_ROTA_NR (ORIGEM, ORIGEM_LAT, ORIGEM_LON, DESTINO, DESTINO_LAT, DESTINO_LON, DISTANCIA_KM, DURACAO_MIN, " +
                        "DISTANCIA_DIRETA_KM, DURACAO_DIRETA_MIN, ALAGAMENTOS_EVITADOS, ALAGAMENTOS_NO_TRAJETO, SITUACAO, DATA_CALCULO, TRAJETO, TRAJETO_DIRETO) " +
                        "values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)", new String[]{"ID_ROTA"});
        preencher(stmt, rota);

        stmt.executeUpdate();

        ResultSet rs = stmt.getGeneratedKeys();
        if (rs.next()) {
            rota.setId(rs.getInt(1));
        }
        rs.close();
        stmt.close();

        return rota.getId();
    }

    // Delete
    public int deletar(int id) throws SQLException {
        PreparedStatement stmt = minhaConexao.prepareStatement
                ("Delete from T_FIAP_ROTA_NR where ID_ROTA = ?");
        stmt.setInt(1, id);

        int linhas = stmt.executeUpdate();
        stmt.close();

        return linhas;
    }

    // Update (usado quando a rota e recalculada)
    public int atualizar(Rota rota) throws SQLException {
        PreparedStatement stmt = minhaConexao.prepareStatement
                ("Update T_FIAP_ROTA_NR set ORIGEM = ?, ORIGEM_LAT = ?, ORIGEM_LON = ?, DESTINO = ?, DESTINO_LAT = ?, DESTINO_LON = ?, " +
                        "DISTANCIA_KM = ?, DURACAO_MIN = ?, DISTANCIA_DIRETA_KM = ?, DURACAO_DIRETA_MIN = ?, ALAGAMENTOS_EVITADOS = ?, " +
                        "ALAGAMENTOS_NO_TRAJETO = ?, SITUACAO = ?, DATA_CALCULO = ?, TRAJETO = ?, TRAJETO_DIRETO = ? where ID_ROTA = ?");
        preencher(stmt, rota);
        stmt.setInt(17, rota.getId());

        int linhas = stmt.executeUpdate();
        stmt.close();

        return linhas;
    }

    // Select (historico sem o desenho do trajeto, que e pesado)
    public List<Rota> selecionar() throws SQLException {
        List<Rota> listaRotas = new ArrayList<Rota>();
        PreparedStatement stmt = minhaConexao.prepareStatement
                ("select " + COLUNAS + " from T_FIAP_ROTA_NR order by DATA_CALCULO desc");
        ResultSet rs = stmt.executeQuery();

        while (rs.next()) {
            listaRotas.add(montarRota(rs));
        }
        rs.close();
        stmt.close();

        return listaRotas;
    }

    // Buscar por id (com o trajeto completo pra desenhar no mapa)
    public Rota buscarPorId(int id) throws SQLException {
        Rota rota = null;

        PreparedStatement stmt = minhaConexao.prepareStatement
                ("select " + COLUNAS + ", TRAJETO, TRAJETO_DIRETO from T_FIAP_ROTA_NR where ID_ROTA = ?");
        stmt.setInt(1, id);

        ResultSet rs = stmt.executeQuery();

        if (rs.next()) {
            rota = montarRota(rs);
            rota.setTrajeto(lerTrajeto(rs.getString(16)));
            rota.setTrajetoDireto(lerTrajeto(rs.getString(17)));
        }
        rs.close();
        stmt.close();

        return rota;
    }

    private void preencher(PreparedStatement stmt, Rota rota) throws SQLException {
        stmt.setString(1, rota.getOrigem());
        stmt.setDouble(2, rota.getOrigemLat());
        stmt.setDouble(3, rota.getOrigemLon());
        stmt.setString(4, rota.getDestino());
        stmt.setDouble(5, rota.getDestinoLat());
        stmt.setDouble(6, rota.getDestinoLon());
        stmt.setDouble(7, rota.getDistanciaKm());
        stmt.setDouble(8, rota.getDuracaoMin());
        stmt.setDouble(9, rota.getDistanciaDiretaKm());
        stmt.setDouble(10, rota.getDuracaoDiretaMin());
        stmt.setInt(11, rota.getAlagamentosEvitados());
        stmt.setInt(12, rota.getAlagamentosNoTrajeto());
        stmt.setString(13, rota.getSituacao());
        stmt.setTimestamp(14, Timestamp.valueOf(rota.getDataCalculo()));
        stmt.setString(15, escreverTrajeto(rota.getTrajeto()));
        stmt.setString(16, escreverTrajeto(rota.getTrajetoDireto()));
    }

    private Rota montarRota(ResultSet rs) throws SQLException {
        Rota rota = new Rota();
        rota.setId(rs.getInt(1));
        rota.setOrigem(rs.getString(2));
        rota.setOrigemLat(rs.getDouble(3));
        rota.setOrigemLon(rs.getDouble(4));
        rota.setDestino(rs.getString(5));
        rota.setDestinoLat(rs.getDouble(6));
        rota.setDestinoLon(rs.getDouble(7));
        rota.setDistanciaKm(rs.getDouble(8));
        rota.setDuracaoMin(rs.getDouble(9));
        rota.setDistanciaDiretaKm(rs.getDouble(10));
        rota.setDuracaoDiretaMin(rs.getDouble(11));
        rota.setAlagamentosEvitados(rs.getInt(12));
        rota.setAlagamentosNoTrajeto(rs.getInt(13));
        rota.setSituacao(rs.getString(14));
        Timestamp calculo = rs.getTimestamp(15);
        rota.setDataCalculo(calculo != null ? calculo.toLocalDateTime().withNano(0) : null);
        return rota;
    }

    // trajeto gravado no CLOB no formato "lat,lon;lat,lon;..."
    private String escreverTrajeto(List<Coordenada> pontos) {
        if (pontos == null || pontos.isEmpty()) {
            return null;
        }
        StringBuilder texto = new StringBuilder();
        for (Coordenada ponto : pontos) {
            if (texto.length() > 0) {
                texto.append(';');
            }
            texto.append(String.format(Locale.US, "%.5f,%.5f", ponto.getLatitude(), ponto.getLongitude()));
        }
        return texto.toString();
    }

    private List<Coordenada> lerTrajeto(String texto) {
        List<Coordenada> pontos = new ArrayList<Coordenada>();
        if (texto == null || texto.isBlank()) {
            return pontos;
        }
        for (String par : texto.split(";")) {
            String[] partes = par.split(",");
            if (partes.length == 2) {
                pontos.add(new Coordenada(Double.parseDouble(partes[0]), Double.parseDouble(partes[1])));
            }
        }
        return pontos;
    }

    @Override
    public void close() throws SQLException {
        if (minhaConexao != null && !minhaConexao.isClosed()) {
            minhaConexao.close();
        }
    }
}
