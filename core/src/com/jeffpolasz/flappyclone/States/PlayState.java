package com.jeffpolasz.flappyclone.States;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.jeffpolasz.flappyclone.FlappyClone;
import com.jeffpolasz.flappyclone.sprites.Bird;
import com.jeffpolasz.flappyclone.sprites.Tube;

/**
 * Created by Jeff on 2017-11-23.
 */

public class PlayState extends State{
    private static final int TUBE_SPACING = 125;
    private static final int TUBE_COUNT = 4;
    private static final int GROUND_Y_OFFSET = -50;

    private Bird bird;
    private Texture bg;
    private Texture ground;
    private Vector2 groundPos1, groundPos2;

    private Array<Tube> tubes;

    private int score = 0;
    BitmapFont font;
    GlyphLayout layout;
    float scoreWidth;
    boolean scoredPoint = false;

    private Sound pointSound;

    public PlayState(GameStateManager gsm) {
        super(gsm);
        bird = new Bird(50, 300);
        cam.setToOrtho(false, FlappyClone.WIDTH/2, FlappyClone.HEIGHT/2);
        bg = new Texture("bg.png");
        ground = new Texture("ground.png");
        groundPos1 = new Vector2(cam.position.x - cam.viewportWidth/2, GROUND_Y_OFFSET);
        groundPos2 = new Vector2((cam.position.x - cam.viewportWidth/2) + ground.getWidth(),GROUND_Y_OFFSET);

        pointSound = Gdx.audio.newSound(Gdx.files.internal("sfx_point.ogg"));

        font = new BitmapFont(Gdx.files.internal("font.fnt"));
//        font.setColor(Color.WHITE);
        font.getData().setScale(0.5f);
        font.setUseIntegerPositions(false);

        tubes = new Array<Tube>();

        for (int i = 1; i <= TUBE_COUNT; i++) {
            tubes.add(new Tube(i*(TUBE_SPACING + Tube.TUBE_WIDTH)));
        }
    }

    @Override
    protected void handleInput() {
        if (Gdx.input.justTouched()) {
            bird.jump();
        }
    }

    @Override
    public void update(float dt) {
        handleInput();
        updateGround();
        bird.update(dt);
        cam.position.x = bird.getPosition().x + 80;
        GlyphLayout layout = new GlyphLayout(font, String.valueOf(score));
        scoreWidth = layout.width;

        for (int i=0; i < tubes.size; i++) {
            Tube tube = tubes.get(i);

            if (cam.position.x - tube.getTopTube().getWidth() > tube.getPosTopTube().x + tube.getTopTube().getWidth()) {
                if (scoredPoint == false) {
                    pointSound.play(0.5f);
                    score++;
                    scoredPoint = true;
                }
            }

            if (cam.position.x - (cam.viewportWidth/2) > tube.getPosTopTube().x + tube.getTopTube().getWidth()) {
                tube.reposition(tube.getPosTopTube().x + ((Tube.TUBE_WIDTH + TUBE_SPACING) * TUBE_COUNT));
                scoredPoint = false;
            }

            if (tube.collides(bird.getBounds())) {
                gsm.set(new PlayState(gsm));
            }
        }

        if (bird.getPosition().y <= ground.getHeight() + GROUND_Y_OFFSET) {
            gsm.set(new PlayState(gsm));
        }
        cam.update();
    }

    @Override
    public void render(SpriteBatch sb) {
        sb.setProjectionMatrix(cam.combined);
        sb.begin();
        sb.draw(bg, cam.position.x - cam.viewportWidth/2, 0);
        sb.draw(bird.getTexture(), bird.getPosition().x, bird.getPosition().y);
        for (Tube tube : tubes) {
            sb.draw(tube.getTopTube(), tube.getPosTopTube().x, tube.getPosTopTube().y);
            sb.draw(tube.getBottomTube(), tube.getPosBotTube().x, tube.getPosBotTube().y);
        }
        font.draw(sb, String.valueOf(score),cam.position.x - scoreWidth/2,cam.viewportHeight-5);
        sb.draw(ground, groundPos1.x, groundPos1.y);
        sb.draw(ground, groundPos2.x, groundPos2.y);
        sb.end();
    }

    @Override
    public void dispose() {
        bg.dispose();
        bird.dispose();
        ground.dispose();
        for (Tube tube : tubes) {
            tube.dispose();
        }
        font.dispose();
        pointSound.dispose();
        System.out.println("Play State Disposed");
    }

    private void updateGround() {
        if (cam.position.x - (cam.viewportWidth/2) > groundPos1.x + ground.getWidth()) {
            groundPos1.add(ground.getWidth()*2, 0);
        }
        if (cam.position.x - (cam.viewportWidth/2) > groundPos2.x + ground.getWidth()) {
            groundPos2.add(ground.getWidth()*2, 0);
        }
    }
}
