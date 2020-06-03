package org.williamsun.spring.test.custom.element;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.xml.XmlBeanFactory;
import org.springframework.core.io.ClassPathResource;

/**
 * @Description TODO
 * @Created by gang.sun on 2020/6/3.
 */
public class AppTest {

	public static void main(String[] args) {
		BeanFactory bf = new XmlBeanFactory(new ClassPathResource("spring-customElement.xml"));
		//MyTestBean myTestBean01 = (MyTestBean) bf.getBean("myTestBean");
		User user = (User) bf.getBean("user");
		Phone iphone = (Phone) bf.getBean("iphone");

		System.out.println(user);
		System.out.println(iphone);
	}
}
