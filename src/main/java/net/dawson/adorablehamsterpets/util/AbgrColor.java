package net.dawson.adorablehamsterpets.util;

/** Utilities for the ABGR pixel layout used by {@code NativeImage}. */
public final class AbgrColor {
    private AbgrColor() {
    }

    public static int color(int alpha, int blue, int green, int red) {
        return (alpha & 0xff) << 24 | (blue & 0xff) << 16 | (green & 0xff) << 8 | red & 0xff;
    }

    public static int alpha(int color) {
        return color >>> 24;
    }

    public static int red(int color) {
        return color & 0xff;
    }

    public static int green(int color) {
        return color >> 8 & 0xff;
    }

    public static int blue(int color) {
        return color >> 16 & 0xff;
    }
}
