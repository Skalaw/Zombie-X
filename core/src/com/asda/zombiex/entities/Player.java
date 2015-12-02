package com.asda.zombiex.entities;

import com.asda.zombiex.Game;
import com.asda.zombiex.handlers.B2DVars;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.MassData;
import com.badlogic.gdx.physics.box2d.World;

import static com.asda.zombiex.handlers.B2DVars.PPM;

/**
 * @author Skala
 */
public class Player extends B2DSprite {
    public final static float HEALTH_MAX = 100;
    private final static float SHOT_RATE = 0.3f;

    private float health = HEALTH_MAX;

    private String name = "";
    private String nickname = "";
    private int scoreKilling = 0;
    private int scoreDead = 0;

    private Viewfinder viewfinder;
    private float shotTime = 0f;
    private float actualTime = 0;

    public Player(Body body, int countPlayers) {
        super(body);

        Texture tex;
        if (countPlayers % 3 == 0) {
            tex = Game.res.getTexture("jenkins");
        } else if (countPlayers % 3 == 1) {
            tex = Game.res.getTexture("jenkins2");
        } else {
            tex = Game.res.getTexture("jenkins3");
        }
        TextureRegion sprite = new TextureRegion(tex, 0, 0, 25, 50);

        viewfinder = new Viewfinder(body, 25, 50);

        setAnimation(sprite, 1 / 12f);
    }

    @Override
    public void update(float dt) {
        super.update(dt);

        actualTime += dt;
    }

    @Override
    public void render(SpriteBatch sb) {
        if (!isDead()) {
            super.render(sb);

            viewfinder.render(sb);
        }
    }

    /**
     * Set moving left or right
     *
     * @param intensity in percentage
     */

    public void moving(float intensity) {
        float impulse = intensity * 0.4f;

        Vector2 pos = body.getPosition();
        body.applyLinearImpulse(impulse, 0, pos.x, pos.y, true);
    }

    /**
     * Release slowly when idle control horizontally
     */

    public void braking() {
        Vector2 vel = body.getLinearVelocity();
        body.setLinearVelocity(vel.x * 0.9f, vel.y);
    }

    /**
     * Jump player
     */

    public void jump() {
        Vector2 vel = body.getLinearVelocity();
        body.setLinearVelocity(vel.x, 0);
        body.applyForceToCenter(0, 175, true);
    }

    /**
     * Shoot player
     */

    public Bullet shot(World world) {
        shotTime = actualTime;

        float power = 4.5f;
        float radian = viewfinder.getRadian();
        float dx = (float) -Math.cos(radian) * power;
        float dy = (float) Math.sin(radian) * power;

        float posX = body.getPosition().x + (float) -Math.cos(radian) * 33 / PPM; // distance bullet from player TODO: fix
        float posY = body.getPosition().y + (float) Math.sin(radian) * 33 / PPM; // distance bullet from player TODO: fix

        BodyDef bdef = new BodyDef();
        bdef.position.set(posX, posY);

        bdef.type = BodyDef.BodyType.DynamicBody;

        Body body = world.createBody(bdef);

        CircleShape shape = new CircleShape();
        shape.setRadius(4f / PPM);
        FixtureDef fdef = new FixtureDef();
        fdef.shape = shape;
        fdef.density = 1f;
        fdef.restitution = 0.2f;
        fdef.filter.categoryBits = B2DVars.BIT_BULLET;
        fdef.filter.maskBits = B2DVars.BIT_RED_BLOCK | B2DVars.BIT_GREEN_BLOCK | B2DVars.BIT_BLUE_BLOCK | B2DVars.BIT_YELLOW_BLOCK | B2DVars.BIT_BORDER | B2DVars.BIT_PLAYER;
        body.createFixture(fdef).setUserData("bullet");
        shape.dispose();

        Bullet bullet = new Bullet(body, name);
        body.setUserData(bullet);

        MassData md = body.getMassData();
        md.mass = 1f;
        body.setMassData(md);
        body.applyLinearImpulse(dx, dy, posX, posY, true);

        return bullet;
    }

    public boolean canShot() {
        return actualTime > shotTime + SHOT_RATE;
    }

    public void setViewfinderRadian(float radian) {
        viewfinder.setRadian(radian);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public float getHealth() {
        return health;
    }

    public void setHealth(float health) {
        this.health = health;
    }

    public void loseHealth(float loseHealth) {
        this.health -= loseHealth;
    }

    public boolean isDead() {
        return health <= 0;
    }

    public int getScoreKilling() {
        return scoreKilling;
    }

    public void setScoreKilling(int scoreKilling) {
        this.scoreKilling = scoreKilling;
    }

    public void incScoreKilling() {
        scoreKilling++;
    }

    public void decScoreKilling() {
        scoreKilling--;
    }

    public int getScoreDead() {
        return scoreDead;
    }

    public void setScoreDead(int scoreDead) {
        this.scoreDead = scoreDead;
    }

    public void incScoreDead() {
        scoreDead++;
    }
}
