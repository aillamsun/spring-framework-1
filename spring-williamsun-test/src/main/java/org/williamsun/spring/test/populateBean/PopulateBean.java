package org.williamsun.spring.test.populateBean;

/**
 * @Description TODO
 * @Created by gang.sun on 2020/4/16.
 */
public class PopulateBean {

	private String name;

	private Integer age;

	private PopulateBeanRef ref;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Integer getAge() {
		return age;
	}

	public void setAge(Integer age) {
		this.age = age;
	}

	public PopulateBeanRef getRef() {
		return ref;
	}

	public void setRef(PopulateBeanRef ref) {
		this.ref = ref;
	}
}
