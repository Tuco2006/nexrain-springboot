package br.com.fiap.nexrain.main;

import br.com.fiap.nexrain.conexoes.ConexaoFactory;
import br.com.fiap.nexrain.excecoes.ExcecoesConexao;

import java.sql.Connection;
import java.sql.SQLException;

public class TesteConexao {

    public static void main(String[] args) throws ExcecoesConexao, SQLException, ClassNotFoundException {

        try {
            Connection cn = new ConexaoFactory().conexao();
            System.out.print("Conectado com o Banco de Dados");
            cn.close();
        }catch (Exception e){
            throw new ExcecoesConexao(e);
        }
    }
}
