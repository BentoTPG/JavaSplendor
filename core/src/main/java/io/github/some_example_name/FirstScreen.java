package io.github.some_example_name;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

/** First screen of the application. Displayed after the application is created. */
public class FirstScreen implements Screen {
	private SpriteBatch batch;
    private BitmapFont font;
    private Game game;
    private GameManager manager;
    @Override
    public void show() {
    	batch = new SpriteBatch();
        font = new BitmapFont();

        game = new Game();
        manager = new GameManager(game);
    }

    @Override
    public void render(float delta) {
    	Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
            manager.takeRedGem();
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
            manager.endTurn();
        }

        Player player = game.getCurrentPlayer();

        batch.begin();

        font.draw(batch,
                "Player: " + player.getName(),
                50, 400);

        font.draw(batch,
                "Red Gem: " + player.getGems().getRedGem(),
                50, 360);

        font.draw(batch,
                "Board Red Gem: "
                + game.getBoard().getGems().getRedGem(),
                50, 320);

        font.draw(batch,
                "SPACE = Take Red Gem",
                50, 260);

        font.draw(batch,
                "ENTER = End Turn",
                50, 230);

        batch.end();
    }

    @Override
    public void resize(int width, int height) {
        // If the window is minimized on a desktop (LWJGL3) platform, width and height are 0, which causes problems.
        // In that case, we don't resize anything, and wait for the window to be a normal size before updating.
        if(width <= 0 || height <= 0) return;

        // Resize your screen here. The parameters represent the new window size.
    }

    @Override
    public void pause() {
        // Invoked when your application is paused.
    }

    @Override
    public void resume() {
        // Invoked when your application is resumed after pause.
    }

    @Override
    public void hide() {
        // This method is called when another screen replaces this one.
    }

    @Override
    public void dispose() {
        // Destroy screen's assets here.
    }
}