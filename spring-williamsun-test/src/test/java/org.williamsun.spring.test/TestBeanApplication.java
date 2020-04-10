package org.williamsun.spring.test;

import org.junit.Test;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.xml.XmlBeanFactory;
import org.springframework.core.io.ClassPathResource;

/**
 * @Description TODO
 * @Created by gang.sun on 2020/4/10.
 */
public class TestBeanApplication {


	@Test
	public void testBean1() {
		BeanFactory bf = new XmlBeanFactory(new ClassPathResource("spring-config.xml"));
		TestBean myTestBean = (TestBean) bf.getBean("myTestBean");
		System.out.println(myTestBean.getName());
	}
}


