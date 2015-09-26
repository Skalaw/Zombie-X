package com.asda.zombiex.states;

import com.asda.zombiex.Game;
import com.asda.zombiex.entities.ControllerPlayer;
import com.asda.zombiex.entities.Player;
import com.asda.zombiex.handlers.B2DVars;
import com.asda.zombiex.handlers.GameContactListener;
import com.asda.zombiex.handlers.GameStateManager;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
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

    public Play(GameStateManager gsm) {
        super(gsm);

        createWorld();
        createMap();
        createPlayer();
        controllerPlayer = new ControllerPlayer(hudCam);
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

        // go through all the cells in the layer
        for (int row = 0; row < layer.getHeight(); row++) {
            for (int col = 0; col < layer.getWidth(); col++) {
                TiledMapTileLayer.Cell cell = layer.getCell(col, row);
                if (cell == null || cell.getTile() == null) {
                    continue;
                }

                bdef.type = BodyDef.BodyType.StaticBody;
                bdef.position.set((col + 0.5f) * tileSize / PPM, (row + 0.5f) * tileSize / PPM);

                ChainShape cs = new ChainShape();
                Vector2[] v = new Vector2[3];
                v[0] = new Vector2(-tileSize / 2 / PPM, -tileSize / 2 / PPM);
                v[1] = new Vector2(-tileSize / 2 / PPM, tileSize / 2 / PPM);
                v[2] = new Vector2(tileSize / 2 / PPM, tileSize / 2 / PPM);
                cs.createChain(v);
                fdef.friction = 0;
                fdef.shape = cs;
                fdef.filter.categoryBits = bits;
                fdef.filter.maskBits = B2DVars.BIT_PLAYER;
                world.createBody(bdef).createFixture(fdef);
                cs.dispose();
            }
        }
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
        body.createFixture(fdef);
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

        if (controllerPlayer.isButtonJumpClicked()) {
            player.jump();
        }
    }

    @Override
    public void update(float dt) {
        handleInput();
        world.step(Game.STEP, 8, 3);
        player.update(dt);
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

        // draw mControllerPlayer
        sb.setProjectionMatrix(hudCam.combined);
        controllerPlayer.render(sb);

        // draw player
        sb.setProjectionMatrix(cam.combined);
        player.render(sb);
    }

    @Override
    public void dispose() {
        map.dispose();
        mapRenderer.dispose();
    }
}
