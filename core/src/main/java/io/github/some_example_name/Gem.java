package io.github.some_example_name;

public class Gem {

    private int redGem;
    private int blueGem;
    private int greenGem;
    private int whiteGem;
    private int blackGem;
    private int goldGem;

    public Gem() {
    	redGem = 0;
        blueGem = 0;
        greenGem = 0;
        whiteGem = 0;
        blackGem = 0;
        goldGem = 0;
    }
    public Gem(int redGem, int blueGem, int greenGem,
               int whiteGem, int blackGem, int goldGem) {

        this.redGem = redGem;
        this.blueGem = blueGem;
        this.greenGem = greenGem;
        this.whiteGem = whiteGem;
        this.blackGem = blackGem;
        this.goldGem = goldGem;
    }


    
    public void addRedGem(int amount) {
        redGem += amount;
    }

    public void addBlueGem(int amount) {
        blueGem += amount;
    }

    public void addGreenGem(int amount) {
        greenGem += amount;
    }

    public void addWhiteGem(int amount) {
        whiteGem += amount;
    }

    public void addBlackGem(int amount) {
        blackGem += amount;
    }

    public void addGoldGem(int amount) {
        goldGem += amount;
    }


    
    public void removeRedGem(int amount) {
        redGem -= amount;
    }

    public void removeBlueGem(int amount) {
        blueGem -= amount;
    }

    public void removeGreenGem(int amount) {
        greenGem -= amount;
    }

    public void removeWhiteGem(int amount) {
        whiteGem -= amount;
    }

    public void removeBlackGem(int amount) {
        blackGem -= amount;
    }

    public void removeGoldGem(int amount) {
        goldGem -= amount;
    }

    
    public int getRedGem() {
        return redGem;
    }

    public int getBlueGem() {
        return blueGem;
    }

    public int getGreenGem() {
        return greenGem;
    }

    public int getWhiteGem() {
        return whiteGem;
    }

    public int getBlackGem() {
        return blackGem;
    }

    public int getGoldGem() {
        return goldGem;
    }
}