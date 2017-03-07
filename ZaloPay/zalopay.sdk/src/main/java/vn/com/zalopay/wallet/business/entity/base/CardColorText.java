package vn.com.zalopay.wallet.business.entity.base;

public class CardColorText {
    public int defaultColor;
    public int highlineColor;
    public int selectedColor;

    public CardColorText(int pDefaultColor, int pHighlineColor, int pSelectedColor) {
        this.defaultColor = pDefaultColor;
        this.highlineColor = pHighlineColor;
        this.selectedColor = pSelectedColor;
    }
}
