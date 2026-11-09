package br.com.fiap.nexrain.services;

import br.com.fiap.nexrain.api.TrajetoCandidato;
import br.com.fiap.nexrain.dao.RotaDAO;
import br.com.fiap.nexrain.dto.LocalEncontrado;
import br.com.fiap.nexrain.dto.ResultadoRota;
import br.com.fiap.nexrain.dto.SolicitacaoRota;
import br.com.fiap.nexrain.entities.Coordenada;
import br.com.fiap.nexrain.entities.Ocorrencia;
import br.com.fiap.nexrain.entities.PlanejadorRota;
import br.com.fiap.nexrain.entities.Rota;
import br.com.fiap.nexrain.excecoes.RecursoNaoEncontradoException;
import br.com.fiap.nexrain.excecoes.RegraNegocioException;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class RotaService {

    // limite de chamadas extras ao OSRM por calculo (o servidor publico tem limite de uso)
    private static final int MAXIMO_DESVIOS = 6;
    private static final double DISTANCIA_MAXIMA_KM = 80;

    private final MapaService mapaService = new MapaService();
    private final OcorrenciaService ocorrenciaService = new OcorrenciaService();

    // Selecionar (historico)
    public List<Rota> selecionar() throws SQLException, ClassNotFoundException {
        try (RotaDAO rotaDAO = new RotaDAO()) {
            return rotaDAO.selecionar();
        }
    }

    // Buscar por id
    public Rota buscarPorId(int id) throws SQLException, ClassNotFoundException {
        try (RotaDAO rotaDAO = new RotaDAO()) {
            Rota rota = rotaDAO.buscarPorId(id);

            if (rota == null) {
                throw new RecursoNaoEncontradoException("Rota " + id + " não encontrada");
            }
            return rota;
        }
    }

    // Calcular uma rota segura e guardar no historico
    public ResultadoRota calcular(SolicitacaoRota solicitacao) throws SQLException, ClassNotFoundException {
        ResultadoRota resultado = planejar(solicitacao);

        try (RotaDAO rotaDAO = new RotaDAO()) {
            rotaDAO.inserir(resultado.getRota());
        }
        return resultado;
    }

    // Recalcular uma rota ja salva (os alagamentos podem ter mudado)
    public ResultadoRota recalcular(int id, SolicitacaoRota solicitacao) throws SQLException, ClassNotFoundException {
        Rota existente = buscarPorId(id);

        if (solicitacao == null) {
            solicitacao = new SolicitacaoRota();
        }
        // o que nao vier no corpo da requisicao continua igual ao que estava salvo
        if (vazio(solicitacao.getOrigem()) && solicitacao.getOrigemLat() == null) {
            solicitacao.setOrigem(existente.getOrigem());
            solicitacao.setOrigemLat(existente.getOrigemLat());
            solicitacao.setOrigemLon(existente.getOrigemLon());
        }
        if (vazio(solicitacao.getDestino()) && solicitacao.getDestinoLat() == null) {
            solicitacao.setDestino(existente.getDestino());
            solicitacao.setDestinoLat(existente.getDestinoLat());
            solicitacao.setDestinoLon(existente.getDestinoLon());
        }

        ResultadoRota resultado = planejar(solicitacao);
        resultado.getRota().setId(id);

        try (RotaDAO rotaDAO = new RotaDAO()) {
            rotaDAO.atualizar(resultado.getRota());
        }
        return resultado;
    }

    // Deletar
    public void deletar(int id) throws SQLException, ClassNotFoundException {
        try (RotaDAO rotaDAO = new RotaDAO()) {
            if (rotaDAO.deletar(id) == 0) {
                throw new RecursoNaoEncontradoException("Rota " + id + " não encontrada");
            }
        }
    }

    // ALGORITMO DE ROTA SEGURA
    // 1) pede ao OSRM a rota mais rapida e as alternativas
    // 2) verifica quais passam dentro do raio de algum alagamento ativo
    // 3) se a melhor ainda passa por alagamento, cria pontos de desvio dos dois lados e pede novas rotas por eles
    // 4) fica com a de menor custo (tempo + penalidade por alagamento)
    private ResultadoRota planejar(SolicitacaoRota solicitacao) throws SQLException, ClassNotFoundException {
        if (solicitacao == null) {
            throw new RegraNegocioException("Informe a origem e o destino");
        }

        LocalEncontrado origem = resolver(solicitacao.getOrigem(), solicitacao.getOrigemLat(), solicitacao.getOrigemLon(), "origem");
        LocalEncontrado destino = resolver(solicitacao.getDestino(), solicitacao.getDestinoLat(), solicitacao.getDestinoLon(), "destino");

        Coordenada coordOrigem = new Coordenada(origem.getLatitude(), origem.getLongitude());
        Coordenada coordDestino = new Coordenada(destino.getLatitude(), destino.getLongitude());

        double distanciaKm = coordOrigem.distanciaMetros(coordDestino) / 1000;
        if (distanciaKm < 0.2) {
            throw new RegraNegocioException("Origem e destino estão muito próximos");
        }
        if (distanciaKm > DISTANCIA_MAXIMA_KM) {
            throw new RegraNegocioException("O NexRain calcula rotas de até " + (int) DISTANCIA_MAXIMA_KM + " km dentro da região monitorada");
        }

        List<TrajetoCandidato> candidatos = new ArrayList<TrajetoCandidato>(mapaService.buscarRotas(coordOrigem, coordDestino));
        if (candidatos.isEmpty()) {
            throw new RegraNegocioException("Não foi encontrada rota de carro entre os dois pontos");
        }
        TrajetoCandidato direto = candidatos.get(0);

        PlanejadorRota planejador = new PlanejadorRota(ocorrenciaService.selecionarAtivas());
        TrajetoCandidato melhor = planejador.escolherMelhor(candidatos);

        int desviosTestados = 0;
        List<Ocorrencia> problemas = planejador.alagamentosNoTrajeto(melhor);
        problemas.sort((a, b) -> Double.compare(b.penalidadeRota(), a.penalidadeRota()));

        for (Ocorrencia problema : problemas) {
            if (desviosTestados >= MAXIMO_DESVIOS) {
                break;
            }
            for (Coordenada via : planejador.gerarPontosDesvio(melhor, problema)) {
                if (desviosTestados >= MAXIMO_DESVIOS) {
                    break;
                }
                TrajetoCandidato desvio = mapaService.buscarRotaPassandoPor(coordOrigem, via, coordDestino);
                desviosTestados++;

                // descarta desvio absurdamente mais longo que a rota original ou que faz "ida e volta" na mesma rua
                if (desvio != null && desvio.getDuracaoSegundos() <= direto.getDuracaoSegundos() * 2.5 + 600
                        && !planejador.possuiRetorno(desvio)) {
                    candidatos.add(desvio);
                    if (!planejador.alagamentosNoTrajeto(desvio).contains(problema)) {
                        break;
                    }
                }
            }
            melhor = planejador.escolherMelhor(candidatos);
            if (planejador.alagamentosNoTrajeto(melhor).isEmpty()) {
                break;
            }
        }

        System.out.println("Rota " + origem.getNome() + " -> " + destino.getNome() + ": " + candidatos.size() +
                " caminho(s) avaliado(s), " + desviosTestados + " ponto(s) de desvio testado(s)");

        Rota rota = planejador.montarRota(origem.getNome(), coordOrigem, destino.getNome(), coordDestino,
                direto, melhor, LocalDateTime.now(Ocorrencia.FUSO).withNano(0));

        List<Ocorrencia> noDireto = planejador.alagamentosNoTrajeto(direto);
        List<Ocorrencia> noTrajeto = planejador.alagamentosNoTrajeto(melhor);

        return new ResultadoRota(rota, noDireto, noTrajeto, candidatos.size(), montarMensagem(rota, noDireto));
    }

    private String montarMensagem(Rota rota, List<Ocorrencia> noDireto) {
        if (noDireto.isEmpty()) {
            return "Nenhum alagamento ativo no caminho. A rota mais rápida já é segura.";
        }
        switch (rota.getSituacao()) {
            case "SEGURA":
                return String.format(Locale.US, "Rota segura encontrada: desvia de %d alagamento(s) com %.1f min a mais de viagem.",
                        rota.getAlagamentosEvitados(), rota.getMinutosExtras());
            case "RISCO_REDUZIDO":
                return "Não foi possível desviar de todos os alagamentos, mas a rota escolhida passa por menos pontos de risco.";
            default:
                return "Não existe caminho próximo sem alagamento. Se puder, adie a viagem ou siga os alertas da Defesa Civil.";
        }
    }

    // aceita coordenada pronta (clique no mapa) ou endereco digitado
    private LocalEncontrado resolver(String texto, Double latitude, Double longitude, String campo) {
        if (latitude != null && longitude != null) {
            Coordenada ponto = new Coordenada(latitude, longitude);
            if (!ponto.valida()) {
                throw new RegraNegocioException("Coordenadas de " + campo + " inválidas");
            }
            String nome = vazio(texto) ? String.format(Locale.US, "%.5f, %.5f", latitude, longitude) : texto.trim();
            return new LocalEncontrado(cortar(nome), latitude, longitude);
        }
        if (vazio(texto)) {
            throw new RegraNegocioException("Informe o endereço de " + campo);
        }
        LocalEncontrado local = mapaService.localizar(texto);
        return new LocalEncontrado(cortar(local.getNome()), local.getLatitude(), local.getLongitude());
    }

    private String cortar(String texto) {
        return texto.length() > 150 ? texto.substring(0, 147) + "..." : texto;
    }

    private boolean vazio(String valor) {
        return valor == null || valor.isBlank();
    }
}
