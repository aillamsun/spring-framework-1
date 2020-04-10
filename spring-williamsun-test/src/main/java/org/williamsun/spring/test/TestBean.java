package org.williamsun.spring.test;

/**
 * @Description TODO
 * @Created by gang.sun on 2020/4/10.
 */
public class TestBean {

	private String name = "williamsun";

	public TestBean(String name) {
		this.name = name;
	}

	public TestBean() {
	}

	@Override
	public String toString() {
		return "MyTestBean{" +
				"name='" + name + '\'' +
				'}';
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
}
