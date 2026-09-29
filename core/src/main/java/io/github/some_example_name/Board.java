package io.github.some_example_name;

import java.util.ArrayList;

public class Board {
	private Gem gems;

	private Deck level1;
	private Deck level2;
	private Deck level3;
	
    private ArrayList<Card> level1Cards;
    private ArrayList<Card> level2Cards;
    private ArrayList<Card> level3Cards;

	private ArrayList<Card> cardsOnBoard;
	private ArrayList<Noble> nobles;
	public Board() {
        gems = new Gem(7, 7, 7, 7, 7, 5);

        level1 = new Deck();
        level2 = new Deck();
        level3 = new Deck();

        level1Cards = new ArrayList<>();
        level2Cards = new ArrayList<>();
        level3Cards = new ArrayList<>();

        nobles = new ArrayList<>();
	}
	public void addCardOnBoard(Card card) {

        if (card.getLevel() == 1) {
            level1Cards.add(card);
        }

        if (card.getLevel() == 2) {
            level2Cards.add(card);
        }

        if (card.getLevel() == 3) {
            level3Cards.add(card);
        }
    }
	public void removeCardOnBoard(Card card) {

        if (card.getLevel() == 1) {
            level1Cards.remove(card);
        }

        if (card.getLevel() == 2) {
            level2Cards.remove(card);
        }

        if (card.getLevel() == 3) {
            level3Cards.remove(card);
        }
    }
	 public void addNoble(Noble noble) {
	        nobles.add(noble);
	    }

	    public void removeNoble(Noble noble) {
	        nobles.remove(noble);
	    }

	    public Gem getGems() {
	        return gems;
	    }

	    public Deck getLevel1() {
	        return level1;
	    }

	    public Deck getLevel2() {
	        return level2;
	    }

	    public Deck getLevel3() {
	        return level3;
	    }
	    public ArrayList<Card> getLevel1Cards() {
	        return level1Cards;
	    }

	    public ArrayList<Card> getLevel2Cards() {
	        return level2Cards;
	    }

	    public ArrayList<Card> getLevel3Cards() {
	        return level3Cards;
	    }

	    public ArrayList<Noble> getNobles() {
	        return nobles;
	    }
}
