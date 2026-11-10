package br.com.fiap.nexrain.services;

import br.com.fiap.nexrain.api.AnaliseVisaoIA;
import br.com.fiap.nexrain.conexoes.ClienteHttp;
import br.com.fiap.nexrain.dto.EntradaAnaliseImagem;
import br.com.fiap.nexrain.dto.ResultadoAnaliseImagem;
import br.com.fiap.nexrain.entities.Camera;
import br.com.fiap.nexrain.entities.Coordenada;
import br.com.fiap.nexrain.entities.Ocorrencia;
import br.com.fiap.nexrain.excecoes.RegraNegocioException;
import br.com.fiap.nexrain.excecoes.ServicoExternoException;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.Base64;

public class MonitoramentoService {

    private static final int TAMANHO_MAXIMO = 8 * 1024 * 1024;

    private final VisaoComputacionalService visaoService = new VisaoComputacionalService();
    private final CameraService cameraService = new CameraService();
    private final OcorrenciaService ocorrenciaService = new OcorrenciaService();

    public boolean iaConfigurada() {
        return visaoService.iaConfigurada();
    }

    public ResultadoAnaliseImagem analisar(EntradaAnaliseImagem entrada) throws SQLException, ClassNotFoundException {
        if (entrada == null) {
            throw new RegraNegocioException("Envie uma câmera ou uma imagem para analisar");
        }

        Camera camera = null;
        if (entrada.getIdCamera() != null) {
            camera = cameraService.buscarPorId(entrada.getIdCamera());
            if (!camera.ativa()) {
                throw new RegraNegocioException("A câmera " + camera.getNome() + " está " + ("MANUTENCAO".equals(camera.getStatus()) ? "em manutenção" : "inativa") + " e não pode ser analisada");
            }
        }

        String tipo = "image/jpeg";
        byte[] imagem;

        if (!vazio(entrada.getImagemBase64())) {
            String base64 = entrada.getImagemBase64().trim();
            if (base64.startsWith("data:")) {
                tipo = base64.substring(5, base64.indexOf(';'));
                base64 = base64.substring(base64.indexOf(',') + 1);
            }
            try {
                imagem = Base64.getDecoder().decode(base64);
            } catch (IllegalArgumentException e) {
                throw new RegraNegocioException("Imagem em base64 inválida");
            }
        } else {
            String url = !vazio(entrada.getUrlImagem()) ? entrada.getUrlImagem().trim() : camera != null ? camera.getUrlImagem() : null;
            if (vazio(url)) {
                throw new RegraNegocioException("Envie uma imagem ou escolha uma câmera com imagem cadastrada");
            }
            if (camera != null && !camera.aceitaImagem(url)) {
                throw new RegraNegocioException("A imagem informada não pertence à câmera " + camera.getNome());
            }
            if (!url.startsWith("http")) {
                throw new RegraNegocioException("A URL da imagem deve começar com http ou https");
            }
            try {
                imagem = ClienteHttp.baixar(url, 20000);
            } catch (IOException e) {
                System.out.println("Falha ao baixar a imagem: " + e.getMessage());
                throw new ServicoExternoException("Não foi possível baixar a imagem da câmera");
            }
            if (url.toLowerCase().endsWith(".png")) {
                tipo = "image/png";
            }
        }

        if (imagem.length == 0) {
            throw new RegraNegocioException("A imagem enviada está vazia");
        }
        if (imagem.length > TAMANHO_MAXIMO) {
            throw new RegraNegocioException("A imagem deve ter no máximo 8 MB");
        }

        AnaliseVisaoIA analise = visaoService.analisar(imagem, tipo);

        Ocorrencia ocorrencia = null;
        String mensagem;

        if (analise.isAlagamentoDetectado() && entrada.isRegistrarOcorrencia()) {
            ocorrencia = ocorrenciaService.registrarDeteccao(montarOcorrencia(entrada, camera, analise));
            mensagem = "Alagamento detectado e registrado como ocorrência #" + ocorrencia.getId() + ", aguardando validação.";
        } else if (analise.isAlagamentoDetectado()) {
            mensagem = "Alagamento detectado (não registrado, apenas análise).";
        } else {
            mensagem = "Nenhum alagamento identificado na imagem.";
        }

        return new ResultadoAnaliseImagem(analise, analise.getOrigem(), camera, ocorrencia, mensagem);
    }

    private Ocorrencia montarOcorrencia(EntradaAnaliseImagem entrada, Camera camera, AnaliseVisaoIA analise) {
        Ocorrencia ocorrencia = new Ocorrencia();

        if (camera != null) {
            ocorrencia.setIdCamera(camera.getId());
            ocorrencia.setIdRegiao(camera.getIdRegiao());
            ocorrencia.setEndereco(camera.getEndereco());
            ocorrencia.setLatitude(camera.getLatitude());
            ocorrencia.setLongitude(camera.getLongitude());
            ocorrencia.setOrigem("IA_CAMERA");
        } else {
            if (entrada.getLatitude() == null || entrada.getLongitude() == null
                    || !new Coordenada(entrada.getLatitude(), entrada.getLongitude()).valida()) {
                throw new RegraNegocioException("Informe a localização da foto para registrar a ocorrência");
            }
            ocorrencia.setIdRegiao(entrada.getIdRegiao() != null ? entrada.getIdRegiao() : 0);
            ocorrencia.setEndereco(vazio(entrada.getEndereco()) ? "Local informado pelo cidadão" : entrada.getEndereco());
            ocorrencia.setLatitude(entrada.getLatitude());
            ocorrencia.setLongitude(entrada.getLongitude());
            ocorrencia.setOrigem("CIDADAO");
        }

        ocorrencia.setDataHora(LocalDateTime.now(Ocorrencia.FUSO).withNano(0));
        ocorrencia.setNivelAguaCm(analise.getNivelAguaEstimadoCm());
        ocorrencia.setNivelRisco(analise.getNivelRisco());
        ocorrencia.setConfianca(analise.getConfianca());
        ocorrencia.setStatus("PENDENTE");

        String descricao = "[" + ("openai".equals(analise.getOrigem()) ? "IA OpenAI" : "Motor local") + "] " + analise.getDescricao();
        ocorrencia.setDescricao(descricao.length() > 400 ? descricao.substring(0, 397) + "..." : descricao);
        return ocorrencia;
    }

    private boolean vazio(String valor) {
        return valor == null || valor.isBlank();
    }
}
