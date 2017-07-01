package it.polimi.ingsw.GC_32.Server.Game;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Random;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonValue;

import it.polimi.ingsw.GC_32.Common.Network.ContextType;
import it.polimi.ingsw.GC_32.Common.Network.GameMessage;
import it.polimi.ingsw.GC_32.Common.Game.ResourceSet;
import it.polimi.ingsw.GC_32.Server.Game.Board.Board;
import it.polimi.ingsw.GC_32.Server.Game.Board.Deck;
import it.polimi.ingsw.GC_32.Server.Game.Board.PersonalBonusTile;
import it.polimi.ingsw.GC_32.Server.Game.Board.TowerRegion;
import it.polimi.ingsw.GC_32.Server.Game.Card.CardRegistry;
import it.polimi.ingsw.GC_32.Server.Game.Card.DevelopmentCard;
import it.polimi.ingsw.GC_32.Server.Game.Card.ExcommunicationCard;
import it.polimi.ingsw.GC_32.Server.Network.MessageManager;
import it.polimi.ingsw.GC_32.Server.Network.GameRegistry;
import it.polimi.ingsw.GC_32.Server.Network.ServerMessageFactory;


public class Game implements Runnable{

	private final Logger LOGGER = Logger.getLogger(this.getClass().getName());
	
	private ArrayList<Player> playerList;
	private Board board;
	
	private HashMap<String, Deck<DevelopmentCard>> decks;	
	private ExcommunicationCard[] excommunicationCards;
	
	private int blackDice;
	private int whiteDice;
	private int orangeDice;
		
	private UUID lock;
	
	private LeaderHandler leaderHandler;
	private TurnManager turnManager;
	private MoveChecker mv;
	private ContextManager cm;
	
	// context management
	private HashMap<String, JsonValue> contextInfoContainer;
	private HashMap<UUID, Action> memoryAction;
	private final UUID gameUUID;
	
	private boolean runGameFlag = true;
	
	public Game(ArrayList<Player> players, UUID uuid){
		this.gameUUID = uuid;
		this.mv = new MoveChecker();
		this.cm = new ContextManager(this);
		MessageManager.getInstance().registerGame(this);
		
		this.memoryAction = new HashMap<>();
		contextInfoContainer = new HashMap<String, JsonValue>();
		
		LOGGER.log(Level.INFO, "setting up game...");
		this.playerList = players;
		this.board = new Board();
		this.turnManager = new TurnManager(this);
		LOGGER.log(Level.INFO, "loading cards...");
		this.decks = new HashMap<String, Deck<DevelopmentCard>>(CardRegistry.getInstance().getDevelopmentDecks());
		this.excommunicationCards = new ExcommunicationCard[3];	
		for(int i=0; i<3; i++){
			this.excommunicationCards[i] = CardRegistry.getInstance().getDeck(i+1).drawRandomElement();
		}
		
		LOGGER.log(Level.INFO, "decks succesfprivateully loaded");
		
		LOGGER.log(Level.INFO, "setting up players resources");

		// assegnazione casuale bonusTile
		ArrayList<PersonalBonusTile> bonusTile = GameConfig.getInstance().getBonusTileList();
		ArrayList<Integer> list = new ArrayList<Integer>();
		for(int k=0; k<bonusTile.size(); k++){
			list.add(new Integer(k));
		}
		Collections.shuffle(list);
		
		for(int i=0,j=0; i<playerList.size(); i++,j++){
			playerList.get(i).registerGame(this.gameUUID);
			playerList.get(i).getResources().setResource("WOOD", 2);
			playerList.get(i).getResources().setResource("STONE", 2);
			playerList.get(i).getResources().setResource("SERVANTS", 3);
			// in base all'ordine di turno assegno le monete iniziali
			playerList.get(i).getResources().setResource("COINS", 5 + i);
			// setta punteggi a 0
			playerList.get(i).getResources().setResource("FAITH_POINTS", 0);
			playerList.get(i).getResources().setResource("VICTORY_POINTS", 0);
			playerList.get(i).getResources().setResource("MILITARY_POINTS", 0);
	
			playerList.get(i).setPersonalBonusTile(bonusTile.get(list.get(j)));
		}
		this.leaderHandler = new LeaderHandler(this);
		LOGGER.log(Level.INFO, "done");
	}
	
