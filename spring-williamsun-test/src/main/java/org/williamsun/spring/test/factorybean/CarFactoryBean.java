package org.williamsun.spring.test.factorybean;

import org.springframework.beans.factory.FactoryBean;

/**
 * @Description TODO
 * @Created by gang.sun on 2020/6/12.
 */
public class CarFactoryBean implements FactoryBean<Car> {

	private String carInfo;

	@Override
	public Car getObject() throws Exception {
		Car car = new Car();
		String[] infos = carInfo.split(",");
		car.setBrand(infos[0]);
		car.setMaxSpeed(Integer.valueOf(infos[1]));
		car.setPrice(Double.valueOf(infos[2]));
		return car;
	}

	@Override
	public Class<?> getObjectType() {
		return Car.class;
	}

	@Override
	public boolean isSingleton() {
		return false;
	}

	public String getCarInfo() {
		return carInfo;
	}

	public void setCarInfo(String carInfo) {
		this.carInfo = carInfo;
	}
}
