package br.com.fiap.nexrain.conexoes;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConexaoFactory {

    private final static String URL = Configuracao.ler("DB_URL", "db.url");
    private final static String USUARIO = Configuracao.ler("DB_USUARIO", "db.usuario");
    private final static String SENHA = Configuracao.ler("DB_SENHA", "db.senha");

    public Connection conexao() throws ClassNotFoundException, SQLException {

        Class.forName("oracle.jdbc.driver.OracleDriver");

        return DriverManager.getConnection(URL, USUARIO, SENHA);
    }
}
