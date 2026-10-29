package com.noahtnt2009.gallifreyan_chronicles.client.watermark;

public interface PixelSurface {
    int width();

    int height();

    int getArgb(int x, int y);

    void setArgb(int x, int y, int argb);
}
