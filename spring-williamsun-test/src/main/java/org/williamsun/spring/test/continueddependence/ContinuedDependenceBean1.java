package org.williamsun.spring.test.continueddependence;

/**
 * @Description TODO
 * @Created by gang.sun on 2020/4/16.
 */
public class ContinuedDependenceBean1 {


	private ContinuedDependenceBean2 b;

	private ContinuedDependenceBean1(ContinuedDependenceBean2 b) {
		this.b = b;
	}

	public ContinuedDependenceBean2 getB() {
		return b;
	}

	public void setB(ContinuedDependenceBean2 b) {
		this.b = b;
	}
}
