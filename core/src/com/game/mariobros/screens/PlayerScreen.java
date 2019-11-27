package com.game.mariobros.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.game.mariobros.MarioBros;
import com.game.mariobros.scenes.Hud;
import com.game.mariobros.sprites.enemies.Enemy;
import com.game.mariobros.sprites.Mario;
import com.game.mariobros.sprites.items.Item;
import com.game.mariobros.sprites.items.ItemDefinition;
import com.game.mariobros.sprites.items.Mushroom;
import com.game.mariobros.tools.B2WorldCreator;
import com.game.mariobros.tools.WorldContactListener;
import java.util.concurrent.LinkedBlockingDeque;

public class PlayerScreen implements Screen {

    // Reference to our game, used to set screens
    private MarioBros game;
    private TextureAtlas atlas;

    // Game variables
    private OrthographicCamera gamecam;
    private Viewport gamePort;
    private Hud hud;

    // Tiled map variables
    private TmxMapLoader mapLoader;
    private TiledMap map;
    private OrthogonalTiledMapRenderer renderer;

    // Box2d variables
    private World world;
    private Box2DDebugRenderer b2dr;
    private B2WorldCreator creator;

    // Mario player
    private Mario player;

    private Music music;

    private Array<Item> items;
    private LinkedBlockingDeque<ItemDefinition> itemToSpawn;

    public PlayerScreen(MarioBros game) {
        this.game = game;

        atlas = new TextureAtlas("Mario_and_Enemies.pack");

        // Create cam used to follow mario through cam world
        gamecam = new OrthographicCamera();

        // Create a FitViewport to maintain virtual aspect ratio despite screen size
        gamePort = new FitViewport(MarioBros.V_WIDHT / MarioBros.PPM, MarioBros.V_HEIGHT / MarioBros.PPM, gamecam);

        // Create our game Hud for scores/timers/level info
        hud = new Hud(game.batch);

        // Load our map and setup our map renderer
        mapLoader = new TmxMapLoader();
        map = mapLoader.load("maps/level1.tmx");
        renderer = new OrthogonalTiledMapRenderer(map, 1 / MarioBros.PPM);

        // Initially set our gamecam to be centered correctly at the start of map
        gamecam.position.set(gamePort.getWorldWidth() / 2, gamePort.getWorldHeight() / 2, 0);

        world = new World(new Vector2(0, -10), true);
        b2dr = new Box2DDebugRenderer();

        creator = new B2WorldCreator(this);

        // Create our player
        player = new Mario(this);

        world.setContactListener(new WorldContactListener());

        //music = MarioBros.manager.get("audio/music/mario_music.ogg", Music.class);
        //music.setVolume(0.4f);
        //music.setLooping(true);
        //music.play();

        items = new Array<Item>();
        itemToSpawn = new LinkedBlockingDeque<ItemDefinition>();
    }

    public TextureAtlas getAtlas() {
        return atlas;
    }

    public void spawnItem(ItemDefinition itemDef) {
        itemToSpawn.add(itemDef);
    }

    public boolean gameOver() {
        return (player.currentState == Mario.State.DEAD && player.getStateTimer() > 3);
    }

    public void handleSpawningItems() {
        if(!itemToSpawn.isEmpty()) {
            ItemDefinition idef = itemToSpawn.poll();
            if(idef.type == Mushroom.class) {
                items.add(new Mushroom(this, idef.position.x, idef.position.y));
            }
        }
    }

    @Override
    public void show() {

    }

    public void handleInput(float dt) {
        if(player.currentState != Mario.State.DEAD) {
            if (Gdx.input.isKeyJustPressed(Input.Keys.UP) && player.getToJump()) {
                player.b2body.applyLinearImpulse(new Vector2(0, 4f), player.b2body.getWorldCenter(), true);
                player.setToJump(false);
            }
            if (Gdx.input.isKeyPressed(Input.Keys.RIGHT) && player.b2body.getLinearVelocity().x <= 2) {
                player.b2body.applyLinearImpulse(new Vector2(.1f, 0), player.b2body.getWorldCenter(), true);
            }
            if (Gdx.input.isKeyPressed(Input.Keys.LEFT) && player.b2body.getLinearVelocity().x >= -2) {
                player.b2body.applyLinearImpulse(new Vector2(-.1f, 0), player.b2body.getWorldCenter(), true);
            }
        }
    }

    public TiledMap getMap() { return map; }

    public World getWorld() { return world; }

    public Hud getHud() { return hud; }

    public void update(float dt) {
        // handle user input first
        handleInput(dt);

        handleSpawningItems();

        world.step(1/60f, 6, 2);

        if(player.currentState != Mario.State.DEAD) {
            gamecam.position.x = player.b2body.getPosition().x;
        }
        //gamecam.position.y = player.b2body.getPosition().y;

        // updating mario frames
        player.update(dt);

        // updating goombas frames
        for(Enemy enemy : creator.getEnemies()) {
            enemy.update(dt);
            if(enemy.getX() < player.getX() + (224 / MarioBros.PPM)) {
                enemy.b2body.setActive(true);
            }
        }

        // updating items
        for(Item item : items) {
            item.update(dt);
        }

        // update our gamecam with correct coordinate after changes
        gamecam.update();

        // tell our renderer to draw only what our camera can see in our game world
        renderer.setView(gamecam);
    }

    @Override
    public void render(float delta) {
        // Separate our update logic from render
        update(delta);

        // Clear the screen with black
        Gdx.gl.glClearColor(0,0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // Render our game map
        renderer.render();

        // Render our game hud
        hud.update(delta);

        // Renderer our Box2DDebugLines
        b2dr.render(world, gamecam.combined);

        game.batch.setProjectionMatrix(gamecam.combined);
        game.batch.begin();

        // drawing player (Mario)
        player.draw(game.batch);

        // drawing enemies
        for(Enemy enemy : creator.getEnemies()) {
            enemy.draw(game.batch);
        }

        // drawing items
        for(Item item : items) {
            item.draw(game.batch);
        }
        game.batch.end();

        // Set out Batch to now draw what the Hud camera sees
        game.batch.setProjectionMatrix(hud.stage.getCamera().combined);
        hud.stage.draw();

        if(gameOver()) {
            game.setScreen(new GameOverScreen(game));
            dispose();
        }

    }

    @Override
    public void resize(int width, int height) {
        gamePort.update(width, height);
    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void hide() {

    }

    @Override
    public void dispose() {
        map.dispose();
        renderer.dispose();
        world.dispose();
        b2dr.dispose();
        hud.dispose();
    }
}
