package fr.titan.tichu.model;

/**
 * User: Titan
 * Date: 29/03/14
 * Time: 11:47
 */
public class ValueCard extends Card{
    private  int value;
    private String color;

    public ValueCard(){
        super(CardType.VALUE);
    }

    public ValueCard(int value, String color) {
        super(CardType.VALUE);
        this.value = value;
        this.color = color;
    }

    public int getValue() {
        return this.value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }
}
