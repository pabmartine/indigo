package com.martinia.indigo.common.util;

import com.sun.net.httpserver.HttpServer;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.net.InetSocketAddress;
import javax.imageio.ImageIO;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;

class AuthorPhotoDownloadTest {
    @Test void missingPhotoIsDifferentFromAServerFailureAndValidPhotosAreDecoded() throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        ImageIO.write(new BufferedImage(10, 20, BufferedImage.TYPE_INT_RGB), "png", bytes);
        byte[] photo = bytes.toByteArray();
        server.createContext("/photo", exchange -> {
            exchange.sendResponseHeaders(200, photo.length);
            try (var output = exchange.getResponseBody()) { output.write(photo); }
        });
        server.createContext("/missing", exchange -> { exchange.sendResponseHeaders(404, -1); exchange.close(); });
        server.createContext("/unavailable", exchange -> { exchange.sendResponseHeaders(503, -1); exchange.close(); });
        server.start();
        try {
            ImageUtils images = new ImageUtils();
            String base = "http://127.0.0.1:" + server.getAddress().getPort();
            assertThat(images.getBase64AuthorUrl(base + "/missing")).isNull();
            byte[] downloaded = java.util.Base64.getDecoder().decode(images.getBase64AuthorUrl(base + "/photo"));
            assertThat(ImageIO.read(new java.io.ByteArrayInputStream(downloaded))).isNotNull();
            assertThatThrownBy(() -> images.getBase64AuthorUrl(base + "/unavailable"))
                    .isInstanceOf(IllegalStateException.class)
                    .hasCauseInstanceOf(org.springframework.web.client.RestClientResponseException.class);
        } finally { server.stop(0); }
    }

    @Test void placeholderAndBlankUrlsAreNotPhotos() {
        ImageUtils images = new ImageUtils();
        assertThat(images.getBase64AuthorUrl(" ")).isNull();
        assertThat(images.getBase64AuthorUrl("https://s.gr-assets.com/assets/nophoto/user/u_200x266-e183445fd1a1b5cc7075bb1cf7043306.png"))
                .isNull();
    }
}
