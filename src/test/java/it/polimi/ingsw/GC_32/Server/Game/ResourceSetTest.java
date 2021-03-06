package it.polimi.ingsw.GC_32.Server.Game;

import static org.junit.Assert.*;

import org.junit.Test;

import com.eclipsesource.json.JsonObject;

import it.polimi.ingsw.GC_32.Common.Game.ResourceSet;

public class ResourceSetTest{
	public ResourceSet resourceSet;
	
	@Test
	public void checkResourceSetNotNull(){
		this.resourceSet = new ResourceSet();
		assertNotNull(this.resourceSet.getResourceSet());
	}
	
	@Test
	public void checkResourceSetting(){
		this.resourceSet = new ResourceSet();
		this.resourceSet.setResource("WOOD", 10);
		assertEquals(this.resourceSet.getResource("WOOD"), 10);
	}
	
	@Test
	public void checkResourceIntAdding(){
		this.resourceSet= new ResourceSet();
		this.resourceSet.setResource("WOOD", 10);
		this.resourceSet.addResource("WOOD", 5);
		assertEquals(this.resourceSet.getResource("WOOD"), 15);
		this.resourceSet.addResource("STONE", 17);
		assertEquals(this.resourceSet.getResource("STONE"), 17);
	}
	
	@Test
	public void checkResourceSetAdding(){
		this.resourceSet= new ResourceSet();
		this.resourceSet.setResource("WOOD", 10);
		ResourceSet newResource = new ResourceSet();
		newResource.setResource("WOOD", 5);
		this.resourceSet.addResource(newResource);
		assertEquals(this.resourceSet.getResource("WOOD"), 15);
	}
	
	@Test
	public void checkResourceSetJson(){
		JsonObject testObject = new JsonObject();
		testObject.add("WOOD", 22);
		this.resourceSet = new ResourceSet(testObject);
		assertEquals(this.resourceSet.getResource("WOOD"), 22);
	}
	
	@Test
	public void checkToStringNotNull(){
		this.resourceSet= new ResourceSet();
		assertNotNull(this.resourceSet.toString());
	}
	
	@Test
	public void checkEqualResource(){
		this.resourceSet= new ResourceSet();
		this.resourceSet.setResource("WOOD", 5);
		this.resourceSet.setResource("COIN", 4);
		this.resourceSet.setResource("STONE", 3);
		ResourceSet newResource = new ResourceSet();
		newResource.setResource("WOOD", 5);
		newResource.setResource("COIN", 4);
		newResource.setResource("STONE", 3);		
		assertEquals(true, this.resourceSet.equals(newResource));
	}
	
	@Test
	public void checkNotEqualResource(){
		this.resourceSet = new ResourceSet();
		this.resourceSet.setResource("WOOD", 1);
		this.resourceSet.setResource("COIN", 10);
		ResourceSet newResource = new ResourceSet();
		newResource.setResource("WOOD", 1);
		newResource.setResource("COIN", 10);
		newResource.setResource("STONE", 3);
		assertEquals(false, this.resourceSet.equals(newResource));
	}
	
	@Test
	public void checkEqualNotResource(){
		this.resourceSet = new ResourceSet();
		assertFalse(this.resourceSet.equals("Test"));
	}
	
	@Test
	public void checkCompareToEqualResource(){
		this.resourceSet = new ResourceSet();
		this.resourceSet.setResource("WOOD", 5);
		this.resourceSet.setResource("COIN", 4);
		this.resourceSet.setResource("STONE", 3);
		ResourceSet newResource = new ResourceSet();
		newResource.setResource("WOOD", 5);
		newResource.setResource("COIN", 4);
		newResource.setResource("STONE", 3);		
		assertEquals(0, this.resourceSet.compareTo(newResource));
	}
	
	@Test
	public void checkCompareToMoreResource(){
		this.resourceSet = new ResourceSet();
		this.resourceSet.setResource("WOOD", 10);
		this.resourceSet.setResource("COIN", 100);
		this.resourceSet.setResource("STONE", 1);
		this.resourceSet.setResource("MILITARY_POINTS", 4);
		ResourceSet newResource = new ResourceSet();
		newResource.setResource("WOOD", 10);
		newResource.setResource("COIN", 100);
		newResource.setResource("MILITARY_POINTS", 3);
		assertEquals(1, this.resourceSet.compareTo(newResource));
	}

	@Test
	public void checkCompareToLessResource(){
		this.resourceSet = new ResourceSet();
		this.resourceSet.setResource("WOOD", 10);
		this.resourceSet.setResource("COIN", 100);
		this.resourceSet.setResource("STONE", 1);
		this.resourceSet.setResource("MILITARY_POINTS", 4);
		ResourceSet newResource = new ResourceSet();
		newResource.setResource("WOOD", 1);
		newResource.setResource("COIN", 1);
		newResource.setResource("MILITARY_POINTS", 100);
		assertEquals(-1, this.resourceSet.compareTo(newResource));
	}
	
	@Test
	public void checkDifferentResource(){
		this.resourceSet = new ResourceSet();
		this.resourceSet.setResource("WOOD", 10);
		ResourceSet newResource = new ResourceSet();
		newResource.setResource("STONE", 1);
		assertEquals(-2, this.resourceSet.compareTo(newResource));
	}
	
	@Test
	public void checkHashCode(){
		this.resourceSet = new ResourceSet();
		this.resourceSet.addResource("WOOD", 10);
		ResourceSet resourceSet2 = new ResourceSet();
		resourceSet2.addResource("WOOD", 10);
		assertEquals(resourceSet2.hashCode(), this.resourceSet.hashCode());
	}
}
