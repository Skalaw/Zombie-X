package com.asda.zombiex.entities;

import com.asda.zombiex.Game;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.physics.box2d.Body;

/**
 * @author Skala
 */
public class Player extends B2DSprite {

    public Player(Body body) {
        super(body);

        Texture tex = Game.res.getTexture("jenkins");
        TextureRegion sprite = new TextureRegion(tex, 0, 0, 25, 50);

        setAnimation(sprite, 1 / 12f);
    }
}
