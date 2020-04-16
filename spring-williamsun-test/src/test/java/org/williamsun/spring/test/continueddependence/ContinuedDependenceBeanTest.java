package org.williamsun.spring.test.continueddependence;

import org.junit.Test;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.core.io.ClassPathResource;

/**
 * @Description TODO
 * @Created by gang.sun on 2020/4/16.
 */
public class ContinuedDependenceBeanTest {


	/**
	 * 构造循环依赖
	 */
	@Test
	public void testBean1() {
		DefaultListableBeanFactory factory = new DefaultListableBeanFactory();
		new XmlBeanDefinitionReader(factory).loadBeanDefinitions(new ClassPathResource("spring-continued-dependence-config.xml"));
		ContinuedDependenceBean1 bean = (ContinuedDependenceBean1) factory.getBean("a");
		ContinuedDependenceBean2 b = bean.getB();
		ContinuedDependenceBean3 c = b.getC();
		ContinuedDependenceBean1 a = c.getA();
		System.out.println(111);
	}


	/**
	 * 属性注入循环依赖
	 */
	@Test
	public void testBean2() {
		DefaultListableBeanFactory factory = new DefaultListableBeanFactory();
		new XmlBeanDefinitionReader(factory).loadBeanDefinitions(new ClassPathResource("spring-continued-dependence-config2.xml"));
		ContinuedDependenceBeanA bean = (ContinuedDependenceBeanA) factory.getBean("a");
		ContinuedDependenceBeanB b = bean.getB();
		System.out.println(111);
	}
}
