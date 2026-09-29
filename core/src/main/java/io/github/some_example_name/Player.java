package io.github.some_example_name;

import java.util.ArrayList;

public class Player {

	private String name;
    private int score;

    private Gem playerGem;

    private ArrayList<Card> cards;
    private ArrayList<Card> reservedCards;
    private ArrayList<Noble> nobles;

    public Player(String name) {
        this.name = name;
        score = 0;

        playerGem = new Gem();

        cards = new ArrayList<>();
        reservedCards = new ArrayList<>();
        nobles = new ArrayList<>();
    }


    public void addCard(Card card) {
        cards.add(card);
    }

    public void reserveCard(Card card) {
        reservedCards.add(card);
    }

    public void addNoble(Noble noble) {
        nobles.add(noble);
    }

    public void addScore(int point) {
        score += point;
    }

    public String getName() {
        return name;
    }

    public int getScore() {
        return score;
    }


    public ArrayList<Card> getCards() {
        return cards;
    }

    public ArrayList<Card> getReservedCards() {
        return reservedCards;
    }

    public ArrayList<Noble> getNobles() {
        return nobles;
    }
    public Gem getGems() {
    	return playerGem;
    }
}