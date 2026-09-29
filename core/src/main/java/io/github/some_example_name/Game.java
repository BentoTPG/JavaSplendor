package io.github.some_example_name;

import io.github.some_example_name.Board;

public class Game {

    private Player[] players;
    private Board board;
    private int currentPlayer;

    public Game() {

        players = new Player[4];

        players[0] = new Player("Player 1");
        players[1] = new Player("Player 2");
        players[2] = new Player("Player 3");
        players[3] = new Player("Player 4");

        board = new Board();

        currentPlayer = 0;
    }

    public Player getCurrentPlayer() {
        return players[currentPlayer];
    }

    public Board getBoard() {
        return board;
    }

    public void nextTurn() {

        currentPlayer++;

        if (currentPlayer >= players.length) {
            currentPlayer = 0;
        }
    }
}