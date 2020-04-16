package org.williamsun.spring.test.populateBean;

import org.junit.Test;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.core.io.ClassPathResource;

/**
 * @Description TODO
 * @Created by gang.sun on 2020/4/16.
 */
public class PopulateBeanTest {


	/**
	 * bean属性填充
	 */
	@Test
	public void testBean1() {
		DefaultListableBeanFactory factory = new DefaultListableBeanFactory();
		new XmlBeanDefinitionReader(factory).loadBeanDefinitions(new ClassPathResource("spring-populatebean-config.xml"));
		PopulateBean bean = (PopulateBean) factory.getBean("populateBean");
		System.out.println(bean.getName());
		System.out.println(bean.getRef().getSex());
	}

}
