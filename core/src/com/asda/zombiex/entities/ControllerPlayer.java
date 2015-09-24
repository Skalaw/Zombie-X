package com.asda.zombiex.entities;

import com.asda.zombiex.Game;
import com.asda.zombiex.handlers.InputKeys;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.box2d.Body;

/**
 * @author Skala
 */
public class ControllerPlayer {
    final static float MAX_VELOCITY_X = 4f;

    private Player player;
    private Body playerBody;
    private OrthographicCamera cam;

    // TODO: to improvement change Texture to TextureRegion
    private Texture analog;
    private Texture buttonJump;
    private Vector3 vec;

    private int buttonJumpX; // TODO: create class button

    public ControllerPlayer(Player player, OrthographicCamera cam) {
        this.player = player;
        playerBody = player.getBody();
        this.cam = cam;

        analog = Game.res.getTexture("analog");
        buttonJump = Game.res.getTexture("button");
        buttonJumpX = Game.V_WIDTH - buttonJump.getWidth();

        vec = new Vector3();
    }

    public void handleInput() {
        vec.set(InputKeys.x, InputKeys.y, 0);
        cam.unproject(vec);

        Vector2 vel = playerBody.getLinearVelocity();

        // control max speed horizontally
        if (Math.abs(vel.x) > MAX_VELOCITY_X) {
            vel.x = Math.signum(vel.x) * MAX_VELOCITY_X;
            playerBody.setLinearVelocity(vel.x, vel.y);
        }

        // release slowly when idle control horizontally
        if (!InputKeys.isPressed() || !(vec.y > 0 && vec.y < analog.getHeight() && vec.x > 0 && vec.x <= analog.getWidth())) {
            playerBody.setLinearVelocity(vel.x * 0.9f, vel.y);
        }

        // set moving left or right
        if (InputKeys.isDown()) {
            float posX = playerBody.getPosition().x;
            float posY = playerBody.getPosition().y;

            if (vec.y > 0 && vec.y < analog.getHeight()) {
                if (vec.x > 0 && vec.x <= analog.getWidth() / 2) {
                    if (vel.x > -MAX_VELOCITY_X) {
                        playerBody.applyLinearImpulse(-0.3f, 0, posX, posY, true);
                    }
                } else if (vec.x > analog.getWidth() / 2 && vec.x < analog.getWidth()) {
                    if (vel.x < MAX_VELOCITY_X) {
                        playerBody.applyLinearImpulse(0.3f, 0, posX, posY, true);
                    }
                }
            }
        }

        // jump
        if (InputKeys.isPressed()) {
            if (vec.x > buttonJumpX && vec.x < buttonJumpX + buttonJump.getWidth()
                    && vec.y > 0 && vec.y < buttonJump.getHeight()) {
                playerBody.setLinearVelocity(vel.x, 0);
                playerBody.applyForceToCenter(0, 175, true);
            }
        }
    }

    public void render(SpriteBatch sb) {
        Color c = sb.getColor();
        sb.setColor(c.r, c.g, c.b, 0.3f);

        sb.begin();
        sb.draw(analog, 0, 0);
        sb.draw(buttonJump, buttonJumpX, 0);
        sb.end();

        sb.setColor(c.r, c.g, c.b, 1f);
    }
}
