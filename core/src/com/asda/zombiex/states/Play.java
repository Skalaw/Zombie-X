package com.asda.zombiex.states;

import com.asda.zombiex.Game;
import com.asda.zombiex.entities.Bullet;
import com.asda.zombiex.entities.ControllerPlayer;
import com.asda.zombiex.entities.HandleInputListener;
import com.asda.zombiex.entities.Player;
import com.asda.zombiex.handlers.B2DVars;
import com.asda.zombiex.handlers.GameContactListener;
import com.asda.zombiex.handlers.GameStateManager;
import com.asda.zombiex.net.Client;
import com.asda.zombiex.net.ClientCallback;
import com.asda.zombiex.net.Server;
import com.asda.zombiex.net.ServerCallback;
import com.asda.zombiex.utils.StringUtils;
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
import com.badlogic.gdx.physics.box2d.Fixture;
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
        fdef.filter.maskBits = B2DVars.BIT_RED_BLOCK | B2DVars.BIT_GREEN_BLOCK | B2DVars.BIT_BLUE_BLOCK | B2DVars.BIT_YELLOW_BLOCK | B2DVars.BIT_BORDER;
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

    public void setSinglePlayer() {
        Player player = createPlayer("SinglePlayer", 50 / PPM, (tileMapHeight - 6) * tileSize / PPM);
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
        server = new Server();
        server.startServer(new ServerCallback() {
            @Override
            public void serverReady() {
                setClient(hostIp);
                world.setGravity(new Vector2(0f, -5f));
            }

            @Override
            public void clientConnected(String remoteAddress) {
                server.sendResponse(remoteAddress, StringUtils.append("createPlayer:", remoteAddress, "|"));
                server.sendResponse(remoteAddress, StringUtils.append("IP:", remoteAddress, " assignPlayer|"));
            }

            @Override
            public void initClient(String remoteAddress) {
                // TODO: here should be send info actually game (in while join to game)

                String directIp = "IP:" + remoteAddress;
                for (int i = 0; i < players.size; i++) {
                    server.sendResponse(remoteAddress, directIp + " createPlayer:" + players.get(i).getName() + "|");
                }
            }

            @Override
            public void request(final String remoteAddress, final Array<String> request) {
                Gdx.app.postRunnable(new Runnable() {
                    @Override
                    public void run() {
                        for (int i = 0; i < request.size; i++) {
                            parserServer(remoteAddress, request.get(i));
                        }
                    }
                });
            }
        });
    }

    public void setClient(String connectIp) {
        world.setGravity(new Vector2(0f, 0f));
        client = new Client();
        client.startClient(new ClientCallback() {
            @Override
            public void onResponse(final String response) {
                final Array<String> responses = splitResponse(response);

                Gdx.app.postRunnable(new Runnable() {
                    @Override
                    public void run() {
                        for (int i = 0; i < responses.size; i++) {
                            parserClient(responses.get(i));
                        }
                    }
                });
            }
        }, connectIp);

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

    private Array<String> splitResponse(String response) {
        Array<String> responses = new Array<String>();

        int firstChar = 0;
        int lastChar = response.indexOf("|");
        while (lastChar != -1) {
            String splitResponse = response.substring(firstChar, lastChar);
            firstChar = lastChar + 1;
            lastChar = response.indexOf("|", firstChar);
            responses.add(splitResponse);
        }
        return responses;
    }

    private void sendDataFromServer() {
        if (server == null) {
            return;
        }

        for (int i = 0; i < players.size; i++) {
            Player player = players.get(i);

            //players.get(0).getBody().getLinearVelocity()
            String sendPosition = StringUtils.append("position: ", player.getPosition().toString(), "|");
            server.sendResponse(player.getName(), sendPosition);
        }
    }

    private void parserServer(String remoteAddress, String request) {
        if (request.startsWith("IP:")) {
            int charEnd = request.indexOf(" ");
            String recipientAddress = request.substring(3, charEnd);
            request = request.substring(charEnd + 1);
            request = "client:" + remoteAddress + " " + request;
            server.sendDataSocket(recipientAddress, request.getBytes());
        } else {
            Player clientPlayer = searchPlayerByName(remoteAddress);

            request = request.substring(0, request.length() - 1);

            if (request.equals("jump")) {
                clientPlayer.jump();
            } else if (request.startsWith("radian: ")) {
                String value = request.replace("radian: ", "");
                float radian = (float) Double.parseDouble(value);
                clientPlayer.setViewfinderRadian(radian);

                StringBuilder sendViewFinder = new StringBuilder();
                sendViewFinder.append(request);
                sendViewFinder.append("|");
                server.sendResponse(remoteAddress, sendViewFinder.toString());

            } else if (request.startsWith("moving: ")) {
                String value = request.replace("moving: ", "");
                float intensity = (float) Double.parseDouble(value);
                clientPlayer.moving(intensity);
            } else if (request.equals("fire")) {
                if (clientPlayer.canShot()) {
                    Bullet bullet = clientPlayer.shot(world);
                    bullets.add(bullet);
                }
            }
        }
    }

    private Vector2 tempVector2 = new Vector2();

    // TODO: refactor this
    private void parserClient(String response) {
        if (response.startsWith("client:")) {
            int firstIndex = response.indexOf(":");
            int lastIndex = response.indexOf(" ");
            String remoteAddress = response.substring(firstIndex + 1, lastIndex);
            String action = response.substring(lastIndex + 1);

            Player clientPlayer = searchPlayerByName(remoteAddress);

            if (action.startsWith("createPlayer")) {
                firstIndex = action.indexOf(":");
                String namePlayer = action.substring(firstIndex + 1);
                Gdx.app.log("namePlayer", "namePlayer: " + namePlayer);

                Player player = createPlayer(namePlayer, 50 / PPM, (tileMapHeight - 6) * tileSize / PPM);
                players.add(player);
            } else if (action.equals("assignPlayer")) {
                actualPlayer = players.get(players.size - 1);
            } else if (action.startsWith("position: ")) {
                if (clientPlayer == null) {
                    return;
                }

                String value = action.replace("position: ", "");

                clientPlayer.setPosition(tempVector2.fromString(value));
            } else if (action.startsWith("radian: ")) {
                if (clientPlayer == null) {
                    return;
                }

                String value = action.replace("radian: ", "");
                float radian = (float) Double.parseDouble(value);
                clientPlayer.setViewfinderRadian(radian);
            }
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
