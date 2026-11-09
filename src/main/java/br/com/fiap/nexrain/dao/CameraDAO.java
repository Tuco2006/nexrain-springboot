package br.com.fiap.nexrain.dao;

import br.com.fiap.nexrain.conexoes.ConexaoFactory;
import br.com.fiap.nexrain.entities.Camera;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class CameraDAO implements AutoCloseable {

    private static final String COLUNAS = "ID_CAMERA, ID_REGIAO, NOME, ENDERECO, LATITUDE, LONGITUDE, URL_IMAGEM, STATUS";

    public Connection minhaConexao;

    public CameraDAO() throws SQLException, ClassNotFoundException {
        super();
        this.minhaConexao = new ConexaoFactory().conexao();
    }

    // Insert
    public int inserir(Camera camera) throws SQLException {
        PreparedStatement stmt = minhaConexao.prepareStatement
                ("Insert into T_FIAP_CAMERA_NR (ID_REGIAO, NOME, ENDERECO, LATITUDE, LONGITUDE, URL_IMAGEM, STATUS) values (?, ?, ?, ?, ?, ?, ?)", new String[]{"ID_CAMERA"});
        stmt.setInt(1, camera.getIdRegiao());
        stmt.setString(2, camera.getNome());
        stmt.setString(3, camera.getEndereco());
        stmt.setDouble(4, camera.getLatitude());
        stmt.setDouble(5, camera.getLongitude());
        stmt.setString(6, camera.getUrlImagem());
        stmt.setString(7, camera.getStatus());

        stmt.executeUpdate();

        ResultSet rs = stmt.getGeneratedKeys();
        if (rs.next()) {
            camera.setId(rs.getInt(1));
        }
        rs.close();
        stmt.close();

        return camera.getId();
    }

    // Delete
    public int deletar(int id) throws SQLException {
        PreparedStatement stmt = minhaConexao.prepareStatement
                ("Delete from T_FIAP_CAMERA_NR where ID_CAMERA = ?");
        stmt.setInt(1, id);

        int linhas = stmt.executeUpdate();
        stmt.close();

        return linhas;
    }

    // Update
    public int atualizar(Camera camera) throws SQLException {
        PreparedStatement stmt = minhaConexao.prepareStatement
                ("Update T_FIAP_CAMERA_NR set ID_REGIAO = ?, NOME = ?, ENDERECO = ?, LATITUDE = ?, LONGITUDE = ?, URL_IMAGEM = ?, STATUS = ? where ID_CAMERA = ?");
        stmt.setInt(1, camera.getIdRegiao());
        stmt.setString(2, camera.getNome());
        stmt.setString(3, camera.getEndereco());
        stmt.setDouble(4, camera.getLatitude());
        stmt.setDouble(5, camera.getLongitude());
        stmt.setString(6, camera.getUrlImagem());
        stmt.setString(7, camera.getStatus());
        stmt.setInt(8, camera.getId());

        int linhas = stmt.executeUpdate();
        stmt.close();

        return linhas;
    }

    // Select
    public List<Camera> selecionar() throws SQLException {
        PreparedStatement stmt = minhaConexao.prepareStatement
                ("select " + COLUNAS + " from T_FIAP_CAMERA_NR order by ID_CAMERA");
        return listar(stmt);
    }

    public List<Camera> selecionarPorRegiao(int idRegiao) throws SQLException {
        PreparedStatement stmt = minhaConexao.prepareStatement
                ("select " + COLUNAS + " from T_FIAP_CAMERA_NR where ID_REGIAO = ? order by ID_CAMERA");
        stmt.setInt(1, idRegiao);
        return listar(stmt);
    }

    // Buscar por id
    public Camera buscarPorId(int id) throws SQLException {
        Camera camera = null;

        PreparedStatement stmt = minhaConexao.prepareStatement
                ("select " + COLUNAS + " from T_FIAP_CAMERA_NR where ID_CAMERA = ?");
        stmt.setInt(1, id);

        ResultSet rs = stmt.executeQuery();

        if (rs.next()) {
            camera = montarCamera(rs);
        }
        rs.close();
        stmt.close();

        return camera;
    }

    private List<Camera> listar(PreparedStatement stmt) throws SQLException {
        List<Camera> listaCameras = new ArrayList<Camera>();
        ResultSet rs = stmt.executeQuery();

        while (rs.next()) {
            listaCameras.add(montarCamera(rs));
        }
        rs.close();
        stmt.close();

        return listaCameras;
    }

    private Camera montarCamera(ResultSet rs) throws SQLException {
        Camera camera = new Camera();
        camera.setId(rs.getInt(1));
        camera.setIdRegiao(rs.getInt(2));
        camera.setNome(rs.getString(3));
        camera.setEndereco(rs.getString(4));
        camera.setLatitude(rs.getDouble(5));
        camera.setLongitude(rs.getDouble(6));
        camera.setUrlImagem(rs.getString(7));
        camera.setStatus(rs.getString(8));
        return camera;
    }

    @Override
    public void close() throws SQLException {
        if (minhaConexao != null && !minhaConexao.isClosed()) {
            minhaConexao.close();
        }
    }
}
