package br.com.fiap.nexrain.entities;

import java.util.Locale;

// objeto de valor que representa um ponto no mapa (latitude / longitude)
public class Coordenada {

    private static final double RAIO_TERRA_METROS = 6371000;

    private double latitude;
    private double longitude;

    public Coordenada() {
        super();
    }

    public Coordenada(double latitude, double longitude) {
        super();
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public boolean valida() {
        return latitude >= -90 && latitude <= 90 && longitude >= -180 && longitude <= 180
                && !(latitude == 0 && longitude == 0);
    }

    // distancia real entre dois pontos (formula de Haversine)
    public double distanciaMetros(Coordenada outra) {
        double dLat = Math.toRadians(outra.latitude - latitude);
        double dLon = Math.toRadians(outra.longitude - longitude);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(latitude)) * Math.cos(Math.toRadians(outra.latitude))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        return RAIO_TERRA_METROS * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    }

    // menor distancia entre este ponto e o trecho de rua que vai de "a" ate "b"
    public double distanciaAteSegmento(Coordenada a, Coordenada b) {
        // projeta tudo num plano em metros usando este ponto como origem (suficiente pra distancias dentro da cidade)
        double[] pa = projetar(a);
        double[] pb = projetar(b);

        double dx = pb[0] - pa[0];
        double dy = pb[1] - pa[1];
        double comprimento2 = dx * dx + dy * dy;

        if (comprimento2 == 0) {
            return Math.hypot(pa[0], pa[1]);
        }

        // t = posicao do ponto mais proximo dentro do segmento (0 = inicio, 1 = fim)
        double t = -(pa[0] * dx + pa[1] * dy) / comprimento2;
        t = Math.max(0, Math.min(1, t));

        double px = pa[0] + t * dx;
        double py = pa[1] + t * dy;
        return Math.hypot(px, py);
    }

    // anda "metros" a partir deste ponto na direcao informada (0 = norte, 90 = leste)
    public Coordenada deslocar(double metros, double direcaoGraus) {
        double direcao = Math.toRadians(direcaoGraus);
        double dLat = metros * Math.cos(direcao) / RAIO_TERRA_METROS;
        double dLon = metros * Math.sin(direcao) / (RAIO_TERRA_METROS * Math.cos(Math.toRadians(latitude)));
        return new Coordenada(latitude + Math.toDegrees(dLat), longitude + Math.toDegrees(dLon));
    }

    // direcao (em graus) deste ponto ate o outro
    public double direcaoAte(Coordenada outra) {
        double[] p = projetar(outra);
        double graus = Math.toDegrees(Math.atan2(p[0], p[1]));
        return (graus + 360) % 360;
    }

    private double[] projetar(Coordenada c) {
        double x = Math.toRadians(c.longitude - longitude) * RAIO_TERRA_METROS * Math.cos(Math.toRadians(latitude));
        double y = Math.toRadians(c.latitude - latitude) * RAIO_TERRA_METROS;
        return new double[]{x, y};
    }

    @Override
    public String toString() {
        return String.format(Locale.US, "%.6f,%.6f", latitude, longitude);
    }
}
