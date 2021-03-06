package it.polimi.ingsw.GC_32.Client.Game;

import java.util.ArrayList;
import java.util.Iterator;

import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonObject.Member;
import com.eclipsesource.json.JsonValue;

/**
 * this class is the client-side representation of the server-side concept of Board. Because client only show information on the screen, the information contained into 
 * this class (like all the classes of the client-side game model) is really less then the server-side equivalent class.
 * 
 * <ul>
 * <li>{@link #region}: the list of region contained into the board</li>
 * <li>{@link #excommunicationCards}: excommunication cards of the game</li>
 * <li>{@link #blackDice}: the black dice value</li>
 * <li>{@link #whiteDice}: the white dice value</li>
 * <li>{@link #orangeDice}: the orange dice value</li>
 * </ul>
 *
 * @see ClientRegion
 */

public class ClientBoard {

	private ArrayList<ClientRegion> region;
	private ArrayList<String> excommunicationCards;
	int blackDice;
	int whiteDice;
	int orangeDice;
	
	/**
	 * initialize the board whit the information contained into the JsonObject passed as argument, which is retrived from the GMSTRT message sent by the server when the 
	 * game starts
	 * @param boardPacket the JSON representation of the client-side board
	 */
	public ClientBoard(JsonObject boardPacket){
		this.region = new ArrayList<ClientRegion>();
		this.excommunicationCards =  new ArrayList<String>();
		Iterator<Member> regions = boardPacket.iterator();
		regions.forEachRemaining(region -> {
			String regionType = region.getName();
			JsonArray actionSpaces = Json.parse(region.getValue().asString()).asArray();
			
			this.region.add(new ClientRegion(regionType,actionSpaces));			
		});	
	}

	/**
	 * allows to retrive the name of the excommunication card of the game
	 * @return
	 */
	public ArrayList<String> getExcommunicationCards(){
		return this.excommunicationCards;
	}
	
	/**
	 * flush the board calling the flushFamilyMember() method on each region which compose the board
	 */
	public void flushFamilyMember(){
		region.forEach(region -> region.flushFamilyMember());
	}
	
	/**
	 * register the excommunication card of the game
	 * @param jsonList a JsonArray containing the excommunication card's name
	 */
	public void setExcommunicationCards(JsonValue jsonList){
		jsonList.asArray().forEach(card ->{
			excommunicationCards.add(card.asString());
		});	
	}
	
	/**
	 * register the value of the dices
	 * @param blackValue the black dice value
	 * @param whiteValue the white dice value
	 * @param orangeValue the orange dice value
	 */
	public void setDiceValue(int blackValue, int whiteValue, int orangeValue){
		this.blackDice = blackValue;
		this.whiteDice = whiteValue;
		this.orangeDice = orangeValue;
	}
	
	/**
	 * allows to retrive all the dice values
	 * @return an array of int which contains all the dice values
	 */
	public int[] getDiceValue(){
		return new int[]{blackDice,whiteDice,orangeDice};
	}
	
	private void fillWith(StringBuilder stringBuilder, int howManyTimes, String string){
		for(int i=0; i<howManyTimes; i++){
			stringBuilder.append(string);
		}
	}
	
