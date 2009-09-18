package edu.ucsc.eis.mario.events;

import org.apache.commons.lang.builder.ToStringBuilder;

import edu.ucsc.eis.mario.LevelScene;
import edu.ucsc.eis.mario.MarioComponent;
import edu.ucsc.eis.mario.sprites.BulletBill;

public class BulletBillSpawn implements Comparable<BulletBillSpawn> {
 	private int cannonId;
 	private long spawnTime;
 	private BulletBill bill;
	
 	public BulletBillSpawn(BulletBill bill, int cannonId) {
 		this(bill, cannonId, MarioComponent.getClockTime());
 	}
 	
	public BulletBillSpawn(BulletBill bill, int cannonId, long spawnTime) {
		this.cannonId = cannonId;
		this.bill = bill;
		this.spawnTime = spawnTime;
	}
	
	public int getCannonId() {
		return cannonId;
	}
	
	public long getSpawnTime() {
		return spawnTime;
	}
	
	@Override
	public String toString() {
		return new ToStringBuilder(this).
			append("Cannon ID:", this.cannonId).
			append("Spawn time:", this.spawnTime).
			toString();
	}

	public int compareTo(BulletBillSpawn o) {
		// TODO Auto-generated method stub
		if (this.spawnTime < o.spawnTime) { return -1; }
		else if (this.spawnTime > o.spawnTime) { return 1; }
		return 0;
	}

	public BulletBill getBulletBill() {
		return bill;
	}
}
