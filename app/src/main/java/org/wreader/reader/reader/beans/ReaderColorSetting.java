package org.wreader.reader.reader.beans;

public class ReaderColorSetting {
    public final int backgroundColor;

    public final boolean isLightBackground;

    public final int textColorPrimary;

    public final int textColorSecondary;

    public final int dividerColor;

    public final int speakingBackgroundColor;

    public ReaderColorSetting(int backgroundColor, boolean isLightBackground,
                              int textColorPrimary, int textColorSecondary,
                              int dividerColor) {
        this.backgroundColor = backgroundColor;
        this.isLightBackground = isLightBackground;
        this.textColorPrimary = textColorPrimary;
        this.textColorSecondary = textColorSecondary;
        this.dividerColor = dividerColor;
        this.speakingBackgroundColor = 0xffeeee11;
    }
}
