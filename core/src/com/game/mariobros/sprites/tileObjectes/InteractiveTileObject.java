package com.game.mariobros.sprites.tileObjectes;

import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTile;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.physics.box2d.*;
import com.game.mariobros.MarioBros;
import com.game.mariobros.screens.PlayerScreen;
import com.game.mariobros.sprites.Mario;

public abstract class InteractiveTileObject {

    World world;
    TiledMap map;
    TiledMapTile tile;
    Rectangle bounds;
    Body body;
    Fixture fixture;
    PlayerScreen screen;
    MapObject object;

    public InteractiveTileObject(PlayerScreen screen, MapObject object) {
        this.object = object;
        this.world = screen.getWorld();
        this.map = screen.getMap();
        this.bounds = ((RectangleMapObject) object).getRectangle();
        this.screen = screen;

        BodyDef bdef = new BodyDef();
        FixtureDef fdef = new FixtureDef();
        PolygonShape shape = new PolygonShape();

        bdef.type = BodyDef.BodyType.StaticBody;
        bdef.position.set((bounds.getX() + bounds.getWidth() / 2) /  MarioBros.PPM,
                (bounds.getY() + bounds.getHeight() / 2) / MarioBros.PPM);

        body = world.createBody(bdef);

        shape.setAsBox(bounds.getWidth() / 2 / MarioBros.PPM, bounds.getHeight() / 2 / MarioBros.PPM);
        fdef.shape = shape;
        fixture = body.createFixture(fdef);
    }

    public abstract void onHeadHit(Mario mario);

    public void setCategoryFilter(short filterBIt) {
        Filter filter = new Filter();
        filter.categoryBits = filterBIt;
        fixture.setFilterData(filter);
    }

    public TiledMapTileLayer.Cell getCell() {
        TiledMapTileLayer layer = (TiledMapTileLayer) map.getLayers().get(1);

        return layer.getCell((int)(body.getPosition().x * MarioBros.PPM / 16),
                (int)(body.getPosition().y * MarioBros.PPM / 16));
    }
}
