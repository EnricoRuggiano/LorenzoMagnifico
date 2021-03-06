package it.polimi.ingsw.GC_32.Client.GUI.Screen;

import java.util.ArrayList;

import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;

public class MenuScreen extends BorderPane{
	private ArrayList <VBox> buttons;
	private GridPane grid;
	
	private GameScreen game;

	
	public MenuScreen(GameScreen game){
	
		super();
		this.game = game;
		
		this.grid = new GridPane();
		this.grid.setAlignment(Pos.CENTER);
		
		this.buttons = new ArrayList <VBox>();
		
		this.setId("menu");
		this.getStylesheets().add(this.getClass().getResource("/css/board.css").toExternalForm());
		
		buildLabel("Main Menu", 100, 100);
		
		buildButton("Tower Regions");
		buildButton("Board");
		buildButton("Personal Board");
		buildButton("Players");
		buildButton("Chat");
		buildButton("End Turn");
		
		loadButtons();
		setEvent();
		this.setCenter(this.grid);
		this.getCenter().setId("gridButton");

	}

	public MenuScreen getMenu(){
		return this;
	}

	public void setEvent(){
			
		this.buttons.forEach(button ->{
			switch(this.buttons.indexOf(button)){
		

			case 0:
				this.buttons.get(0).getChildren().get(0).setOnMouseClicked(click->{ this.game.getBoard().changeToTower();
																		GameUtils.update(this.game);});
				break;
			
			case 1:
				this.buttons.get(1).getChildren().get(0).setOnMouseClicked(click->{ this.game.getBoard().changeToBoard();
																					GameUtils.update(this.game);});
				break;
				
			case 2:
				this.buttons.get(2).getChildren().get(0).setOnMouseClicked(click-> {this.game.getBoard().changeToPersonal();
																					GameUtils.update(this.game);});
				break;
				
			case 3:
				this.buttons.get(3).getChildren().get(0).setOnMouseClicked(click -> {this.game.getExtraScreen().setProfile();
																					GameUtils.update(this.game);});
				break;
				
			case 4:
				this.buttons.get(4).getChildren().get(0).setOnMouseClicked(click-> {this.game.getExtraScreen().setChat();
																					GameUtils.update(this.game);});
				break;
				
			case 5:
				this.buttons.get(5).getChildren().get(0).setOnMouseClicked(click-> {
																			this.game.getExtraScreen().resetChat();
																			GameUtils.update(this.game);});																							
				break;
				
			default:
				break;
			}	
		});
	}

	public void buildLabel(String string, int x, int y){
		Label label = new Label(string);
		label.setId("lorenzo_idle");
		label.setLayoutX(x);
		label.setLayoutY(y);
		label.getStylesheets().add(this.getClass().getResource("/css/game_buttons.css").toExternalForm());
		label.setOnMouseEntered(mouse->label.setId("lorenzo_Entered"));
		label.setOnMouseExited(mouse->label.setId("lorenzo_idle"));
		this.setTop(label);
	}
	
	public void buildButton(String label){
		MenuButton button = new MenuButton(label);
		VBox set = new VBox();
		set.getChildren().add(button);
		this.buttons.add(set);
	}
	
	public void loadButtons(){
		this.buttons.forEach(vbox -> this.grid.add(vbox, 0, buttons.indexOf(vbox)));
	}

	public ArrayList <VBox> getButtons(){
		return this.buttons;
	}
}

