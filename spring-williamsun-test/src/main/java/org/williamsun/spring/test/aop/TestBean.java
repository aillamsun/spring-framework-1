package org.williamsun.spring.test.aop;

/**
 * @Description TODO
 * @Created by gang.sun on 2020/5/7.
 */
public class TestBean {

	private String message = "test bean";

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public void test() {
		System.out.println(this.message);
	}

}