	public void run(){
		LOGGER.log(Level.INFO, "notifying connected players on game settings...");
		MessageManager.getInstance().sendMessge(ServerMessageFactory.buildGMSTRTmessage(this));

		playerList.forEach(player -> {
			MessageManager.getInstance().sendMessge(ServerMessageFactory.buildSTATCHNGmessage(this, player));
		});
		
		// do tempo ai thread di rete di spedire i messaggi in coda
		try {
			Thread.sleep(200);
		} catch (InterruptedException e) {}
		LOGGER.log(Level.INFO, "done");	
		
		LOGGER.log(Level.INFO, "ready to play");
		this.board.placeCards(this);
		MessageManager.getInstance().sendMessge(ServerMessageFactory.buildCHGBOARDSTATmessage(this, getBoard()));
		LOGGER.log(Level.INFO, "notified players on card layout");
		
		diceRoll();
		LOGGER.log(Level.INFO, "dice rolled");
		MessageManager.getInstance().sendMessge(ServerMessageFactory.buildDICEROLLmessage(this, blackDice, whiteDice, orangeDice));
		
		// do tempo ai thread di rete di spedire i messaggi in coda
		try {
			Thread.sleep(200);
		} catch (InterruptedException e) {}
		
	////-------------------------LEADER DISTRIBUTION ----------------////////
			LOGGER.log(Level.INFO, "giving lock to the first player...");
			if(true){  // da settare se si vuole giocare con le carte Leader
				boolean flag = true;
				while(leaderHandler.getRunning()){ // da settare se si vuole giocare con le carte Leader
					if(flag){
						LOGGER.log(Level.INFO, "leader rule activated. Distribuiting card");
						Player firstPlayer = this.playerList.get(0);
						setLock(firstPlayer.getUUID());
						leaderHandler.leaderPhase(firstPlayer);
						flag = false; // blocca l'accesso al primo send;
					}
					//ricezione messaggi
					GameMessage message = null;
					
					try{
						message = MessageManager.getInstance().getQueueForGame(this.gameUUID).take();
					} catch(InterruptedException e){
						Thread.currentThread().interrupt();
						LOGGER.log(Level.FINEST, "InterruptedException when taking packet", e);
					}
			
					if(message != null){
						JsonObject Jsonmessage = message.getMessage().asObject();
						switch(message.getOpcode()){
						case "LDRSET":
							JsonArray json = Jsonmessage.get("LIST").asArray();
							//setto la lista
							leaderHandler.setList(GameRegistry.getInstance().getPlayerFromID(message.getPlayerUUID()), json);
							int index = leaderHandler.getIndex(GameRegistry.getInstance().getPlayerFromID(message.getPlayerUUID()))+1;
							System.out.println("TURNO: " + leaderHandler.getTurn() + "\nPlayerIndice: " + (index -1));
							
							if(index < playerList.size()){   						///STACK 1
								setLock(playerList.get(index).getUUID());
								leaderHandler.leaderPhase(playerList.get(index));
								break;
							}
							if(index == playerList.size() && leaderHandler.getTurn() < 3 ){   /// STACK 2
								leaderHandler.addTurn();
								setLock(playerList.get(0).getUUID());
								leaderHandler.leaderPhase(playerList.get(0)); // ricomincia con il primo
								break;
							}
							if(index == playerList.size() && leaderHandler.getTurn() == 3){  // sono 4 turni per 4 carte
								leaderHandler.setInactive();
								break;
							}
						}
					}
				}
			}
			//setLock(playerList.get(0).getUUID()); // inizio con il Primo player
			setLock(turnManager.nextPlayer());
			LOGGER.log(Level.INFO, "player "+getLock()+" has the lock");
			
		///----------------------FINE LEADER DISTRIBUTION--------------------------------------///////////
		
		// ask action
		MessageManager.getInstance().sendMessge(ServerMessageFactory.buildTRNBGNmessage(this, getLock()));
		GameMessage message = null;
		
		while(runGameFlag){
			try{
				message = MessageManager.getInstance().getQueueForGame(this.gameUUID).take();
			} catch(InterruptedException e){
				Thread.currentThread().interrupt();
				LOGGER.log(Level.FINEST, "InterruptedException when taking packet", e);
			}

			if(message != null){
				JsonObject Jsonmessage = message.getMessage().asObject();
				switch(message.getOpcode()){
					case "ASKACT":
						LOGGER.log(Level.INFO, "processing ASKACT message from "+message.getPlayerID());
						int index = playerList.indexOf(GameRegistry.getInstance().getPlayerFromID(message.getPlayerUUID())); 
						int pawnID = Jsonmessage.get("FAMILYMEMBER_ID").asInt();
						int actionValue = GameRegistry.getInstance().getPlayerFromID(message.getPlayerUUID())
																	.getFamilyMember()[pawnID].getActionValue();
	
						int regionID = Jsonmessage.get("REGIONID").asInt();
						int spaceID = Jsonmessage.get("SPACEID").asInt();
						String actionType = Jsonmessage.get("ACTIONTYPE").asString();
	
						Action action = new Action(actionType,actionValue,regionID,spaceID);
						action.setAdditionalInfo(new JsonObject().add("FAMILYMEMBER_ID", Jsonmessage.get("FAMILYMEMBER_ID").asInt()));
						action.getAdditionalInfo().add("COSTINDEX", Jsonmessage.get("COSTINDEX").asInt()); // Cost Index
						action.getAdditionalInfo().add("CARDNAME", Jsonmessage.get("CARDNAME").asString());
						
						Player player = playerList.get(index);
						memoryAction.put(player.getUUID(), action);
						
						System.out.println("INIZIO CHECK: ");
						System.out.println("STATO PRIMA DELL'ESECUZIONE:");
						System.out.println(action);
						System.out.println(player);
			    		if(mv.checkMove(this, player, action, cm)){
			    			System.out.println("check with copy: PASSATO");
			    			makeMove(player, action);
			    			System.out.println("AZIONE ESEGUITA!\n");
							System.out.println("STATO DOPO AZIONE: ");
			    			System.out.println(player);
			    			
			    			// notifiche server
			    			MessageManager.getInstance().sendMessge(ServerMessageFactory.buildACTCHKmessage(this, player, action, true));
			    		} else {
		    				MessageManager.getInstance().sendMessge(ServerMessageFactory.buildACTCHKmessage(this, player, action, false));
			    		}
		    			break;
				//RAPPORTO AL VATICANO
					case "SENDPOPE":
						LOGGER.info("ricevo risposte dal rapporto in vaticano [GAME]");
						boolean answer = Jsonmessage.get("ANSWER").asBoolean();
						int points = Jsonmessage.get("FAITH_NEEDED").asInt();
						int playerIndex= playerList.indexOf(GameRegistry.getInstance().getPlayerFromID(message.getPlayerUUID())); 
						if(answer){ 
							
							//ATTIVAZIONE CARTA SCOMUNICA
							System.out.println("FIGLIOLO...IL PAPA TI HA SCOMUNICATO, MI SPIACE");
							ExcommunicationCard card = this.excommunicationCards[this.turnManager.getPeriod()];
							System.out.println("Attivo effetto carta: " +card.getName());
							
							if(!card.getInstantEffect().isEmpty()){
								card.getInstantEffect().get(0).apply(getBoard(), playerList.get(playerIndex), null, null);
							}
							else 
								System.out.println("Non ha effetti instantanei!");
							if(!card.getPermanentEffect().isEmpty()){
								playerList.get(playerIndex).addEffect(card.getPermanentEffect().get(0));
							}
							else
								System.out.println("Non ha effetti permanenti!");
						}	
						else{
							System.out.println("Sostegno alla Chiesa!");
							int faithScore = playerList.get(playerIndex).getResources().getResource("FAITH_POINTS");	
							System.out.println("Punti Fede Giocatore: " + faithScore);
							
							playerList.get(playerIndex).getResources().addResource("FAITH_POINTS", -faithScore); //azzera punteggio player
							int victoryPointsConverted = 0;
							Reader scoreJs = new InputStreamReader(this.getClass().getClassLoader().getResourceAsStream("excommunication_track.json"));
							try {
								JsonObject excommunicationJson = Json.parse(scoreJs).asObject();	
								try{
									victoryPointsConverted += excommunicationJson.get(Integer.toString(faithScore)).asInt();
								}catch(NullPointerException e){
									victoryPointsConverted = faithScore*2; // caso faithPoints > 15
								}
							} catch (IOException e) {} 
							if(playerList.get(playerIndex).isFlagged("MOREFAITH")){  // Sisto IV
								victoryPointsConverted += 5;
							}
							System.out.println("Punti Vittoria convertiti Giocatore: " + victoryPointsConverted);
							playerList.get(playerIndex).getResources().addResource("VICTORY_POINTS", victoryPointsConverted);
						}
						break;	
					//LEADER ACTION
					case "ASKLDRACT":
						String cardName = Jsonmessage.get("LEADERCARD").asString();
						String decision = Jsonmessage.get("DECISION").asString();
						System.out.println("LEADERCARD: " +cardName);
						System.out.println("DECISION:" + decision);
						Player p = GameRegistry.getInstance().getPlayerFromID(message.getPlayerUUID());
						boolean result;
						if(LeaderUtils.checkLeaderMove(p.getUUID(), cardName, decision)){
							System.out.println("ATTIVATO!");
							result = true;
							if(decision.equals("DISCARD")){	//GUADAGNA UN PRIVILEGIO
								cm.openContext(ContextType.PRIVILEGE, p, null, Json.value(1));
								JsonValue COUNCILPRIVILEGEresponse = cm.waitForContextReply();
								
								System.out.println("PRIMA DEL PRIVILEGE:\n" + GameRegistry.getInstance().getPlayerFromID(getLock()));
								GameRegistry.getInstance().getPlayerFromID(getLock()).getResources().addResource("COINS", 1);
								GameRegistry.getInstance().getPlayerFromID(getLock()).getResources().addResource( new ResourceSet(Json.parse(COUNCILPRIVILEGEresponse.asArray().get(0).asString()).asObject()));
								System.out.println("DOPO DEL PRIVILEGE:\n" + GameRegistry.getInstance().getPlayerFromID(getLock()));
							}
						}
						else{
							System.out.println("QUALCOSA NON VA!\n NON PUOI ATTIVARE QUESTA AZIONE LEADER!\n");
							result = false;
						}
						MessageManager.getInstance().sendMessge(ServerMessageFactory
								.buildASKLDRACKmessage(this, p, cardName, decision, result));		
						break;
					case "TRNEND":
						MessageManager.getInstance().sendMessge(ServerMessageFactory.buildCHGBOARDSTATmessage(this, getBoard()));
						MessageManager.getInstance().sendMessge(ServerMessageFactory.buildSTATCHNGmessage(this, GameRegistry.getInstance().getPlayerFromID(getLock())));
						MessageManager.getInstance().sendMessge(ServerMessageFactory.buildCHGBOARDSTATmessage(this, getLock().toString(), memoryAction.get(getLock())));
						
						try{ // wait for TRNBGN message
						Thread.sleep(200);
						}catch(InterruptedException e){}
						
						memoryAction.remove(getLock());
						
						LOGGER.info("ricevo turn end [GAME]");
						System.out.println("ROUND ID: "+ turnManager.getRoundID());
						System.out.println("PERIOD ID: "+ turnManager.getRoundID()/2);
						System.out.println("TURN ID: "+ turnManager.getTurnID());
						if(!turnManager.isGameEnd()){
							LOGGER.log(Level.INFO, message.getPlayerID()+" has terminated his turn");
							if(turnManager.isRoundEnd()){
							//if(true){  DEBUG
								LOGGER.log(Level.INFO, "round end");
								
								if(turnManager.isPeriodEnd()){
							//	if(true){	DEBUG
									LOGGER.log(Level.INFO, "period "+turnManager.getRoundID()/2+" finished");
									int excommunicationLevel = 3 + turnManager.getTurnID()/2 -1 ; //calcolo punti fede richiesti 
									
									Player pl = GameRegistry.getInstance().getPlayerFromID(message.getPlayerUUID());
									MessageManager.getInstance().sendMessge(ServerMessageFactory
													  .buildCONTEXTmessage(this, pl, ContextType.EXCOMMUNICATION, 
															excommunicationLevel,
															pl.getResources().getResource("FAITH_POINTS")));

									try{ // wait for TRNBGN message
										Thread.sleep(200);
									}catch(InterruptedException e){}
										
								}
								LOGGER.log(Level.INFO, "giving lock to the next player");
								UUID nextPlayer = turnManager.nextPlayer();
								
								//Skip Turn  ///DA TESTARE E NON FUNZIONA A COLPO D'OCCHIO
								for(int i = 0; i < this.playerList.size(); i++){
									if(!GameRegistry.getInstance().getPlayerFromID(nextPlayer)
																	  .isFlagged("SKIPTURN")){
										break;
									}
									nextPlayer = turnManager.nextPlayer();
								}
								setLock(nextPlayer);
								LOGGER.log(Level.INFO, "player "+getLock()+" has the lock");
								// ask action
								MessageManager.getInstance().sendMessge(ServerMessageFactory.buildTRNBGNmessage(this, getLock()));
							}else{
								LOGGER.log(Level.INFO, "giving lock to the next player");
								setLock(turnManager.nextPlayer());
								LOGGER.log(Level.INFO, "player "+getLock()+" has the lock");
								// ask action
								MessageManager.getInstance().sendMessge(ServerMessageFactory.buildTRNBGNmessage(this, getLock()));
							}
						} else {
							LOGGER.log(Level.INFO, "game end");
							EndPhase.endGame(this);
							//stopGame();
						}
						break;
				}
			}
		}
	}
	
