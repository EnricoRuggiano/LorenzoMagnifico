package it.polimi.ingsw.GC_32.Client.CLI;

import java.util.ArrayList;

import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonObject;

import it.polimi.ingsw.GC_32.Client.Network.ClientMessageFactory;

public class LeaderSetContext extends Context{

	private ClientCLI client;
	
	public  LeaderSetContext(ClientCLI client){ // context aperto nella distribuzione delle carte leader
		this.client = client;
	}
	
	public void open(Object object){
		int index = 0;
		runFlag=true;
		
		JsonObject jsonPayload = (JsonObject) object;
		
		JsonArray list = jsonPayload.get("LIST").asArray();
		
		ArrayList <String> cardList = new ArrayList<String>();
		list.forEach(js -> {
			cardList.add(js.asString());
		});
		System.out.println("Choose one of the following card");
			for(int i=0; i<cardList.size(); i++){
				System.out.println(i + "]" + cardList.get(i));
		}
		boolean optionSelected = false;
		System.out.println("type the index of the card you want to get");
		while(!optionSelected){	
			try{
					command = in.nextLine();	
					switch(Integer.parseInt(command)){
						case 0:
							index = 0;
							System.out.println("You choose the card: " + cardList.get(index));
							optionSelected = true;
							break;
							
						case 1:
							if(cardList.size()<1){
								System.out.println("type a valid index");
								break;
							} index = 1;
							System.out.println("You choose the card: " + cardList.get(index));
							optionSelected = true;
							break;
		
						case 2:
							if(cardList.size()<2){
								System.out.println("type a valid index");
								break;
							} index = 2;
							System.out.println("You choose the card: " + cardList.get(index));
							optionSelected = true;
							break;
							
						case 3:
							if(cardList.size()<3){
								System.out.println("type a valid index");
								break;
							} index = 3;
							System.out.println("You choose the card: " + cardList.get(index));
							optionSelected = true;
							break;
							
						default:
							System.out.println("type a valid index");
							break;
					}
			}catch(NumberFormatException e) {System.out.println("type a number, please");}
		}
		client.getPlayerList().get(client.getUUID()).addCard("LEADER", cardList.get(index));
		cardList.remove(index);
		list.remove(index);
		System.out.println("Prima di inviare al Server:" + list);
		this.sendQueue.add(ClientMessageFactory.buildLDRSETmessage(client.getGameUUID(), client.getUUID(), list));
		
	}
}