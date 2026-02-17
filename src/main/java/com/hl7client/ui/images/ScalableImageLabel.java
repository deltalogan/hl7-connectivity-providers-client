package com.hl7client.ui.images;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ScalableImageLabel extends JLabel {

    private static final Logger logger = Logger.getLogger(ScalableImageLabel.class.getName());

    private BufferedImage image;
    private boolean gifMode;

    public void setImage(URL imageUrl) {
        image = null;
        gifMode = false;
        setIcon(null);

        if (imageUrl == null) {
            repaint();
            return;
        }

        String path = imageUrl.getPath().toLowerCase();

        // -------------------------------------------------
        // GIF → delegar a Swing
        // -------------------------------------------------
        if (path.endsWith(".gif")) {
            gifMode = true;
            setIcon(new ImageIcon(imageUrl));
            return;
        }

        // -------------------------------------------------
        // Imagen estática
        // -------------------------------------------------
        try {
            image = ImageIO.read(imageUrl);
        } catch (Exception e) {
            // Reemplazamos printStackTrace por logging robusto
            logger.log(Level.SEVERE, "No se pudo cargar la imagen desde: {0}", new Object[]{imageUrl, e});
            // O versión más simple sin stack trace completo:
            // logger.log(Level.SEVERE, "Error al leer imagen de URL: " + imageUrl, e);
        }

        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        if (gifMode || image == null) {
            return;
        }

        Graphics2D g2 = (Graphics2D) g.create();
        try {
            g2.setRenderingHint(
                    RenderingHints.KEY_INTERPOLATION,
                    RenderingHints.VALUE_INTERPOLATION_BILINEAR
            );

            int w = getWidth();
            int h = getHeight();

            double imgRatio = (double) image.getWidth() / image.getHeight();
            double cmpRatio = (double) w / h;

            int drawW, drawH;
            if (cmpRatio > imgRatio) {
                drawH = h;
                drawW = (int) (h * imgRatio);
            } else {
                drawW = w;
                drawH = (int) (w / imgRatio);
            }

            int x = (w - drawW) / 2;
            int y = (h - drawH) / 2;

            g2.drawImage(image, x, y, drawW, drawH, null);
        } finally {
            g2.dispose();
        }
    }
}