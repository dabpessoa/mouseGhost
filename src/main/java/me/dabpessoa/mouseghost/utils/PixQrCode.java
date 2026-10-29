package me.dabpessoa.mouseghost.utils;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

/**
 * Gera o QR Code do Pix em formato vetorial a partir do código "copia e cola".
 * Um QR Code do Pix nada mais é do que esse mesmo texto codificado.
 */
public final class PixQrCode {

    /** Margem de silêncio recomendada para leitura, em módulos (quadradinhos). */
    public static final int ZONA_SILENCIOSA = 4;

    private PixQrCode() {
        // Do nothing. Construtor protegido para evitar instanciação de classe utilitária.
    }

    public static void salvarSvg(String payload, Path destino) throws IOException {
        Files.writeString(destino, gerarSvg(payload));
    }

    /** Matriz de módulos do QR Code, sem margem (true = módulo preto). */
    private static BitMatrix gerarMatriz(String payload) {
        try {
            // Sem CHARACTER_SET: o payload do Pix é ASCII, e esse hint adicionaria um segmento ECI desnecessário.
            Map<EncodeHintType, Object> hints = Map.of(
                    EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.M,
                    EncodeHintType.MARGIN, 0);

            // Tamanho 0x0 pede a menor matriz possível: 1 pixel por módulo.
            return new QRCodeWriter().encode(payload, BarcodeFormat.QR_CODE, 0, 0, hints);
        } catch (WriterException e) {
            throw new IllegalStateException("Não foi possível gerar o QR Code do Pix", e);
        }
    }

    /** Converte a matriz em dados de caminho SVG (um retângulo por sequência de módulos pretos na linha). */
    private static String gerarPathData(BitMatrix matriz) {
        StringBuilder sb = new StringBuilder();

        for (int y = 0; y < matriz.getHeight(); y++) {
            int x = 0;
            while (x < matriz.getWidth()) {
                if (!matriz.get(x, y)) {
                    x++;
                    continue;
                }

                int inicio = x;
                while (x < matriz.getWidth() && matriz.get(x, y)) {
                    x++;
                }

                int tamanho = x - inicio;
                sb.append('M').append(inicio).append(' ').append(y)
                        .append('h').append(tamanho)
                        .append("v1h-").append(tamanho)
                        .append('z');
            }
        }

        return sb.toString();
    }

    /** Documento SVG completo, com fundo branco e margem de silêncio. */
    private static String gerarSvg(String payload) {
        BitMatrix matriz = gerarMatriz(payload);
        int lado = matriz.getWidth() + ZONA_SILENCIOSA * 2;

        return """
                <svg xmlns="http://www.w3.org/2000/svg" width="512" height="512" viewBox="0 0 %1$d %1$d" shape-rendering="crispEdges">
                  <rect width="%1$d" height="%1$d" fill="#FFFFFF"/>
                  <path transform="translate(%2$d %2$d)" d="%3$s" fill="#000000"/>
                </svg>
                """.formatted(lado, ZONA_SILENCIOSA, gerarPathData(matriz));
    }

    /**
     * Gera o arquivo "pix-qrcode.svg" na pasta de trabalho.
     * Uso: informe o código Pix copia e cola, entre aspas, como argumento.
     */
    static void main(String[] args) throws IOException {
        String pixCopiaECola = "00020101021126330014br.gov.bcb.pix0111017792113765204000053039865802BR5925DIEGO AUGUSTO BEZERRA PES6009SAO PAULO622905251M61DJD8W126RJ510EWWNSVK06304F58D";
        Path destino = Path.of("/images/pix-qrcode.svg");

        salvarSvg(pixCopiaECola, destino);
        System.out.println("Arquivo gerado: " + destino.toAbsolutePath());
    }

}