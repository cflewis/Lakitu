package edu.ucsc.eis.mario.events;

import org.apache.commons.lang.builder.ToStringBuilder;

public class BulletBillSpawn {
 	private int cannonId;
	
	public BulletBillSpawn(int cannonId) {
		this.cannonId = cannonId;
	}
	
	public int getCannonId() {
		return cannonId;
	}
	
	@Override
	public String toString() {
		return new ToStringBuilder(this).
			append(this.cannonId).
			toString();
	}
}
