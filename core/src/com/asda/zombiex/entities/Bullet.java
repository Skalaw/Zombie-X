package com.asda.zombiex.entities;

import com.asda.zombiex.Game;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.physics.box2d.Body;

/**
 * @author Skala
 */
public class Bullet extends B2DSprite {
    public static final float POWER_BULLET = 35;
    private final String nameOwnerBullet;

    public Bullet(Body body, String nameOwnerBullet) {
        super(body);
        this.nameOwnerBullet = nameOwnerBullet;

        Texture tex = Game.res.getTexture("bullet");
        TextureRegion sprite = new TextureRegion(tex, 0, 0, 8, 8);

        setAnimation(sprite, 1 / 12f);
    }

    public String getNameOwnerBullet() {
        return nameOwnerBullet;
    }
}
