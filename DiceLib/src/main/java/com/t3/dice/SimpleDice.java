package com.t3.dice;

import java.util.Random;

public class SimpleDice implements Dice {

	protected int count;
	protected int type;
	protected int[] results;
	protected int result;

	public SimpleDice(int count, int type) {
		this.count=count;
		this.type=type;
	}

	public void rollDice(Random r) {
		results=new int[count];
		result=0;
		
		for(int i=0;i<count;i++) {
			results[i]=r.nextInt(type)+1;
			result+=results[i];
		}
	}
	
	@Override
	public String toString() {
		return count+"d"+type;
	}

	public int getResult() {
		return result;
	}

	public int[] getResults() {
		return results;
	}
	
	public boolean isRolled() {
		return results!=null;
	}
}