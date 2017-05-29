package it.polimi.ingsw.GC_32.Server.Game;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

import it.polimi.ingsw.GC_32.Server.Game.Board.*;
import it.polimi.ingsw.GC_32.Server.Setup.Setup;

public class TurnManager {
	
	private int turnID;
	private Game game;
	
	public TurnManager(Game game){
		this.turnID = 1;
		this.game = game;
	}
	
	public int getTurnID(){
		return this.turnID;
	}
	
	private void diceRoll(){
		Random randomGenerator = new Random();
		game.setBlackDiceValue(1+randomGenerator.nextInt(6));
		game.setOrangeDiceValue(1+randomGenerator.nextInt(6));
		game.setWhiteDiceValue(1+randomGenerator.nextInt(6));
	}
	
	private void updateTurnOrder(){
		ArrayList<Player> oldTurnOrder = game.getPlayerList(); //vecchio ordine di turno	
		ArrayList<FamilyMember> councilRegionState = game.getBoard().getCouncilRegion().getOccupants();		
		ArrayList<Player> newTurnOrder = new ArrayList<Player>();
		
		//aggiorno stato dell'ordine di turno quardando i familiari in councilRegion
		for(FamilyMember f : councilRegionState){
			if(!newTurnOrder.contains(f.getOwner())){
				newTurnOrder.add(f.getOwner());
			}
		}
		//player che non hanno piazzato familiari nel councilregion
		for(Player p : oldTurnOrder){
			if(!newTurnOrder.contains(p)){
				newTurnOrder.add(p);
			}
		}	
		game.setPlayerOrder(newTurnOrder);		
	}
	
	private void placeCards(){
		for(TowerRegion towerRegion : this.game.getBoard().getTowerRegion()){
			for(TowerLayer towerLayer : towerRegion.getTowerLayers()){
				towerLayer.setCard(game.getDeck(towerRegion.getTypeCard()).drawElement());
			}
		}	
	}
	
	// chiede al client, secondo il protocollo di comunicazione, di effettuare una mossa
	private void performAction(Player currentPlayer){
		System.out.println(currentPlayer.getName()+" esegue mossa");
	}
	
	// controlla punti fede posseduti e se del caso attiva carta scomunica sul giocatore da scomunicare
	private void checkExcommunication(){
		int excommunicationLevel = 3 + this.turnID/2 -1 ; //calcolo punti fede richiesti 
		for(Player p : game.getPlayerList()){
			if(p.getFaithPoints()<=excommunicationLevel){
				System.out.println("TIE! beccati la scomunica!");
			}
		}
	}
	
	public void roundSetup(){
		placeCards();
		diceRoll();
	}
	
	public void actionPhase(){
		for(FamilyMember f : this.game.getPlayerList().get(0).getFamilyMember()){ //recupera il numero di familiari, meglio caricare da file di configurazione
			for(Player p : this.game.getPlayerList()){
				performAction(p);
			}
		}
	}
	public void vaticanReportPhase(){
		if(this.turnID%2==0){ //solo per turni pari
			checkExcommunication();			
		}
	}
	
	public void roundEnd(){
		updateTurnOrder();
		this.game.getBoard().flushBoard();
		this.turnID++;
	}
	
	public static void main(String[] args) throws IOException{
		
		Player a1 = new Player("aaa");
		Player a2 = new Player("bbb");
		Player a3 = new Player("ccc");
		
		
		ArrayList<Player> playerList = new ArrayList<Player>();
		playerList.add(a1);
		playerList.add(a2);
		playerList.add(a3);
		
		
		Game game = new Game(playerList);
		Setup setupGame = new Setup(game);
		
		TurnManager turnManager = new TurnManager(game);
		turnManager.roundSetup();
		
		System.out.println(game.getBoard().toString());
		System.out.println(game.getBlackDiceValue());
		
		/*while(turnManager.getTurnID()<6){
			turnManager.roundSetup();
			turnManager.actionPhase();
			turnManager.vaticanReportPhase();
			turnManager.roundEnd();
		}*/
		
	}
	
	/**
	 * solo per il turno finale
	 * - i giocatori che non hanno i punti fede richiesti, dopo aver attivato
	 *   gli effetti della scomunica, guadagnano tanti punti vittoria quanti
	 *   i punti fede posseduti
	 * - calcolo punteggio tenendo conto di eventuali scomuniche del terzo 
	 *   periodo
	 * 
	 */
	
	
}