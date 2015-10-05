package com.asda.zombiex.states;

import com.asda.zombiex.Game;
import com.asda.zombiex.entities.Bullet;
import com.asda.zombiex.entities.ControllerPlayer;
import com.asda.zombiex.entities.Player;
import com.asda.zombiex.handlers.B2DVars;
import com.asda.zombiex.handlers.GameContactListener;
import com.asda.zombiex.handlers.GameStateManager;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.ParticleEffect;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.ChainShape;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.MassData;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Array;

import static com.asda.zombiex.handlers.B2DVars.PPM;

/**
 * @author Skala
 */
public class Play extends GameState {
    private World world;
    private GameContactListener cl;

    private TiledMap map;
    private int tileMapWidth;
    private int tileMapHeight;
    private float tileSize;
    private OrthogonalTiledMapRenderer mapRenderer;

    private ControllerPlayer controllerPlayer;
    private Player player;
    private Array<Bullet> bullets;
    private Array<ParticleEffect> destroyBulletEffect;

    public Play(GameStateManager gsm) {
        super(gsm);

        createWorld();
        createMap();
        bullets = new Array<Bullet>();
        createPlayer();
        controllerPlayer = new ControllerPlayer(hudCam);

        destroyBulletEffect = new Array<ParticleEffect>();
    }

    private void createWorld() {
        world = new World(new Vector2(0f, -5f), true);
        cl = new GameContactListener();
        world.setContactListener(cl);
    }

    private void createMap() {
        TmxMapLoader tmxMapLoad = new TmxMapLoader();
        map = tmxMapLoad.load("maps/board.tmx");

        mapRenderer = new OrthogonalTiledMapRenderer(map);
        tileMapWidth = (Integer) map.getProperties().get("width");
        tileMapHeight = (Integer) map.getProperties().get("height");
        tileSize = (Integer) map.getProperties().get("tilewidth");
        cam.setBounds(0, tileMapWidth * tileSize, 0, tileMapHeight * tileSize);

        TiledMapTileLayer layer;
        layer = (TiledMapTileLayer) map.getLayers().get("red");
        createBlocks(layer, B2DVars.BIT_RED_BLOCK);
        layer = (TiledMapTileLayer) map.getLayers().get("green");
        createBlocks(layer, B2DVars.BIT_GREEN_BLOCK);
        layer = (TiledMapTileLayer) map.getLayers().get("blue");
        createBlocks(layer, B2DVars.BIT_BLUE_BLOCK);
        layer = (TiledMapTileLayer) map.getLayers().get("yellow");
        createBlocks(layer, B2DVars.BIT_YELLOW_BLOCK);
    }

    private void createBlocks(TiledMapTileLayer layer, short bits) {
        BodyDef bdef = new BodyDef();
        FixtureDef fdef = new FixtureDef();

        float sizeTileB2D = tileSize / 2 / PPM;

        ChainShape cs = new ChainShape();
        float vectors[] = {
                -sizeTileB2D, -sizeTileB2D,
                -sizeTileB2D, sizeTileB2D,
                sizeTileB2D, sizeTileB2D,
                sizeTileB2D, -sizeTileB2D};
        cs.createLoop(vectors);

        // go through all the cells in the layer
        int height = layer.getHeight();
        int width = layer.getWidth();
        for (int row = 0; row < height; row++) {
            for (int col = 0; col < width; col++) {
                TiledMapTileLayer.Cell cell = layer.getCell(col, row);
                if (cell == null || cell.getTile() == null) {
                    continue;
                }

                bdef.type = BodyDef.BodyType.StaticBody;
                bdef.position.set((col + 0.5f) * tileSize / PPM, (row + 0.5f) * tileSize / PPM);
                fdef.friction = 0;
                fdef.shape = cs;
                fdef.filter.categoryBits = bits;
                fdef.filter.maskBits = B2DVars.BIT_PLAYER | B2DVars.BIT_BULLET;
                world.createBody(bdef).createFixture(fdef).setUserData("block");
            }
        }

        cs.dispose();
    }

