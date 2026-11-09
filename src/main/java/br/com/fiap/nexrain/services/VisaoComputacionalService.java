package br.com.fiap.nexrain.services;

import br.com.fiap.nexrain.api.AnaliseVisaoIA;
import br.com.fiap.nexrain.entities.MonitorRisco;
import br.com.fiap.nexrain.excecoes.RegraNegocioException;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Base64;

public class VisaoComputacionalService {

    private final static int LARGURA_ANALISE = 160;

    private final static double LIMIAR_ALAGAMENTO = 0.12;

    private final OpenAiService openAiService = new OpenAiService();

    public boolean iaConfigurada() {
        return openAiService.configurada();
    }

    public AnaliseVisaoIA analisar(byte[] imagem, String tipo) {
        BufferedImage figura = lerImagem(imagem);

        if (openAiService.configurada()) {
            try {
                String dataUrl = "data:" + tipo + ";base64," + Base64.getEncoder().encodeToString(imagem);
                AnaliseVisaoIA analise = openAiService.analisarImagem(dataUrl);
                return normalizar(analise);
            } catch (IOException | RuntimeException e) {
                System.out.println("IA indisponível, usando o motor local: " + e.getMessage());
            }
        }
        return analisarLocalmente(figura);
    }

    public AnaliseVisaoIA analisarLocalmente(BufferedImage original) {
        BufferedImage imagem = reduzir(original);
        int largura = imagem.getWidth();
        int altura = imagem.getHeight();

        double[][] luz = new double[altura][largura];
        for (int y = 0; y < altura; y++) {
            for (int x = 0; x < largura; x++) {
                int rgb = imagem.getRGB(x, y);
                int r = (rgb >> 16) & 0xff, g = (rgb >> 8) & 0xff, b = rgb & 0xff;
                luz[y][x] = 0.299 * r + 0.587 * g + 0.114 * b;
            }
        }

        int inicioRua = (int) (altura * 0.40);
        int total = 0, barrenta = 0, reflexo = 0;
        float[] hsv = new float[3];

        for (int y = Math.max(1, inicioRua); y < altura - 2; y++) {
            for (int x = 1; x < largura - 2; x++) {
                total++;

                double media = 0;
                for (int dy = -1; dy <= 1; dy++) {
                    for (int dx = -1; dx <= 1; dx++) {
                        media += luz[y + dy][x + dx];
                    }
                }
                media /= 9;
                double textura = 0;
                for (int dy = -1; dy <= 1; dy++) {
                    for (int dx = -1; dx <= 1; dx++) {
                        textura += Math.abs(luz[y + dy][x + dx] - media);
                    }
                }
                textura /= 9;

                if (textura >= 4) {
                    continue;
                }

                int rgb = imagem.getRGB(x, y);
                Color.RGBtoHSB((rgb >> 16) & 0xff, (rgb >> 8) & 0xff, rgb & 0xff, hsv);
                double matiz = hsv[0] * 360, saturacao = hsv[1], brilho = hsv[2];

                if (matiz >= 15 && matiz <= 55 && saturacao >= 0.12 && saturacao <= 0.75 && brilho >= 0.2 && brilho <= 0.9) {
                    barrenta++;
                } else if (saturacao < 0.22 && brilho > 0.5) {
                    reflexo++;
                }
            }
        }

        double proporcaoBarrenta = total > 0 ? (double) barrenta / total : 0;
        double proporcaoReflexo = total > 0 ? (double) reflexo / total : 0;
        double indiceAgua = proporcaoBarrenta + proporcaoReflexo * 1.6;

        AnaliseVisaoIA analise = new AnaliseVisaoIA();
        analise.setOrigem("motor-local");

        if (indiceAgua >= LIMIAR_ALAGAMENTO) {
            int nivelAgua = (int) Math.min(90, Math.round(indiceAgua * 110));
            analise.setAlagamentoDetectado(true);
            analise.setNivelAguaEstimadoCm(nivelAgua);
            analise.setNivelRisco(MonitorRisco.classificarPorNivelAgua(nivelAgua));
            analise.setConfianca(Math.min(85, Math.round(50 + indiceAgua * 60)));
            analise.setViaTransitavel(nivelAgua < 30);
            analise.setDescricao(String.format("Motor local encontrou %.0f%% da área da via com aspecto de água acumulada " +
                            "(%.0f%% de água barrenta e %.0f%% de reflexo de lâmina d'água).",
                    Math.min(100, indiceAgua * 100), proporcaoBarrenta * 100, proporcaoReflexo * 100));
        } else {
            analise.setAlagamentoDetectado(false);
            analise.setNivelAguaEstimadoCm(0);
            analise.setNivelRisco("BAIXO");
            analise.setConfianca(Math.min(85, Math.round(50 + (LIMIAR_ALAGAMENTO - indiceAgua) * 300)));
            analise.setViaTransitavel(true);
            analise.setDescricao("Motor local não encontrou sinais de água acumulada na via.");
        }
        return analise;
    }

    private AnaliseVisaoIA normalizar(AnaliseVisaoIA analise) {
        analise.setNivelAguaEstimadoCm(Math.max(0, analise.getNivelAguaEstimadoCm()));
        analise.setConfianca(Math.max(0, Math.min(100, analise.getConfianca())));

        String nivelPelaAgua = MonitorRisco.classificarPorNivelAgua(analise.getNivelAguaEstimadoCm());
        if (!MonitorRisco.nivelValido(analise.getNivelRisco())) {
            analise.setNivelRisco(nivelPelaAgua);
        }
        if (analise.getDescricao() == null) {
            analise.setDescricao("");
        }
        return analise;
    }

    private BufferedImage lerImagem(byte[] imagem) {
        try {
            BufferedImage figura = ImageIO.read(new ByteArrayInputStream(imagem));
            if (figura == null) {
                throw new RegraNegocioException("Formato de imagem não suportado, envie JPG ou PNG");
            }
            return figura;
        } catch (IOException e) {
            throw new RegraNegocioException("Não foi possível ler a imagem enviada");
        }
    }

    private BufferedImage reduzir(BufferedImage original) {
        int altura = Math.max(1, original.getHeight() * LARGURA_ANALISE / original.getWidth());
        BufferedImage reduzida = new BufferedImage(LARGURA_ANALISE, altura, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = reduzida.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.drawImage(original, 0, 0, LARGURA_ANALISE, altura, null);
        g.dispose();
        return reduzida;
    }
}