	public void makeMove(Player player, Action action){
		
		System.out.println(contextInfoContainer.isEmpty());
		
		System.out.println("PRIMA DEGLI EFFETTI PERMANENTI:\n" + action);
		MoveUtils.applyEffects(this.board, player, action, cm);
		System.out.println("DOPO GLI EFFETTI PERMANENTI:\n" + action);
		System.out.println("PRIMA DEL BONUS:\n" + player);
		MoveUtils.addActionSpaceBonus(this.board, player, action);
		System.out.println("DOPO DEL BONUS:\n" + player);
		moveFamiliar(this.board, player, action);
		
		switch(action.getActionType()){
			case "PRODUCTION":
				player.getResources().addResource(player.getPersonalBonusTile().getPersonalProductionBonus()); 
				cm.openContext(ContextType.SERVANT, player, action, null);
				
				JsonValue SERVANTProductionresponse = cm.waitForContextReply();
				action.setActionValue(action.getActionValue() + SERVANTProductionresponse.asObject().get("CHOOSEN_SERVANTS").asInt());
				
				JsonArray CHANGEcontextPayload = new JsonArray();
				JsonArray CHANGEnameCardArray = new JsonArray();
				
				ArrayList<DevelopmentCard> CHANGEeffectCardList = new ArrayList<DevelopmentCard>();
				ArrayList<DevelopmentCard> notCHANGEeffectCardList = new ArrayList<DevelopmentCard>();
				
				player.getPersonalBoard().getCardsOfType("BUILDINGCARD").forEach(card -> {
					if(card.getMinimumActionvalue() <= action.getActionValue()){ 
						// cards with CHANGE effect
						if(card.getPermanentEffectType().contains("CHANGE")){
							CHANGEeffectCardList.add(card);
							card.getPayloadInfo().forEach(payload -> {
								CHANGEcontextPayload.add(payload);
								CHANGEnameCardArray.add(card.getName());
							});
						}else{
							notCHANGEeffectCardList.add(card);
						}
					}
				});
				JsonArray CHANGEpacket = new JsonArray();
				CHANGEpacket.asArray().add(CHANGEnameCardArray);
				CHANGEpacket.asArray().add(CHANGEcontextPayload);
				
				// c'è almeno una carta con effetto CHANGE
				if(!CHANGEeffectCardList.isEmpty()){
					cm.openContext(ContextType.CHANGE, player, action, CHANGEpacket);
					
					JsonArray indexResponse = cm.waitForContextReply().asObject().get("CHANGEIDARRAY").asArray();
					for(int i=0; i<CHANGEeffectCardList.size(); i++){
						action.getAdditionalInfo().set("CHANGEID", indexResponse.get(i));
						CHANGEeffectCardList.get(i).getPermanentEffect().forEach(effect -> effect.apply(board, player, action, cm));
					}
				}
				notCHANGEeffectCardList.forEach(card -> { 
					card.getPermanentEffect().forEach(effect -> {
						effect.apply(board, player, action, cm);
					});
				});
				break;
			case "HARVEST":				
				player.getResources().addResource(player.getPersonalBonusTile().getPersonalProductionBonus()); 
				cm.openContext(ContextType.SERVANT, player, action, null);
				
				JsonValue SERVANTHarvestresponse = cm.waitForContextReply();
				action.setActionValue(action.getActionValue() + SERVANTHarvestresponse.asObject().get("CHOOSEN_SERVANTS").asInt());
				
				player.getPersonalBoard().getCardsOfType("TERRITORYCARD").forEach(card -> {
					if(card.getMinimumActionvalue() <= action.getActionValue()){ 
						card.getPermanentEffect().forEach(effect -> effect.apply(board, player, action, cm));
					}
				});
				break;
			case "COUNCIL":
				cm.openContext(ContextType.PRIVILEGE, player, action, Json.value(1));
				JsonValue COUNCILPRIVILEGEresponse = cm.waitForContextReply();
				
				System.out.println(COUNCILPRIVILEGEresponse.toString());
				
				System.out.println("PRIMA DEL PRIVILEGE:\n" + player);
				player.getResources().addResource("COINS", 1);
				player.getResources().addResource( new ResourceSet(COUNCILPRIVILEGEresponse.asArray().get(0).asObject()));
				System.out.println("DOPO DEL PRIVILEGE:\n" + player);
				break;
			case "MARKET":
				if(action.getActionSpaceId() == 3){
					cm.openContext(ContextType.PRIVILEGE, player, action, Json.value(2));
					JsonValue MARKETPRIVILEGEresponse = cm.waitForContextReply();

					player.getResources().addResource( new ResourceSet(Json.parse(MARKETPRIVILEGEresponse.asArray().get(0).asString()).asObject()));
					player.getResources().addResource( new ResourceSet(Json.parse(MARKETPRIVILEGEresponse.asArray().get(1).asString()).asObject()));
				}
				break;
			default:
				TowerRegion selectedTower = (TowerRegion)(board.getRegion(action.getRegionId()));
				DevelopmentCard card = selectedTower.getTowerLayers()[action.getActionSpaceId()].getCard();
				takeCard(this.board, player, action);
				
				if(card.getType().equals("CHARACTERCARD")){
					if(card.getPermanentEffect()!= null){
						card.getPermanentEffect().forEach(effect -> player.addEffect(effect));
						System.out.println("AGGIUNTO EFFETTO PERMANENTE");
					}	
				}
				if(!card.getInstantEffect().isEmpty()){
					card.getInstantEffect().forEach(effect -> effect.apply(board, player, action, cm));
				}
				break;
		}
		
	}
	
