package io.github.some_example_name;

public class GameManager {

    private Game game;

    public GameManager(Game game) {
        this.game = game;
    }

    public void endTurn() {
        game.nextTurn();
    }

    public void takeRedGem() {
        Player player = game.getCurrentPlayer();
        Board board = game.getBoard();

        if (board.getGems().getRedGem() > 0) {

            board.getGems().removeRedGem(1);
            player.getGems().addRedGem(1);
        }
    }

    public void takeBlueGem() {
        Player player = game.getCurrentPlayer();
        Board board = game.getBoard();

        if (board.getGems().getBlueGem() > 0) {

            board.getGems().removeBlueGem(1);
            player.getGems().addBlueGem(1);
        }
    }

    public void takeGreenGem() {
        Player player = game.getCurrentPlayer();
        Board board = game.getBoard();

        if (board.getGems().getGreenGem() > 0) {

            board.getGems().removeGreenGem(1);
            player.getGems().addGreenGem(1);
        }
    }

    public void takeWhiteGem() {
        Player player = game.getCurrentPlayer();
        Board board = game.getBoard();

        if (board.getGems().getWhiteGem() > 0) {

            board.getGems().removeWhiteGem(1);
            player.getGems().addWhiteGem(1);
        }
    }

    public void takeBlackGem() {
        Player player = game.getCurrentPlayer();
        Board board = game.getBoard();

        if (board.getGems().getBlackGem() > 0) {

            board.getGems().removeBlackGem(1);
            player.getGems().addBlackGem(1);
        }
    }

    public void takeGoldGem() {
        Player player = game.getCurrentPlayer();
        Board board = game.getBoard();

        if (board.getGems().getGoldGem() > 0) {

            board.getGems().removeGoldGem(1);
            player.getGems().addGoldGem(1);
        }
    }

    public void buyCard(Card card) {

        Player player = game.getCurrentPlayer();

        if (canBuyCard(player, card)) {

            payCardCost(player, card);

            player.addCard(card);
            player.addScore(card.getPoint());

            game.getBoard().removeCardOnBoard(card);
        }
    }

    private boolean canBuyCard(Player player, Card card) {

        Gem playerGem = player.getGems();
        Gem cost = card.getCardCost();

        int redNeed = cost.getRedGem();
        int blueNeed = cost.getBlueGem();
        int greenNeed = cost.getGreenGem();
        int whiteNeed = cost.getWhiteGem();
        int blackNeed = cost.getBlackGem();

        int redHave = playerGem.getRedGem();
        int blueHave = playerGem.getBlueGem();
        int greenHave = playerGem.getGreenGem();
        int whiteHave = playerGem.getWhiteGem();
        int blackHave = playerGem.getBlackGem();

        return redHave >= redNeed
            && blueHave >= blueNeed
            && greenHave >= greenNeed
            && whiteHave >= whiteNeed
            && blackHave >= blackNeed;
    }

    private void payCardCost(Player player, Card card) {

        Gem playerGem = player.getGems();
        Gem cost = card.getCardCost();

        playerGem.removeRedGem(cost.getRedGem());
        playerGem.removeBlueGem(cost.getBlueGem());
        playerGem.removeGreenGem(cost.getGreenGem());
        playerGem.removeWhiteGem(cost.getWhiteGem());
        playerGem.removeBlackGem(cost.getBlackGem());
    }

    public Game getGame() {
        return game;
    }
}