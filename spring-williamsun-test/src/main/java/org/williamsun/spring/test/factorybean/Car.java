package org.williamsun.spring.test.factorybean;

/**
 * @Description TODO
 * @Created by gang.sun on 2020/6/12.
 */
public class Car {

	private int maxSpeed;
	private String brand;
	private double price;

	public int getMaxSpeed() {
		return maxSpeed;
	}

	public void setMaxSpeed(int maxSpeed) {
		this.maxSpeed = maxSpeed;
	}

	public String getBrand() {
		return brand;
	}

	public void setBrand(String brand) {
		this.brand = brand;
	}

	public double getPrice() {
		return price;
	}

	public void setPrice(double price) {
		this.price = price;
	}
}
