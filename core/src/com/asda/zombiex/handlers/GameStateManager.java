package com.asda.zombiex.handlers;

import com.asda.zombiex.Game;
import com.asda.zombiex.states.GameState;
import com.asda.zombiex.states.Menu;
import com.asda.zombiex.states.Play;

import java.util.Stack;

/**
 * @author Skala
 */
public class GameStateManager {
    private Game game;

    private Stack<GameState> gameStates;

    public static final int MENU = 0;
    public static final int PLAY = 1;

    public GameStateManager(Game game) {
        this.game = game;
        gameStates = new Stack<GameState>();
        pushState(MENU);
    }

    public void update(float dt) {
        gameStates.peek().update(dt);
    }

    public void render() {
        gameStates.peek().render();
    }

    private GameState getState(int state) {
        if (state == MENU) return new Menu(this);
        if (state == PLAY) return new Play(this);
        return null;
    }

    public void setState(int state) {
        popState();
        pushState(state);
    }

    public void pushState(int state) {
        gameStates.push(getState(state));
    }

    public void popState() {
        GameState g = gameStates.pop();
        g.dispose();
    }

    public Game getGame() {
        return game;
    }

    public void setSinglePlayer() {
        ((Play) gameStates.peek()).setSinglePlayer();
    }

    public void setServer(String hostIp) {
        ((Play) gameStates.peek()).setServer(hostIp);
    }

    public void setClient(String connectIp) {
        ((Play) gameStates.peek()).setClient(connectIp);
    }
}