	public TurnManager getTurnManager(){
		return this.turnManager;
	}
	
	public ArrayList<Player> getPlayerList(){
		return this.playerList;
	}
	
	public void setPlayerOrder(ArrayList<Player> playerList){
		this.playerList = playerList;
	}
	
	public Board getBoard(){
		return this.board;
	}
	
	public UUID getLock(){
		return this.lock;
	}
	
	public UUID getUUID(){
		return this.gameUUID;
	}
	
	public Deck<DevelopmentCard> getDeck(String type){
		return this.decks.get(type);
	}
	
	public void setDeck(String type, Deck<DevelopmentCard> deck){
		this.decks.put(type, deck);
	}
	
	public void setExcommunicationCard(ExcommunicationCard card, int period){
		this.excommunicationCards[period-1] = card;
	}
	
	public ExcommunicationCard getExcommunicationCard(int period){
		return this.excommunicationCards[period-1];
	}
	
	public ExcommunicationCard[] getExcommunicationCard(){
		return this.excommunicationCards;
	}
	
	public void setLock(UUID player){
		this.lock = player;
	}
	
	private void diceRoll(){
		Random randomGenerator = new Random();
		this.blackDice = 1+randomGenerator.nextInt(6);
		this.orangeDice = 1+randomGenerator.nextInt(6);
		this.whiteDice = 1+randomGenerator.nextInt(6);
		playerList.forEach(player -> {
			player.getFamilyMember()[1].setActionValue(this.blackDice);
			player.getFamilyMember()[2].setActionValue(this.whiteDice);
			player.getFamilyMember()[3].setActionValue(this.orangeDice);
		});
	}
	
	public void moveFamiliar(Board board, Player player, Action action){
		
		MoveUtils.checkServants(board, player, action); // subtract the servants
		MoveUtils.checkCoinForTribute(board, player, action); // pays the 3 coins if the tower is busy
		
		int pawnID = action.getAdditionalInfo().get("FAMILYMEMBER_ID").asInt();
		player.moveFamilyMember(pawnID, action, board); // calls: player's moveFamilyMember and sets the position of this familyMember
															// calls: action's space addFamilyMember and sets this familymember as an occupant.
	}
	
	public void takeCard(Board board, Player player, Action action){
		 // calls: player's moveFamilyMember and sets the position of this familyMember
		MoveUtils.checkCardCost(board, player, action); // pays the cost of the card
		player.takeCard(board, action);													// calls: action's space addFamilyMember and sets this familymember as an occupant.
	}
	
	
}
