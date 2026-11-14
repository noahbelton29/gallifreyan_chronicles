package uk.nbforge.gallifreyan_chronicles.client.protection.watermark;

public interface PixelSurface {
    int width();

    int height();

    int getArgb(int x, int y);

    void setArgb(int x, int y, int argb);
}
