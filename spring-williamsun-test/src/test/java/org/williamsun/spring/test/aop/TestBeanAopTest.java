package org.williamsun.spring.test.aop;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * @Description TODO
 * @Created by gang.sun on 2020/5/7.
 */
public class TestBeanAopTest {

	public static void main(String[] args) {
		ApplicationContext bf = new ClassPathXmlApplicationContext("aspectTest.xml");
		TestBean bean = (TestBean) bf.getBean("test");
		bean.test();
	}
}