	/**
	 * return a string representation of the board, nicely formatted
	 */
	public String toString(){
		ArrayList<ClientRegion> towerList = new ArrayList<ClientRegion>(region.subList(4, region.size()));
		// dimensione delle torri
		int towerWidth = 56;
		String title = "LORENZO IL MAGNIFICO";
		
		StringBuilder boardString = new StringBuilder();
		
		// title
		boardString.append("+");
		fillWith(boardString, towerWidth*towerList.size()-6,"-");
		boardString.append("+\n|");
		int titlePosition = (towerWidth*towerList.size() - title.length() -6)/2;
		fillWith(boardString, titlePosition, " ");
		boardString.append(title);
		fillWith(boardString, titlePosition, " ");
		boardString.append("|\n+");
		fillWith(boardString, towerWidth*towerList.size() - 6, "-");
		boardString.append("+\n");		
			
		// ********************************** towers
		towerList.forEach(tower -> {
			int tmpTowerWidth = towerWidth;
			boardString.append("|");
			int numberOfDashes = (tmpTowerWidth - tower.getType().length())/2 - 2;
			fillWith(boardString, numberOfDashes, "-");
			boardString.append(" "+tower.getType()+" ");
			fillWith(boardString, numberOfDashes, "-");
			boardString.append("|");
			});
		boardString.append("\n");

		int numberOfActionSpaces = towerList.get(0).getActionSpaceList().size();
		String[] infoContainerMask = {"regionID","actionSpaceID","actionValue","singleFlag","blocked","bonus","occupants","card"};
		
		for(int i=numberOfActionSpaces-1; i>=0; i--){ // actionSpace
			for(int w=0; w<infoContainerMask.length; w++){ // informations
				for(int j=0; j<towerList.size(); j++){ // tower
					String item = towerList.get(j).getActionSpaceList().get(i).getInfoContainer()[w];
					String field = infoContainerMask[w];
					boardString.append("| "+field+": "+item);
					int numberOfWhiteSpaces = towerWidth - item.length() - field.length() - 6;
					fillWith(boardString, numberOfWhiteSpaces, " ");
					boardString.append("|");
				}
				boardString.append("\n");
			}
			for(int j=0; j<towerList.size(); j++){
				boardString.append("|");
				int numberOfDashes = towerWidth - 3;
				fillWith(boardString, numberOfDashes, "-");
				boardString.append("|");
			}
			boardString.append("\n");
		}
		
		int halfBoardWidth = (towerWidth*towerList.size())/2;
		
		boardString.append("|");
		String productionTitle = region.get(0).getType();
		int numberOfDashes = (halfBoardWidth - productionTitle.length())/2 -3;
		fillWith(boardString, numberOfDashes, "-");
		boardString.append(" "+productionTitle+" ");
		fillWith(boardString, numberOfDashes, "-");
		boardString.append("||");
		
		String councilTitle = region.get(2).getType();
		numberOfDashes = (halfBoardWidth - councilTitle.length())/2 - 3;
		fillWith(boardString, numberOfDashes, "-");
		boardString.append(" "+councilTitle+" ");
		fillWith(boardString, numberOfDashes, "-");
		boardString.append("-|\n");
		
		int singleActionSpaceWidth = halfBoardWidth/3;
		int multipleActionSpaceWidth = halfBoardWidth - singleActionSpaceWidth;
		
		for(int w=0; w<infoContainerMask.length-1; w++){
			String field = infoContainerMask[w];
			String item = region.get(0).getActionSpaceList().get(0).getInfoContainer()[w];
			boardString.append("| "+field+": "+item);
			int numberOfWhiteSpaces = singleActionSpaceWidth - field.length() - item.length() - 4;
			fillWith(boardString, numberOfWhiteSpaces, " ");
			boardString.append("|");
			item = region.get(0).getActionSpaceList().get(1).getInfoContainer()[w];
			boardString.append("| "+field+": "+item);
			
			numberOfWhiteSpaces = multipleActionSpaceWidth - field.length() - item.length() - 8;
			fillWith(boardString, numberOfWhiteSpaces, " ");
			boardString.append("|");
			item = region.get(2).getActionSpaceList().get(0).getInfoContainer()[w];
			boardString.append("| "+field+": "+item);
			numberOfWhiteSpaces = halfBoardWidth - field.length() - item.length() - 7;
			fillWith(boardString, numberOfWhiteSpaces, " ");
			boardString.append("|\n");	
		}
		
		int marketActionSpaceWidth = halfBoardWidth/4 + 2;
		
		boardString.append("|");
		String harvastTitle = region.get(1).getType();
		numberOfDashes = (halfBoardWidth - harvastTitle.length())/2 -3;
		fillWith(boardString, numberOfDashes, "-");
		boardString.append(" "+harvastTitle+" ");
		fillWith(boardString, numberOfDashes, "-");
		boardString.append("-||");
		
		String marketTitle = region.get(3).getType();
		numberOfDashes = (halfBoardWidth - marketTitle.length())/2 - 3;
		fillWith(boardString, numberOfDashes, "-");
		boardString.append(" "+marketTitle+" ");
		fillWith(boardString, numberOfDashes, "-");
		boardString.append("|\n");
		
		for(int w=0; w<infoContainerMask.length-1; w++){
			String field = infoContainerMask[w];
			String item = region.get(1).getActionSpaceList().get(0).getInfoContainer()[w];
			boardString.append("| "+field+": "+item);
			int numberOfWhiteSpaces = singleActionSpaceWidth - field.length() - item.length() - 4;
			fillWith(boardString, numberOfWhiteSpaces, " ");
			boardString.append("|");
			
			item = region.get(1).getActionSpaceList().get(1).getInfoContainer()[w];
			boardString.append("| "+field+": "+item);
			numberOfWhiteSpaces = multipleActionSpaceWidth - field.length() - item.length() - 8;
			fillWith(boardString, numberOfWhiteSpaces, " ");
			boardString.append("|");
			for(int j=0; j<region.get(3).getActionSpaceList().size(); j++){
				item = region.get(3).getActionSpaceList().get(j).getInfoContainer()[w];
				boardString.append("| "+field+": "+item);
				numberOfWhiteSpaces = marketActionSpaceWidth - field.length() - item.length() - 7;
				fillWith(boardString, numberOfWhiteSpaces, " ");
			}
			boardString.append(" |\n");	
		}
		
		
		return new String(boardString);
	}
	
	/**
	 * allows to retrive the list of region which compose this board
	 * @return an ArrayList of ClientRegion
	 */
	public ArrayList<ClientRegion> getRegionList(){
		return this.region;
	}
}
