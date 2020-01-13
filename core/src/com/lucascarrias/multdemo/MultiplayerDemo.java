package com.lucascarrias.multdemo;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.StretchViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.lucascarrias.multdemo.sprites.StarShip;
import com.lucascarrias.multdemo.tools.Controller;
import com.lucascarrias.multdemo.tools.Login;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

import javax.swing.JOptionPane;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;
import jdk.nashorn.api.scripting.JSObject;
import sun.rmi.runtime.Log;


public class MultiplayerDemo extends ApplicationAdapter{
	private final float UPDATE_TIME = 1/60f;
	float timer;

	public static SpriteBatch batch;

	private Socket socket;
	private String id;

	private StarShip player;

	private Texture playerShip;
	private Texture friendlyShip;
	private Texture background;

	private HashMap<String, StarShip> friendlyPlayers;

	private OrthographicCamera gameCam;
	private Viewport gamePort;
	private Controller controller;


	@Override
	public void create () {
		batch = new SpriteBatch();

		playerShip = new Texture("playerShip2.png");
		friendlyShip = new Texture("playerShip.png");
		background = new Texture("bg.jpeg");

		friendlyPlayers = new HashMap<>();
		connectSocket();
		configSocketEvents();

		gameCam = new OrthographicCamera();
		gamePort = new FitViewport(1024,720,gameCam);

		controller = new Controller();
	}

	public void handleInput(float dt){
		if(player != null){
			if (controller.isLeftPressed()){
				player.setPosition(player.getX() -(200 * dt), player.getY());
				player.setRotation(90);
			} else if (controller.isRigthPressed()) {
				player.setPosition(player.getX() + (200 * dt), player.getY());
				player.setRotation(270);
			}else if (controller.isDownPressed()) {
				player.setPosition(player.getX() , player.getY() - (200 * dt));
				player.setRotation(180);
			}else if (controller.isUpPressed()) {
				player.setPosition(player.getX() , player.getY() + (200 * dt));
				player.setRotation(0);
			}
		}

	}

	public void updateServer(float dt){
		gameCam.update();
		timer += dt;
		if (timer >= UPDATE_TIME && player != null && player.hasMoved()){
			JSONObject data = new JSONObject();
			try{
				data.put("x", player.getX());
				data.put("y", player.getY());
				data.put("angle", player.getRotation());
				socket.emit("playerMoved", data);
			}catch (JSONException e){
				Gdx.app.log("SocketIO", e.getMessage());
			}
		}
	}

	@Override
	public void resize(int width, int height) {
		super.resize(width, height);
		gamePort.update(width,height);
		controller.resize(width, height);
	}

	@Override
	public void render () {
		Gdx.gl.glClearColor(0f, 0f, 0f, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

		handleInput(Gdx.graphics.getDeltaTime());
		updateServer(Gdx.graphics.getDeltaTime());

		batch.begin();
		batch.draw(background, 0, 0);
		if (player != null){
			player.draw(batch);
		}
		for(HashMap.Entry<String, StarShip> entry: friendlyPlayers.entrySet()){
			entry.getValue().draw(batch);
		}
		batch.end();
		if(Gdx.app.getType() == Application.ApplicationType.Android)
			controller.draw();

	}
	
	@Override
	public void dispose () {
		batch.dispose();
		playerShip.dispose();
		friendlyShip.dispose();
		background.dispose();
		controller.dispose();
	}

	public void connectSocket(){
		try{
			socket = IO.socket("http://192.168.1.3:8080");
			socket.connect();
		}catch (Exception e){
			System.out.println(e);
		}
	}

	public void configSocketEvents(){
		socket.on(Socket.EVENT_CONNECT, new Emitter.Listener() {
			@Override
			public void call(Object... args) {
				Gdx.app.log("SocketIO", "Connected");
				player = new StarShip(playerShip);
			}
		}).on("socketID", new Emitter.Listener() {
			@Override
			public void call(Object... args)  {
				try {
					JSONObject data = (JSONObject) args[0];
					String id = data.getString("id");
					Gdx.app.log("SocketIO", "My id: " + id);
				}catch (JSONException e){
					Gdx.app.log("SocketIO", e.getMessage());
				}

			}
		}).on("newPlayer", new Emitter.Listener() {
			@Override
			public void call(Object... args) {
				try {
					JSONObject data = (JSONObject) args[0];
					String id = data.getString("id");
					Gdx.app.log("SocketIO", "New player connected: " + id);
					friendlyPlayers.put(id, new StarShip(friendlyShip));
				}catch (JSONException e){
					Gdx.app.log("SocketIO", e.getMessage());
				}
			}
		}).on("playerDisconnected", new Emitter.Listener() {
			@Override
			public void call(Object... args) {
				try {
					JSONObject data = (JSONObject) args[0];
					String id = data.getString("id");
					friendlyPlayers.remove(id);
				}catch (JSONException e){
					Gdx.app.log("SocketIO", e.getMessage());
				}
			}
		}).on("getPlayers", new Emitter.Listener() {
			@Override
			public void call(Object... args) {
				JSONArray objects = (JSONArray) args[0];
				try {
					for(int i = 0; i < objects.length(); i++){
						StarShip coopPlayer = new StarShip(friendlyShip);
						Vector2 position = new Vector2();
						position.x = ((Double) objects.getJSONObject(i).getDouble("x")).floatValue();
						position.y = ((Double) objects.getJSONObject(i).getDouble("y")).floatValue();
						coopPlayer.setPosition(position.x, position.y);
						coopPlayer.setRotation(((Double) objects.getJSONObject(i).getDouble("angle")).floatValue());
						friendlyPlayers.put(objects.getJSONObject(i).getString("id"), coopPlayer);
					}
				}catch (JSONException e){
					Gdx.app.log("SocketIO", e.getMessage());
				}
			}
		}).on("playerMoved", new Emitter.Listener() {
			@Override
			public void call(Object... args) {
				JSONObject data = (JSONObject) args[0];
				try {
					String playerId = data.getString("id");
					Double x = data.getDouble("x");
					Double y = data.getDouble("y");
					Double angle = data.getDouble("angle");
					if (friendlyPlayers.get(playerId) != null){
						friendlyPlayers.get(playerId).setPosition(x.floatValue(), y.floatValue());
						friendlyPlayers.get(playerId).setRotation(angle.floatValue());
					}
				}catch (JSONException e){
					Gdx.app.log("SocketIO", e.getMessage());
				}
			}
		});
	}

}