    private void createPlayer() {
        BodyDef bdef = new BodyDef();
        bdef.position.set(50 / PPM, (tileMapHeight - 4) * tileSize / PPM);
        bdef.type = BodyDef.BodyType.DynamicBody;
        bdef.fixedRotation = true;

        Body body = world.createBody(bdef);

        PolygonShape shape = new PolygonShape();
        shape.setAsBox(12.5f / PPM, 25f / PPM);
        FixtureDef fdef = new FixtureDef();
        fdef.shape = shape;
        fdef.density = 1f;
        fdef.friction = 0;
        fdef.filter.categoryBits = B2DVars.BIT_PLAYER;
        fdef.filter.maskBits = B2DVars.BIT_RED_BLOCK | B2DVars.BIT_GREEN_BLOCK | B2DVars.BIT_BLUE_BLOCK | B2DVars.BIT_YELLOW_BLOCK;
        body.createFixture(fdef).setUserData("player");
        shape.dispose();

        player = new Player(body);
        body.setUserData(player);

        MassData md = body.getMassData();
        md.mass = 1f;
        body.setMassData(md);
    }

    @Override
    public void handleInput() {
        if (!controllerPlayer.isAnalogDown()) {
            player.braking();
        }

        float intensity = controllerPlayer.getAnalogIntensity();
        if (intensity != 0f) {
            player.moving(intensity);
        }

        float angle = controllerPlayer.getAnalogAngle();
        if (angle != 0f) {
            float radian = angle * (float) Math.PI / 180;
            player.setViewfinderRadian(radian);
        }

        if (controllerPlayer.isButtonJumpClicked()) {
            player.jump();
        }

        if (controllerPlayer.isButtonFireClicked()) {
            if (player.canShot()) {
                Bullet bullet = player.shot(world);
                bullets.add(bullet);
            }
        }
    }

    @Override
    public void update(float dt) {
        handleInput();
        world.step(Game.STEP, 1, 1);

        removeBullets();
        removeEndedDestroyBulletEffects();

        player.update(dt);
        for (int i = 0; i < bullets.size; i++) {
            bullets.get(i).update(dt);
        }
    }

    private void removeBullets() {
        Array<Body> bodies = cl.getBodiesToRemove();
        for (int i = 0; i < bodies.size; i++) {
            Body body = bodies.get(i);
            boolean isRemove = bullets.removeValue((Bullet) body.getUserData(), true);
            if (isRemove) {
                ParticleEffect particleEffect = new ParticleEffect();
                particleEffect.load(Gdx.files.internal("particle/destroy_blocks.p"), Gdx.files.internal("particle"));
                particleEffect.setPosition(body.getPosition().x * PPM, body.getPosition().y * PPM);
                particleEffect.start();
                destroyBulletEffect.add(particleEffect);

                body.setUserData(null);
                world.destroyBody(body);
            }
        }
        bodies.clear();
    }

    private void removeEndedDestroyBulletEffects() {
        for (int i = 0; i < destroyBulletEffect.size; i++) {
            ParticleEffect particleEffect = destroyBulletEffect.get(i);
            if (particleEffect.isComplete()) {
                particleEffect.dispose();
                destroyBulletEffect.removeValue(particleEffect, true);
                i--;
            }
        }
    }

    @Override
    public void render() {
        Gdx.gl20.glClearColor(0, 0, 0, 1);
        Gdx.gl20.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // camera follow player TODO: set better position camera
        cam.setPosition(player.getPosition().x * PPM,
                player.getPosition().y * PPM);
        cam.update();

        mapRenderer.setView(cam);
        mapRenderer.render();

        // draw player
        sb.setProjectionMatrix(cam.combined);
        player.render(sb);

        // draw bullets
        for (int i = 0; i < bullets.size; i++) {
            bullets.get(i).render(sb);
        }

        sb.begin();
        for (int i = 0; i < destroyBulletEffect.size; i++) {
            ParticleEffect particleEffect = destroyBulletEffect.get(i);
            particleEffect.draw(sb, Gdx.graphics.getDeltaTime());
        }
        sb.end();

        // draw controllerPlayer
        sb.setProjectionMatrix(hudCam.combined);
        controllerPlayer.render(sb);
    }

    @Override
    public void dispose() {
        map.dispose();
        mapRenderer.dispose();
    }
}
