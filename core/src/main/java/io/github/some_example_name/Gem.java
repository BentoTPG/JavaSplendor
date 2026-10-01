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

    // ---- access by color name: red, blue, green, white, black, gold ----
    public int get(String color) {
        switch (color.toLowerCase()) {
            case "red": return redGem;
            case "blue": return blueGem;
            case "green": return greenGem;
            case "white": return whiteGem;
            case "black": return blackGem;
            case "gold": return goldGem;
            default: throw new IllegalArgumentException("Unknown color: " + color);
        }
    }

    public void add(String color, int amount) {
        switch (color.toLowerCase()) {
            case "red": redGem += amount; break;
            case "blue": blueGem += amount; break;
            case "green": greenGem += amount; break;
            case "white": whiteGem += amount; break;
            case "black": blackGem += amount; break;
            case "gold": goldGem += amount; break;
            default: throw new IllegalArgumentException("Unknown color: " + color);
        }
    }

    public void remove(String color, int amount) {
        if (get(color) < amount) {
            throw new IllegalStateException("Not enough " + color + " gems");
        }
        add(color, -amount);
    }

    public int total() {
        return redGem + blueGem + greenGem + whiteGem + blackGem + goldGem;
    }
}
