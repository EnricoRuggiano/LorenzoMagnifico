package it.polimi.ingsw.GC_32.Server.Game.Board;

/**
 * represent the MarketRegion of the board
 * 
 * @see Region
 *
 */
public class MarketRegion extends Region  {
	
	/**
	 * initialize this region with the given regionID
	 * @param regionID the ID of this region
	 */
	public MarketRegion(int regionID){
		super(regionID,4);
		super.getTrack()[0] = new ActionSpace(null, 1, true, this.getRegionID(), 0);
		super.getTrack()[1] = new ActionSpace(null, 1, true, this.getRegionID(), 1);
		super.getTrack()[2] = new ActionSpace(null, 1, true, this.getRegionID(), 2);
		super.getTrack()[3] = new ActionSpace(null, 1, true, this.getRegionID(), 3);
	}
	
	public void activateEffect(){
		
	}
}
