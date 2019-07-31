package org.wreader.reader.reader;

class ReaderColorSetting {
    final int backgroundColor;

    final boolean isLightBackground;

    final int textColorPrimary;

    final int textColorSecondary;

    final int dividerColor;

    ReaderColorSetting(int backgroundColor, boolean isLightBackground,
                       int textColorPrimary, int textColorSecondary,
                       int dividerColor) {
        this.backgroundColor = backgroundColor;
        this.isLightBackground = isLightBackground;
        this.textColorPrimary = textColorPrimary;
        this.textColorSecondary = textColorSecondary;
        this.dividerColor = dividerColor;
    }
}
