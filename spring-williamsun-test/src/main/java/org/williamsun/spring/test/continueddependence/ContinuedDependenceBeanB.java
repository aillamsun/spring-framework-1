package org.williamsun.spring.test.continueddependence;

import org.springframework.stereotype.Service;

/**
 * @Description TODO
 * @Created by gang.sun on 2020/4/16.
 */
public class ContinuedDependenceBeanB {


	private ContinuedDependenceBeanA a;

	public ContinuedDependenceBeanA getA() {
		return a;
	}

	public void setA(ContinuedDependenceBeanA a) {
		this.a = a;
	}
}
