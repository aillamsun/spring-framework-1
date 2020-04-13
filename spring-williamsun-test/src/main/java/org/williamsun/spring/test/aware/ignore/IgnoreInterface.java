package org.williamsun.spring.test.aware.ignore;

import java.util.List;
import java.util.Set;

/**
 * @Description 定义一个要被忽略的接口
 * @Created by gang.sun on 2020/4/13.
 */
public interface IgnoreInterface {

	void setList(List<String> list);

	void setSet(Set<String> set);
}
