package io.github.some_example_name;

public class Card {

    private int id;
    private int level;
    private int point;

    private String color;

    private Gem cardCost;
    public Card(int id, int level, int point, String color,
                Gem cardCost) {
        this.id = id;
        this.level = level;
        this.point = point;
        this.color = color;

        this.cardCost = cardCost;
    }

    public int getId() {
        return id;
    }

    public int getLevel() {
        return level;
    }

    public int getPoint() {
        return point;
    }

    public String getColor() {
        return color;
    }

    public Gem getCardCost() {
        return cardCost;
    }

}