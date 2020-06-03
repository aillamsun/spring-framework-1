package org.williamsun.spring.test.custom.element;

import org.springframework.beans.factory.xml.NamespaceHandlerSupport;

/**
 * @Description TODO
 * @Created by gang.sun on 2020/6/3.
 */
public class CustomElementNamespaceHandler extends NamespaceHandlerSupport {

	@Override
	public void init() {
		registerBeanDefinitionParser("user", new UserBeanDefinitionParser());
		registerBeanDefinitionParser("phone", new PhoneBeanDefinitionParser());
	}
}
