package org.williamsun.spring.test.continueddependence;

import org.springframework.stereotype.Service;

/**
 * @Description TODO
 * @Created by gang.sun on 2020/4/16.
 */
public class ContinuedDependenceBeanA {


	private ContinuedDependenceBeanB b;

	public ContinuedDependenceBeanB getB() {
		return b;
	}

	public void setB(ContinuedDependenceBeanB b) {
		this.b = b;
	}
}
