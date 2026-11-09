package br.com.fiap.nexrain.main;

import br.com.fiap.nexrain.entities.Regiao;
import br.com.fiap.nexrain.services.RegiaoService;

import java.sql.SQLException;
import java.util.List;

public class TesteSelecionarRegiao {

    public static void main(String[] args) throws SQLException, ClassNotFoundException {

        RegiaoService regiaoService = new RegiaoService();

        List<Regiao> listaRegioes = regiaoService.selecionar();

        if (listaRegioes != null) {
            // foreach
            for (Regiao r : listaRegioes) {
                System.out.println(
                        r.getId() + " " +
                        r.getNome() + " " +
                        r.getCidade() + " " +
                        r.getSuscetibilidade() + " " +
                        r.getLimiteChuvaMm() + " mm/h"
                );
            }
        }
    }
}
