package org.williamsun.spring.test.custom.element;

import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.AbstractSingleBeanDefinitionParser;
import org.springframework.util.StringUtils;
import org.w3c.dom.Element;

/**
 * @Description TODO
 * @Created by gang.sun on 2020/6/3.
 */
public class PhoneBeanDefinitionParser extends AbstractSingleBeanDefinitionParser {

	@Override
	protected Class<?> getBeanClass(Element element) {
		return Phone.class;

	}

	@Override
	protected void doParse(Element element, BeanDefinitionBuilder builder) {
		String color = element.getAttribute("color");
		int size = Integer.parseInt(element.getAttribute("size"));
		String remark = element.getAttribute("remark");
		if (StringUtils.hasText(color)) {
			builder.addPropertyValue("color", color);
		}
		if (StringUtils.hasText(String.valueOf(size))) {
			builder.addPropertyValue("size", size);
		}
		if (StringUtils.hasText(remark)) {
			builder.addPropertyValue("remark", remark);
		}
	}
}
