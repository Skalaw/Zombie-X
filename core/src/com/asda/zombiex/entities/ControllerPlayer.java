package com.asda.zombiex.entities;

import com.asda.zombiex.Game;
import com.asda.zombiex.handlers.InputController;
import com.asda.zombiex.handlers.InputKeys;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector3;

/**
 * @author Skala
 */
public class ControllerPlayer {
    private OrthographicCamera cam;

    // TODO: to improvement change Texture to TextureRegion
    private Texture analog;
    private Texture buttonJump;
    private Vector3 vec;

    private int buttonJumpX; // TODO: create class button

    public ControllerPlayer(OrthographicCamera cam) {
        this.cam = cam;

        analog = Game.res.getTexture("analog");
        buttonJump = Game.res.getTexture("button");
        buttonJumpX = Game.V_WIDTH - buttonJump.getWidth();

        vec = new Vector3();
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

    public boolean isButtonJumpClicked() {
        return isButtonJumpClicked(InputController.inputKeys[0]) || isButtonJumpClicked(InputController.inputKeys[1]);
    }

    private boolean isButtonJumpClicked(InputKeys inputKeys) {
        if (inputKeys.isPressed()) {
            vec.set(inputKeys.x, inputKeys.y, 0);
            cam.unproject(vec);
            if (vec.x > buttonJumpX && vec.x < buttonJumpX + buttonJump.getWidth()
                    && vec.y > 0 && vec.y < buttonJump.getHeight()) {
                return true;
            }
        }

        return false;
    }

    public boolean isAnalogDown() {
        boolean firstPointer = isAnalogDown(InputController.inputKeys[0]);
        boolean secondPointer = isAnalogDown(InputController.inputKeys[1]);

        return !(firstPointer && secondPointer);
    }

    private boolean isAnalogDown(InputKeys inputKeys) {
        vec.set(inputKeys.x, inputKeys.y, 0);
        cam.unproject(vec);
        return !inputKeys.isPressed() || !(vec.y > 0 && vec.y < analog.getHeight() && vec.x > 0 && vec.x <= analog.getWidth());
    }

    /**
     * @return Intensity in percentage
     */

    public float getAnalogIntensity() {
        float intensity1 = getAnalogIntensity(InputController.inputKeys[0]);
        float intensity2 = getAnalogIntensity(InputController.inputKeys[1]);

        return Math.abs(intensity1) > Math.abs(intensity2) ? intensity1 : intensity2;
    }

    private float getAnalogIntensity(InputKeys inputKeys) {
        if (inputKeys.isDown()) {
            vec.set(inputKeys.x, inputKeys.y, 0);
            cam.unproject(vec);

            if (vec.y > 0 && vec.y < analog.getHeight()) {
                if (vec.x > 0 && vec.x <= analog.getWidth() / 2) {
                    return -(1 - calcPercentValue(0, analog.getWidth() / 2, vec.x));
                } else if (vec.x > analog.getWidth() / 2 && vec.x < analog.getWidth()) {
                    return calcPercentValue(analog.getWidth() / 2, analog.getWidth(), vec.x);
                }
            }
        }

        return 0f;
    }

    private float calcPercentValue(float left, float right, float value) {
        right -= left;
        value -= left;

        return value / right;
    }
}
