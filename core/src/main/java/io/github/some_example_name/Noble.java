package io.github.some_example_name;

public class Noble {

    private int id;
    private int point;

    private int redRequirement;
    private int blueRequirement;
    private int greenRequirement;
    private int whiteRequirement;
    private int blackRequirement;

    public Noble(int id, int point,
                 int redRequirement,
                 int blueRequirement,
                 int greenRequirement,
                 int whiteRequirement,
                 int blackRequirement) {

        this.id = id;
        this.point = point;

        this.redRequirement = redRequirement;
        this.blueRequirement = blueRequirement;
        this.greenRequirement = greenRequirement;
        this.whiteRequirement = whiteRequirement;
        this.blackRequirement = blackRequirement;
    }

    public int getId() {
        return id;
    }

    public int getPoint() {
        return point;
    }

    public int getRedRequirement() {
        return redRequirement;
    }

    public int getBlueRequirement() {
        return blueRequirement;
    }

    public int getGreenRequirement() {
        return greenRequirement;
    }

    public int getWhiteRequirement() {
        return whiteRequirement;
    }

    public int getBlackRequirement() {
        return blackRequirement;
    }
}