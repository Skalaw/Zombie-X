package com.asda.zombiex.states;

import com.asda.zombiex.Game;
import com.asda.zombiex.entities.Player;
import com.asda.zombiex.handlers.GameStateManager;
import com.asda.zombiex.handlers.InputKeys;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
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

    private TiledMap map;
    private int tileMapWidth;
    private int tileMapHeight;
    private float tileSize;
    private OrthogonalTiledMapRenderer mapRenderer;

    private Player player;

    public Play(GameStateManager gsm) {
        super(gsm);

        world = new World(new Vector2(0f, 0f), true);

        createMap();
        createPlayer();
    }

    private void createMap() {
        TmxMapLoader tmxMapLoad = new TmxMapLoader();
        map = tmxMapLoad.load("maps/board.tmx");

        mapRenderer = new OrthogonalTiledMapRenderer(map);
        tileMapWidth = (Integer) map.getProperties().get("width");
        tileMapHeight = (Integer) map.getProperties().get("height");
        tileSize = (Integer) map.getProperties().get("tilewidth");
        cam.setBounds(0, tileMapWidth * tileSize, 0, tileMapHeight * tileSize);
    }

    private void createPlayer() {
        BodyDef bdef = new BodyDef();
        bdef.position.set(Game.V_WIDTH / 2 / PPM, 0 / PPM);
        bdef.type = BodyDef.BodyType.DynamicBody;
        bdef.fixedRotation = true;

        Body body = world.createBody(bdef);

        PolygonShape shape = new PolygonShape();
        shape.setAsBox(13 / PPM, 13 / PPM);
        FixtureDef fdef = new FixtureDef();
        fdef.shape = shape;
        fdef.density = 1;
        fdef.friction = 0;
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
        if (InputKeys.isDown()) {
            float x = player.getBody().getPosition().x;
            float y = player.getBody().getPosition().y;

            if (InputKeys.isTouchedLeftScreen()) {
                x -= 0.05f;
            } else if (InputKeys.isTouchedRightScreen()) {
                x += 0.05f;
            }

            if (InputKeys.isTouchedTopScreen()) {
                y += 0.05f;
            } else if (InputKeys.isTouchedBottomScreen()) {
                y -= 0.05f;
            }

            player.getBody().setTransform(x, y, 0);
        }
    }

    @Override
    public void update(float dt) {
        handleInput();
        world.step(Game.STEP, 1, 1);
        player.update(dt);
    }

    @Override
    public void render() {
        Gdx.gl20.glClearColor(0, 1, 0, 1);
        Gdx.gl20.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // camera follow player
        cam.setPosition(player.getPosition().x * PPM /*+ Game.V_WIDTH / 4*/,
                player.getPosition().y * PPM /*+ Game.V_HEIGHT / 4*/);
        cam.update();

        mapRenderer.setView(cam);
        mapRenderer.render();

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
