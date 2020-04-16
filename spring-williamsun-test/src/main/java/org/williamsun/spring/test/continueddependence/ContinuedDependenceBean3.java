package org.williamsun.spring.test.continueddependence;

/**
 * @Description TODO
 * @Created by gang.sun on 2020/4/16.
 */
public class ContinuedDependenceBean3 {


	private ContinuedDependenceBean1 a;

	private ContinuedDependenceBean3(ContinuedDependenceBean1 a) {
		this.a = a;
	}

	public ContinuedDependenceBean1 getA() {
		return a;
	}

	public void setA(ContinuedDependenceBean1 a) {
		this.a = a;
	}
}
