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
    private final static float INTERFACE_TRANSPARENT = 0.3f;

    private OrthographicCamera cam;

    // TODO: to improvement change Texture to TextureRegion
    private Texture buttonAnalog;
    private Texture buttonJump;
    private Texture buttonFire;
    private Texture buttonChangePlayer;
    private Vector3 vec;

    private int buttonJumpX; // TODO: create class button
    private int buttonFireY;
    private int buttonChangePlayerY;

    public ControllerPlayer(OrthographicCamera cam) {
        this.cam = cam;

        buttonAnalog = Game.res.getTexture("analog");
        buttonJump = Game.res.getTexture("button");
        buttonJumpX = Game.V_WIDTH - buttonJump.getWidth();
        buttonFire = Game.res.getTexture("fire");
        buttonFireY = buttonFire.getHeight();
        buttonChangePlayer = Game.res.getTexture("button");
        buttonChangePlayerY = buttonFireY + buttonChangePlayer.getHeight();

        vec = new Vector3();
    }

    public void render(SpriteBatch sb) {
        Color c = sb.getColor();
        sb.setColor(c.r, c.g, c.b, INTERFACE_TRANSPARENT);

        sb.begin();
        sb.draw(buttonAnalog, 0, 0);
        sb.draw(buttonJump, buttonJumpX, 0);
        sb.draw(buttonFire, buttonJumpX, buttonFireY);
        sb.draw(buttonChangePlayer, buttonJumpX, buttonChangePlayerY);
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

    public boolean isButtonFireClicked() {
        return isButtonFireClicked(InputController.inputKeys[0]) || isButtonFireClicked(InputController.inputKeys[1]);
    }

    private boolean isButtonFireClicked(InputKeys inputKeys) {
        if (inputKeys.isDown()) {
            vec.set(inputKeys.x, inputKeys.y, 0);
            cam.unproject(vec);
            if (vec.x > buttonJumpX && vec.x < buttonJumpX + buttonFire.getWidth()
                    && vec.y > buttonFireY && vec.y < buttonFireY + buttonJump.getHeight()) {
                return true;
            }
        }

        return false;
    }

    public boolean isButtonChangePlayerClicked() {
        return isButtonChangePlayerClicked(InputController.inputKeys[0]) || isButtonChangePlayerClicked(InputController.inputKeys[1]);
    }

    private boolean isButtonChangePlayerClicked(InputKeys inputKeys) {
        if (inputKeys.isPressed()) {
            vec.set(inputKeys.x, inputKeys.y, 0);
            cam.unproject(vec);
            if (vec.x > buttonJumpX && vec.x < buttonJumpX + buttonChangePlayer.getWidth()
                    && vec.y > buttonChangePlayerY && vec.y < buttonChangePlayerY + buttonChangePlayer.getHeight()) {
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
        return !inputKeys.isPressed() || !(vec.y > 0 && vec.y < buttonAnalog.getHeight() && vec.x > 0 && vec.x <= buttonAnalog.getWidth());
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

            if (vec.y > 0 && vec.y < buttonAnalog.getHeight()) {
                if (vec.x > 0 && vec.x <= buttonAnalog.getWidth() / 2) {
                    return -(1 - calcPercentValue(0, buttonAnalog.getWidth() / 2, vec.x));
                } else if (vec.x > buttonAnalog.getWidth() / 2 && vec.x < buttonAnalog.getWidth()) {
                    return calcPercentValue(buttonAnalog.getWidth() / 2, buttonAnalog.getWidth(), vec.x);
                }
            }
        }

        return 0f;
    }

    public float getAnalogAngle() {
        float angle1 = getAnalogAngle(InputController.inputKeys[0]);
        float angle2 = getAnalogAngle(InputController.inputKeys[1]);

        return angle1 > angle2 ? angle1 : angle2;
    }

    private float getAnalogAngle(InputKeys inputKeys) {
        if (inputKeys.isDown()) {
            vec.set(inputKeys.x, inputKeys.y, 0);
            cam.unproject(vec);

            if (vec.y > 0 && vec.y < buttonAnalog.getHeight() && vec.x > 0 && vec.x <= buttonAnalog.getWidth()) {
                float analogCenterX = buttonAnalog.getWidth() / 2;
                float analogCenterY = buttonAnalog.getHeight() / 2;

                float angle = (float) Math.toDegrees(Math.atan2(vec.x - analogCenterX, vec.y - analogCenterY));
                angle += 90;
                if (angle < 0) {
                    angle += 360;
                }

                return angle;
            }
        }

        return 0f;
    }

    /**
     * @param left  value from left side
     * @param right value from right side
     * @param value calculated value
     * @return value in percent
     */

    private float calcPercentValue(float left, float right, float value) {
        right -= left;
        value -= left;

        return value / right;
    }
}
