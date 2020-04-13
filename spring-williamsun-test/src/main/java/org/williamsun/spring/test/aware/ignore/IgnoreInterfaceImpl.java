package org.williamsun.spring.test.aware.ignore;

import java.util.List;
import java.util.Set;

/**
 * @Description 在实现类中注意要有setter方法入参相同类型的域对象，在例子中就是List<String>和Set<String>。
 * @Created by gang.sun on 2020/4/13.
 */
public class IgnoreInterfaceImpl implements IgnoreInterface {

	private List<String> list;
	private Set<String> set;

	@Override
	public void setList(List<String> list) {
		this.list = list;
	}

	@Override
	public void setSet(Set<String> set) {
		this.set = set;
	}


	public List<String> getList() {
		return list;
	}

	public Set<String> getSet() {
		return set;
	}
}
