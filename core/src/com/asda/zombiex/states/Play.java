package com.asda.zombiex.states;

import com.asda.zombiex.Game;
import com.asda.zombiex.entities.Bullet;
import com.asda.zombiex.entities.ControllerPlayer;
import com.asda.zombiex.entities.HandleInputListener;
import com.asda.zombiex.entities.Player;
import com.asda.zombiex.handlers.B2DVars;
import com.asda.zombiex.handlers.GameContactListener;
import com.asda.zombiex.handlers.GameStateManager;
import com.asda.zombiex.handlers.RespawnPoints;
import com.asda.zombiex.net.Client;
import com.asda.zombiex.net.ClientCallback;
import com.asda.zombiex.net.ClientRequestListener;
import com.asda.zombiex.net.Server;
import com.asda.zombiex.net.ServerCallback;
import com.asda.zombiex.net.ServerResponseListener;
import com.asda.zombiex.utils.StringUtils;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.ParticleEffect;
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.MapObjects;
import com.badlogic.gdx.maps.MapProperties;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.ChainShape;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.MassData;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Array;

import java.util.Random;

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
    private Array<RespawnPoints> respawns = new Array<RespawnPoints>();

    private Array<Player> players;
    private Player actualPlayer;
    private ControllerPlayer controllerPlayer;
    private HandleInputListener handleInputListener;

    private Array<Bullet> bullets;
    private Array<ParticleEffect> destroyBulletEffect;

    private Server server;
    private Client client;

    public Play(GameStateManager gsm) {
        super(gsm);

        createWorld();
        createMap();
        bullets = new Array<Bullet>();
        players = new Array<Player>();
        destroyBulletEffect = new Array<ParticleEffect>();
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

        createBorderMap();
        createRespawnPoints(map.getLayers().get("respawn"));
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
                Fixture fixture = world.createBody(bdef).createFixture(fdef);
                if (bits == B2DVars.BIT_GREEN_BLOCK) {
                    fixture.setUserData("block_green");
                    fdef.restitution = 0.8f;
                } else {
                    fixture.setUserData("block");
                }
            }
        }

        cs.dispose();
    }

    private void createBorderMap() {
        BodyDef bdef = new BodyDef();
        FixtureDef fdef = new FixtureDef();

        float borderWidth = tileMapWidth * tileSize / 2 / PPM;
        float borderHeight = tileMapHeight * tileSize / 2 / PPM;

        ChainShape cs = new ChainShape();
        float vectors[] = {
                -borderWidth, -borderHeight,
                -borderWidth, borderHeight,
                borderWidth, borderHeight,
                borderWidth, -borderHeight};
        cs.createLoop(vectors);

        bdef.type = BodyDef.BodyType.StaticBody;
        bdef.position.set(borderWidth, borderHeight);
        fdef.friction = 0;
        fdef.shape = cs;
        fdef.filter.categoryBits = B2DVars.BIT_BORDER;
        fdef.filter.maskBits = B2DVars.BIT_PLAYER | B2DVars.BIT_BULLET;

        world.createBody(bdef).createFixture(fdef).setUserData("border");

        cs.dispose();
    }

    private void createRespawnPoints(MapLayer respawnPoints) {
        if (respawnPoints == null) {
            throw new RuntimeException("Map don't have respawn");
        }

        MapObjects respawnObjects = respawnPoints.getObjects();
        int count = respawnObjects.getCount();
        for (int i = 0; i < count; i++) {
            MapProperties properties = respawnObjects.get(i).getProperties();
            float x = (Float) properties.get("x") / PPM;
            float y = (Float) properties.get("y") / PPM;

            respawns.add(new RespawnPoints(x, y));
        }
    }

    private Player createPlayer(String namePlayer) {
        Random rand = new Random();
        int random = rand.nextInt(respawns.size);
        RespawnPoints respawnPoints = respawns.get(random);
        return createPlayer(namePlayer, respawnPoints.getX(), respawnPoints.getY());
    }

    private Player createPlayer(String namePlayer, float posX, float posY) {
        BodyDef bdef = new BodyDef();
        bdef.position.set(posX, posY);
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
        fdef.filter.maskBits = B2DVars.BIT_RED_BLOCK | B2DVars.BIT_GREEN_BLOCK | B2DVars.BIT_BLUE_BLOCK | B2DVars.BIT_YELLOW_BLOCK | B2DVars.BIT_BORDER | B2DVars.BIT_BULLET;
        body.createFixture(fdef).setUserData("player");
        shape.dispose();

        MassData md = body.getMassData();
        md.mass = 1f;
        body.setMassData(md);

        Player player = new Player(body, players.size);
        player.setName(namePlayer);
        body.setUserData(player);

        return player;
    }

    @Override
    public void handleInput() {
        if (!controllerPlayer.isAnalogDown()) {
            for (int i = 0; i < players.size; i++) {
                players.get(i).braking();
            }
        }

        float intensity = controllerPlayer.getAnalogIntensity();
        if (intensity != 0f) {
            handleInputListener.analogIntensity(intensity);
        }

        float angle = controllerPlayer.getAnalogAngle();
        if (angle != 0f) {
            float radian = angle * (float) Math.PI / 180;
            handleInputListener.analogRadian(radian);
        }

        if (controllerPlayer.isButtonJumpClicked()) {
            handleInputListener.firstButtonClicked();
        }

        if (controllerPlayer.isButtonFireClicked()) {
            handleInputListener.secondButtonClicked();
        }
    }

    @Override
    public void update(float dt) {
        handleInput();
        world.step(Game.STEP, 1, 1);

        removeBullets();
        removeEndedDestroyBulletEffects();

        for (int i = 0; i < players.size; i++) {
            players.get(i).update(dt);
        }

        for (int i = 0; i < bullets.size; i++) {
            bullets.get(i).update(dt);
        }

        sendDataFromServer();
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
        if (actualPlayer != null) {
            cam.setPosition(actualPlayer.getPosition().x * PPM,
                    actualPlayer.getPosition().y * PPM);
            cam.update();
        }

        mapRenderer.setView(cam);
        mapRenderer.render();

        // draw player
        sb.setProjectionMatrix(cam.combined);
        for (int i = 0; i < players.size; i++) {
            players.get(i).render(sb);
        }

        // draw bullets
        for (int i = 0; i < bullets.size; i++) {
            bullets.get(i).render(sb);
        }

        sb.begin();
        for (int i = 0; i < destroyBulletEffect.size; i++) {
            destroyBulletEffect.get(i).draw(sb, Gdx.graphics.getDeltaTime());
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

    public void setSinglePlayer() {
        Player player = createPlayer("SinglePlayer");
        players.add(player);
        actualPlayer = player;

        handleInputListener = new HandleInputListener() {
            @Override
            public void analogIntensity(float intensity) {
                actualPlayer.moving(intensity);
            }

            @Override
            public void analogRadian(float radian) {
                actualPlayer.setViewfinderRadian(radian);
            }

            @Override
            public void firstButtonClicked() {
                actualPlayer.jump();
            }

            @Override
            public void secondButtonClicked() {
                if (actualPlayer.canShot()) {
                    Bullet bullet = actualPlayer.shot(world);
                    bullets.add(bullet);
                }
            }
        };
    }

    public void setServer(final String hostIp) {
        ClientRequestListener clientRequestListener = new ClientRequestListener() {
            @Override
            public void analogIntensity(String remoteAddress, float intensity) {
                Player clientPlayer = searchPlayerByName(remoteAddress);
                clientPlayer.moving(intensity);
            }

            @Override
            public void analogRadian(String remoteAddress, float radian) {
                Player clientPlayer = searchPlayerByName(remoteAddress);
                clientPlayer.setViewfinderRadian(radian);
            }

            @Override
            public void firstButtonClicked(String remoteAddress) {
                Player clientPlayer = searchPlayerByName(remoteAddress);
                clientPlayer.jump();
            }

            @Override
            public boolean secondButtonClicked(String remoteAddress) {
                Player clientPlayer = searchPlayerByName(remoteAddress);
                if (clientPlayer.canShot()) {
                    Bullet bullet = clientPlayer.shot(world);
                    bullets.add(bullet);
                    return true;
                }

                return false;
            }
        };

        server = new Server();
        server.startServer(new ServerCallback() {
            @Override
            public void serverReady() {
                setClient(hostIp);
            }

            @Override
            public void clientConnected(String remoteAddress) {
                server.sendResponseClients(remoteAddress, StringUtils.append("createPlayer:", remoteAddress, "|"));
                server.sendResponseClient(remoteAddress, "assignPlayer|");
            }

            @Override
            public void initClient(String remoteAddress) {
                // TODO: here should be send info actually game (in while join to game)

                for (int i = 0; i < players.size; i++) {
                    server.sendResponseClient(remoteAddress, StringUtils.append("createPlayer:", players.get(i).getName(), "|"));
                }
            }
        }, clientRequestListener);
    }

    public void setClient(String connectIp) {
        ServerResponseListener serverResponseListener = new ServerResponseListener() {
            @Override
            public void serverCreatePlayer(String namePlayer) {
                Player player = createPlayer(namePlayer);
                players.add(player);
            }

            @Override
            public void serverAssignPlayer(String namePlayer) {
                actualPlayer = searchPlayerByName(namePlayer);
            }

            @Override
            public void serverPositionPlayer(String namePlayer, Vector2 value) {
                if (server != null) {
                    return;
                }
                Player clientPlayer = searchPlayerByName(namePlayer);
                clientPlayer.setPosition(value);
            }

            @Override
            public void serverViewfinderRadian(String namePlayer, float radian) {
                if (server != null) {
                    return;
                }
                Player clientPlayer = searchPlayerByName(namePlayer);
                clientPlayer.setViewfinderRadian(radian);
            }

            @Override
            public void serverShot(String namePlayer) {
                if (server != null) {
                    return;
                }
                Player clientPlayer = searchPlayerByName(namePlayer);
                Bullet bullet = clientPlayer.shot(world);
                bullets.add(bullet);
            }

            @Override
            public void serverVelocityPlayer(String namePlayer, Vector2 value) {
                if (server != null) {
                    return;
                }
                Player clientPlayer = searchPlayerByName(namePlayer);
                clientPlayer.setVelocity(value);
            }
        };

        client = new Client();
        client.startClient(new ClientCallback() {
            @Override
            public void onResponse(final Array<String> responses) {

            }
        }, serverResponseListener, connectIp);

        handleInputListener = new HandleInputListener() {
            @Override
            public void analogIntensity(float intensity) {
                client.sendToServer("moving: " + intensity + "|");
            }

            @Override
            public void analogRadian(float radian) {
                client.sendToServer("radian: " + radian + "|");
            }

            @Override
            public void firstButtonClicked() {
                client.sendToServer("jump|");
            }

            @Override
            public void secondButtonClicked() {
                client.sendToServer("fire|");
            }
        };
    }

    private void sendDataFromServer() {
        if (server == null) {
            return;
        }

        for (int i = 0; i < players.size; i++) {
            Player player = players.get(i);

            server.sendResponseClients(player.getName(), StringUtils.append("position: ", player.getPosition().toString(), "|"));
            server.sendResponseClients(player.getName(), StringUtils.append("velocity: ", player.getBody().getLinearVelocity().toString(), "|"));
        }
    }

    private Player searchPlayerByName(String remoteAddress) {
        for (int i = 0; i < players.size; i++) {
            if (players.get(i).getName().equals(remoteAddress)) {
                return players.get(i);
            }
        }
        return null;
    }
}
