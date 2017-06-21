package it.polimi.ingsw.GC_32.Client.CLI;

import java.util.HashMap;
import java.util.Scanner;
import java.util.concurrent.ConcurrentLinkedQueue;

import com.eclipsesource.json.JsonObject;

import it.polimi.ingsw.GC_32.Client.ClientInterface;
import it.polimi.ingsw.GC_32.Client.Game.ClientBoard;
import it.polimi.ingsw.GC_32.Client.Game.ClientPlayer;

public class ClientCLI implements ClientInterface{

	// game management
	private ClientBoard boardReference;
	private HashMap<String, ClientPlayer> playerListReference;
	private String UUID;
	
	// network management
	private ConcurrentLinkedQueue<Object> contextQueue;
	private ConcurrentLinkedQueue<String> sendQueue;
	
	// context management
	private Context[] contextList;
	private Thread zeroLevelContextThread;
	
	private boolean idleRun = false;
	
	public ClientCLI(){
		contextQueue = new ConcurrentLinkedQueue<Object>();
		
		this.contextList = new Context[5];
		contextList[0] = new ZeroLevelContext(this); 
		contextList[1] = new PrivilegeContext();
		
	}
	
	public void registerBoard(ClientBoard board){
		this.boardReference = board;
	}
	
	public void registerPlayers(HashMap<String,ClientPlayer> playerList){
		this.playerListReference = playerList;
	}
	
	public void registerUUID(String UUID){
		this.UUID = UUID;
	}
	
	public ClientBoard getBoard(){
		return this.boardReference;
	}
	
	public HashMap<String, ClientPlayer> getPlayerList(){
		return this.playerListReference;
	}
	
	public void registerSendMessageQueue(ConcurrentLinkedQueue<String> queue) {
		this.sendQueue = queue;
		if(this.sendQueue!=null) System.out.println("sendQueue registered");
		
	}
	
	public void displayMessage(String message){
		System.out.println(message);
	}
	
	public void run(){	
		while(true){
			if(!idleRun){
				idleRun=true;
				zeroLevelContextThread = new Thread((Runnable) contextList[0]);
				zeroLevelContextThread.start();
			}
			
			while(!contextQueue.isEmpty()){
				contextList[0].close();
				JsonObject contextMessage = (JsonObject) contextQueue.poll();
				contextList[contextMessage.get("CONTEXTID").asInt()].registerSendQueue(sendQueue);
				contextList[contextMessage.get("CONTEXTID").asInt()].open(null);
				idleRun=false;
			}
		}
	}
	
	@Override
	public void openContext(JsonObject contextMessage){
		this.contextQueue.add(contextMessage);
	}
	
	@Override
	public int getContextId() {
		// TODO Auto-generated method stub
		return 0;
	}

	// change context
	@Override
	public void openContext(int screenId, String additionalData) {
		
	}	
	
	@Override
	public void registerContextPayloadQueue(ConcurrentLinkedQueue<Object> queue) {
		
	}

	@Override
	public void receiveMessage(String playerID, String message) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setTowerCards(int towerID, String[] cardArray) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void updateTurnOrder(String[] playerIDs) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setDiceValue(int blackDice, int whiteDice, int orangeDice) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void enableSpace(int regionID, int spaceID) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void disableSpace(int regionID, int spaceID) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void moveFamiliar(int familiar, int regionID, int spaceID) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void moveCardToPlayer(String playerID, int regionID, int spaceID) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setTrackValue(String playerID, int trackID, int value) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setCurrentPlayer(String playerID) {
		// TODO Auto-generated method stub
		
	}
	
}