package org.williamsun.spring.test.continueddependence;

/**
 * @Description TODO
 * @Created by gang.sun on 2020/4/16.
 */
public class ContinuedDependenceBean2 {

	private ContinuedDependenceBean3 c;

	private ContinuedDependenceBean2(ContinuedDependenceBean3 c) {
		this.c = c;
	}

	public ContinuedDependenceBean3 getC() {
		return c;
	}

	public void setC(ContinuedDependenceBean3 c) {
		this.c = c;
	}
}
