
[toc]

---

## 概述

    本文主要研究Spring标签的解析，Spring的标签中有默认标签和自定义标签，两者的解析有着很大的不同，这次重点说默认标签的解析过程。

    默认标签的解析是在DefaultBeanDefinitionDocumentReader.parseDefaultElement函数中进行的，分别对4种不同的标签（import,alias,bean和beans）做了不同处理。我们先看下此函数的源码：
    
    
```java
/**
 * 分别对4种不同的标签（import,alias,bean和beans）做了不同处理
 *
 * @param ele
 * @param delegate
 */
private void parseDefaultElement(Element ele, BeanDefinitionParserDelegate delegate) {
    if (delegate.nodeNameEquals(ele, IMPORT_ELEMENT)) { // import
        importBeanDefinitionResource(ele);
    } else if (delegate.nodeNameEquals(ele, ALIAS_ELEMENT)) { // alias
        processAliasRegistration(ele);
    } else if (delegate.nodeNameEquals(ele, BEAN_ELEMENT)) { // bean
        // 对bean标签的解析最为复杂也最为重要
        processBeanDefinition(ele, delegate);
    } else if (delegate.nodeNameEquals(ele, NESTED_BEANS_ELEMENT)) { // beans
        // recurse
        doRegisterBeanDefinitions(ele);
    }
}
```

### 1. Bean标签的解析及注册


    在4中标签中对bean标签的解析最为复杂也最为重要，所以从此标签开始深入分析，如果能理解这个标签的解析过程，其他标签的解析就迎刃而解了。对于bean标签的解析用的是processBeanDefinition函数，首先看看函数processBeanDefinition(ele,delegate)，其代码如下：
    
```java

/**
 * Process the given bean element, parsing the bean definition
 * and registering it with the registry.
 */
protected void processBeanDefinition(Element ele, BeanDefinitionParserDelegate delegate) {
    // parseBeanDefinitionElement方法进行元素的解析，返回BeanDefinitionHolder类型的实例bdHolder
    BeanDefinitionHolder bdHolder = delegate.parseBeanDefinitionElement(ele);
    if (bdHolder != null) {
        /**
         * <bean id="demo" class="com.xx.xxx.MyTestBean">
         *      <property name="beanName" value="bean demo1"/>
         *      <meta key="demo" value="demo"/>
         *      <mybean:username="mybean"/>
         *  </bean>
         *
         *  这个配置文件中有个自定义的标签 decorateBeanDefinitionIfRequired方法就是用来处理这种情况的，其中的null是用来传递父级BeanDefinition的
         */
        bdHolder = delegate.decorateBeanDefinitionIfRequired(ele, bdHolder);
        try {
            // Register the final decorated instance.
            //
            BeanDefinitionReaderUtils.registerBeanDefinition(bdHolder, getReaderContext().getRegistry());
        } catch (BeanDefinitionStoreException ex) {
            getReaderContext().error("Failed to register bean definition with name '" +
                    bdHolder.getBeanName() + "'", ele, ex);
        }
        // Send registration event.
        getReaderContext().fireComponentRegistered(new BeanComponentDefinition(bdHolder));
    }
}
```

> * 首先委托BeanDefinitionDelegate类的parseBeanDefinitionElement方法进行元素的解析，返回BeanDefinitionHolder类型的实例bdHolder，经过这个方法后bdHolder实例已经包含了我们配置文件中的各种属性了，例如class，name，id，alias等。
> *  当返回的dbHolder不为空的情况下若存在默认标签的子节点下再有自定义属性，还需要再次对自定义标签进行解析。
> * 当解析完成后，需要对解析后的bdHolder进行注册，注册过程委托给了BeanDefinitionReaderUtils的registerBeanDefinition方法。
> * 最后发出响应事件，通知相关的监听器已经加载完这个Bean了。

#### 解析BeanDefinition

接下来我们就针对具体的方法进行分析，首先我们从元素解析及信息提取开始，也就是BeanDefinitionHolder bdHolder = delegate.parseBeanDefinitionElement（ele），进入 BeanDefinitionDelegate 类的 parseBeanDefinitionElement 方法。我们看下源码：

```java
/**
 * Parses the supplied {@code <bean>} element. May return {@code null}
 * if there were errors during parse. Errors are reported to the
 * {@link org.springframework.beans.factory.parsing.ProblemReporter}.
 */
@Nullable
public BeanDefinitionHolder parseBeanDefinitionElement(Element ele, @Nullable BeanDefinition containingBean) {
	// 解析 ID 属性
	String id = ele.getAttribute(ID_ATTRIBUTE);
	// 解析 name 属性
	String nameAttr = ele.getAttribute(NAME_ATTRIBUTE);
	// 分割 name 属性
	List<String> aliases = new ArrayList<>();
	if (StringUtils.hasLength(nameAttr)) {
		String[] nameArr = StringUtils.tokenizeToStringArray(nameAttr, MULTI_VALUE_ATTRIBUTE_DELIMITERS);
		aliases.addAll(Arrays.asList(nameArr));
	}

	String beanName = id;
	if (!StringUtils.hasText(beanName) && !aliases.isEmpty()) {
		beanName = aliases.remove(0);
		if (logger.isDebugEnabled()) {
			logger.debug("No XML 'id' specified - using '" + beanName +
					"' as bean name and " + aliases + " as aliases");
		}
	}
	// 检查 name 的唯一性
	if (containingBean == null) {
		checkNameUniqueness(beanName, aliases, ele);
	}

	// 解析 属性，构造 AbstractBeanDefinition
	// parseBeanDefinitionElement(ele, beanName, containingBean);是用来对标签中的其他属性进行解析
	AbstractBeanDefinition beanDefinition = parseBeanDefinitionElement(ele, beanName, containingBean);
	if (beanDefinition != null) {
		// 如果 beanName 不存在，则根据条件构造一个 beanName
		if (!StringUtils.hasText(beanName)) {
			try {
				if (containingBean != null) {
					beanName = BeanDefinitionReaderUtils.generateBeanName(beanDefinition, this.readerContext.getRegistry(), true);
				} else {
					beanName = this.readerContext.generateBeanName(beanDefinition);
					// Register an alias for the plain bean class name, if still possible,
					// if the generator returned the class name plus a suffix.
					// This is expected for Spring 1.2/2.0 backwards compatibility.
					String beanClassName = beanDefinition.getBeanClassName();
					if (beanClassName != null &&
							beanName.startsWith(beanClassName) && beanName.length() > beanClassName.length() &&
							!this.readerContext.getRegistry().isBeanNameInUse(beanClassName)) {
						aliases.add(beanClassName);
					}
				}
				if (logger.isDebugEnabled()) {
					logger.debug("Neither XML 'id' nor 'name' specified - " +
							"using generated bean name [" + beanName + "]");
				}
			} catch (Exception ex) {
				error(ex.getMessage(), ele);
				return null;
			}
		}
		String[] aliasesArray = StringUtils.toStringArray(aliases);
		// 封装 BeanDefinitionHolder
		return new BeanDefinitionHolder(beanDefinition, beanName, aliasesArray);
	}

	return null;
}
```

上述方法就是对默认标签解析的全过程了，我们分析下当前层完成的工作：

> * 提取元素中的id和name属性
> * 进一步解析其他所有属性并统一封装到GenericBeanDefinition类型的实例中
> * 如果检测到bean没有指定beanName，那么使用默认规则为此bean生成beanName
> * 将获取到的信息封装到BeanDefinitionHolder的实例中。

AbstractBeanDefinition beanDefinition = parseBeanDefinitionElement(ele, beanName, containingBean); 用来对标签中的其他属性进行解析，我们详细看下源码：

```java
/**
 * Parse the bean definition itself, without regard to name or aliases. May return
 * {@code null} if problems occurred during the parsing of the bean definition.
 */
@Nullable
public AbstractBeanDefinition parseBeanDefinitionElement(
		Element ele, String beanName, @Nullable BeanDefinition containingBean) {

	this.parseState.push(new BeanEntry(beanName));

	String className = null;
	// 解析class属性
	if (ele.hasAttribute(CLASS_ATTRIBUTE)) {
		className = ele.getAttribute(CLASS_ATTRIBUTE).trim();
	}
	String parent = null;
	// 解析parent属性
	if (ele.hasAttribute(PARENT_ATTRIBUTE)) {
		parent = ele.getAttribute(PARENT_ATTRIBUTE);
	}

	try {
		// 创建用于承载属性的AbstractBeanDefinition类型的GenericBeanDefinition实例
		AbstractBeanDefinition bd = createBeanDefinition(className, parent);
		// 硬编码解析bean的各种属性
		parseBeanDefinitionAttributes(ele, beanName, containingBean, bd);
		// 设置description属性
		bd.setDescription(DomUtils.getChildElementValueByTagName(ele, DESCRIPTION_ELEMENT));
		// 解析meta元素
		/**
		 *   meta属性的使用
		 *
		 *   <bean id="demo" class="com.xxx.xxx.MyBeanDemo">
		 *       <property name="beanName" value="bean demo1"/>
		 *       <meta key="demo" value="demo"/>
		 *   </bean>
		 *
		 */
		parseMetaElements(ele, bd);
		// 解析lookup-method属性
		parseLookupOverrideSubElements(ele, bd.getMethodOverrides());
		// 解析replace-method属性
		parseReplacedMethodSubElements(ele, bd.getMethodOverrides());

		// 解析构造函数的参数
		parseConstructorArgElements(ele, bd);
		// 解析properties子元素
		parsePropertyElements(ele, bd);
		// 解析qualifier子元素
		parseQualifierElements(ele, bd);

		bd.setResource(this.readerContext.getResource());
		bd.setSource(extractSource(ele));

		return bd;
	} catch (ClassNotFoundException ex) {
		error("Bean class [" + className + "] not found", ele, ex);
	} catch (NoClassDefFoundError err) {
		error("Class that bean class [" + className + "] depends on not found", ele, err);
	} catch (Throwable ex) {
		error("Unexpected failure during bean definition parsing", ele, ex);
	} finally {
		this.parseState.pop();
	}

	return null;
}
```


接下来我们一步步分析解析过程。

### 2. Bean详细解析过程

#### 创建用于承载属性的BeanDefinition 

    BeanDefinition是一个接口，在spring中此接口有三种实现：RootBeanDefinition、ChildBeanDefinition已经GenericBeanDefinition。而三种实现都继承了AbstractBeanDefinition，其中BeanDefinition是配置文件元素标签在容器中的内部表示形式。元素标签拥有class、scope、lazy-init等属性
    
    BeanDefinition则提供了相应的beanClass、scope、lazyInit属性，BeanDefinition和<bean>中的属性一一对应。其中RootBeanDefinition是最常用的实现类，他对应一般性的元素标签，GenericBeanDefinition是自2.5版本以后新加入的bean文件配置属性定义类，是一站式服务的。
    
    在配置文件中可以定义父和字，父用RootBeanDefinition表示，而子用ChildBeanDefinition表示，而没有父的就使用RootBeanDefinition表示。AbstractBeanDefinition对两者共同的类信息进行抽象。
    
    Spring通过BeanDefinition将配置文件中的配置信息转换为容器的内部表示，并将这些BeanDefinition注册到BeanDefinitionRegistry中。Spring容器的BeanDefinitionRegistry就像是Spring配置信息的内存数据库，主要是以map的形式保存，后续操作直接从BeanDefinitionResistry中读取配置信息
    

因此，要解析属性首先要创建用于承载属性的实例，也就是创建GenericBeanDefinition类型的实例。而代码createBeanDefinition（className,parent）的作用就是实现此功能。我们详细看下方法体，代码如下：

```java

/**
 * Create a bean definition for the given class name and parent name.
 *
 * @param className  the name of the bean class
 * @param parentName the name of the bean's parent bean
 * @return the newly created bean definition
 * @throws ClassNotFoundException if bean class resolution was attempted but failed
 */
protected AbstractBeanDefinition createBeanDefinition(@Nullable String className, @Nullable String parentName)
		throws ClassNotFoundException {

	return BeanDefinitionReaderUtils.createBeanDefinition(
			parentName, className, this.readerContext.getBeanClassLoader());
}




/**
 * Create a new GenericBeanDefinition for the given parent name and class name,
 * eagerly loading the bean class if a ClassLoader has been specified.
 *
 * @param parentName  the name of the parent bean, if any
 * @param className   the name of the bean class, if any
 * @param classLoader the ClassLoader to use for loading bean classes
 *                    (can be {@code null} to just register bean classes by name)
 * @return the bean definition
 * @throws ClassNotFoundException if the bean class could not be loaded
 */
public static AbstractBeanDefinition createBeanDefinition(
		@Nullable String parentName, @Nullable String className, @Nullable ClassLoader classLoader) throws ClassNotFoundException {

	GenericBeanDefinition bd = new GenericBeanDefinition();
	bd.setParentName(parentName);
	if (className != null) {
		if (classLoader != null) {
			bd.setBeanClass(ClassUtils.forName(className, classLoader));
		} else {
			bd.setBeanClassName(className);
		}
	}
	return bd;
}

```

#### 对应的属性解析

当创建好了承载bean信息的实例后，接下来就是解析各种属性了，首先我们看下parseBeanDefinitionAttributes(ele, beanName, containingBean, bd);

```java

/**
 * Apply the attributes of the given bean element to the given bean * definition.
 *
 * @param ele            bean declaration element
 * @param beanName       bean name
 * @param containingBean containing bean definition
 * @return a bean definition initialized according to the bean element attributes
 */
public AbstractBeanDefinition parseBeanDefinitionAttributes(Element ele, String beanName,
															@Nullable BeanDefinition containingBean, AbstractBeanDefinition bd) {

	// 解析singleton属性
	if (ele.hasAttribute(SINGLETON_ATTRIBUTE)) {
		error("Old 1.x 'singleton' attribute in use - upgrade to 'scope' declaration", ele);
	} else if (ele.hasAttribute(SCOPE_ATTRIBUTE)) { // 解析scope属性
		bd.setScope(ele.getAttribute(SCOPE_ATTRIBUTE));
	} else if (containingBean != null) {
		// Take default from containing bean in case of an inner bean definition.
		bd.setScope(containingBean.getScope());
	}
	// 解析abstract属性
	if (ele.hasAttribute(ABSTRACT_ATTRIBUTE)) {
		bd.setAbstract(TRUE_VALUE.equals(ele.getAttribute(ABSTRACT_ATTRIBUTE)));
	}

	// 解析lazy_init属性
	String lazyInit = ele.getAttribute(LAZY_INIT_ATTRIBUTE);
	if (DEFAULT_VALUE.equals(lazyInit)) {
		lazyInit = this.defaults.getLazyInit();
	}
	bd.setLazyInit(TRUE_VALUE.equals(lazyInit));

	// 解析autowire属性
	String autowire = ele.getAttribute(AUTOWIRE_ATTRIBUTE);
	bd.setAutowireMode(getAutowireMode(autowire));

	// 解析dependsOn属性
	if (ele.hasAttribute(DEPENDS_ON_ATTRIBUTE)) {
		String dependsOn = ele.getAttribute(DEPENDS_ON_ATTRIBUTE);
		bd.setDependsOn(StringUtils.tokenizeToStringArray(dependsOn, MULTI_VALUE_ATTRIBUTE_DELIMITERS));
	}

	// 解析autowireCandidate属性
	String autowireCandidate = ele.getAttribute(AUTOWIRE_CANDIDATE_ATTRIBUTE);
	if ("".equals(autowireCandidate) || DEFAULT_VALUE.equals(autowireCandidate)) {
		String candidatePattern = this.defaults.getAutowireCandidates();
		if (candidatePattern != null) {
			String[] patterns = StringUtils.commaDelimitedListToStringArray(candidatePattern);
			bd.setAutowireCandidate(PatternMatchUtils.simpleMatch(patterns, beanName));
		}
	} else {
		bd.setAutowireCandidate(TRUE_VALUE.equals(autowireCandidate));
	}

	// 解析primary属性
	if (ele.hasAttribute(PRIMARY_ATTRIBUTE)) {
		bd.setPrimary(TRUE_VALUE.equals(ele.getAttribute(PRIMARY_ATTRIBUTE)));
	}

	// 解析init_method属性
	if (ele.hasAttribute(INIT_METHOD_ATTRIBUTE)) {
		String initMethodName = ele.getAttribute(INIT_METHOD_ATTRIBUTE);
		bd.setInitMethodName(initMethodName);
	} else if (this.defaults.getInitMethod() != null) {
		bd.setInitMethodName(this.defaults.getInitMethod());
		bd.setEnforceInitMethod(false);
	}
	// 解析destroy_method属性
	if (ele.hasAttribute(DESTROY_METHOD_ATTRIBUTE)) {
		String destroyMethodName = ele.getAttribute(DESTROY_METHOD_ATTRIBUTE);
		bd.setDestroyMethodName(destroyMethodName);
	} else if (this.defaults.getDestroyMethod() != null) {
		bd.setDestroyMethodName(this.defaults.getDestroyMethod());
		bd.setEnforceDestroyMethod(false);
	}
	// 解析factory_method属性
	if (ele.hasAttribute(FACTORY_METHOD_ATTRIBUTE)) {
		bd.setFactoryMethodName(ele.getAttribute(FACTORY_METHOD_ATTRIBUTE));
	}
	// 解析factory_bean属性
	if (ele.hasAttribute(FACTORY_BEAN_ATTRIBUTE)) {
		bd.setFactoryBeanName(ele.getAttribute(FACTORY_BEAN_ATTRIBUTE));
	}

	return bd;
}

```


#### 解析meta元素 

    parseMetaElements(ele, bd);

```java
public void parseMetaElements(Element ele, BeanMetadataAttributeAccessor attributeAccessor) {
	NodeList nl = ele.getChildNodes();
	for (int i = 0; i < nl.getLength(); i++) {
		Node node = nl.item(i);
		if (isCandidateElement(node) && nodeNameEquals(node, META_ELEMENT)) {
			Element metaElement = (Element) node;
			String key = metaElement.getAttribute(KEY_ATTRIBUTE);
			String value = metaElement.getAttribute(VALUE_ATTRIBUTE);
			BeanMetadataAttribute attribute = new BeanMetadataAttribute(key, value);
			attribute.setSource(extractSource(metaElement));
			attributeAccessor.addMetadataAttribute(attribute);
		}
	}
}
```

#### 解析lookup-method属性

    parseLookupOverrideSubElements(ele, bd.getMethodOverrides());

```java
/**
 * Parse lookup-override sub-elements of the given bean element.
 */
public void parseLookupOverrideSubElements(Element beanEle, MethodOverrides overrides) {
	NodeList nl = beanEle.getChildNodes();
	for (int i = 0; i < nl.getLength(); i++) {
		Node node = nl.item(i);
		if (isCandidateElement(node) && nodeNameEquals(node, LOOKUP_METHOD_ELEMENT)) {
			Element ele = (Element) node;
			String methodName = ele.getAttribute(NAME_ATTRIBUTE);
			String beanRef = ele.getAttribute(BEAN_ELEMENT);
			LookupOverride override = new LookupOverride(methodName, beanRef);
			override.setSource(extractSource(ele));
			overrides.addOverride(override);
		}
	}
}
```

#### 解析replace-method属性


    在分析代码前我们还是先简单的了解下replaced-method的用法，其主要功能是方法替换：即在运行时用新的方法替换旧的方法。与之前的lookup-method不同的是此方法不仅可以替换返回的bean，还可以动态的更改原有方法的运行逻辑，我们看下使用：

```java
//原有的changeMe方法
public class TestChangeMethod {
    public void changeMe(){
            System.out.println("ChangeMe");
        }
    }
    //新的实现方法
    public class ReplacerChangeMethod implements MethodReplacer {
        public Object reimplement(Object o, Method method, Object[] objects) throws Throwable {
            System.out.println("I Replace Method");
            return null;
        }
}
    
    
//新的配置文件
<?xml version="1.0" encoding="UTF-8" ?>
<beans xmlns="http://www.springframework.org/schema/beans"
   xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
   xsi:schemaLocation="
    http://www.springframework.org/schema/beans
    http://www.springframework.org/schema/beans/spring-beans.xsd">

<bean id="changeMe" class="com.williamsun.spring.TestChangeMethod">
    <replaced-method name="changeMe" replacer="replacer"/>
</bean>
<bean id="replacer" class="com.williamsun.spring.ReplacerChangeMethod"></bean>
</beans>


    //测试方法
public class TestDemo {
    public static void main(String[] args) {
        ApplicationContext context = new ClassPathXmlApplicationContext("replaced-method.xml");
    
        TestChangeMethod test =(TestChangeMethod) context.getBean("changeMe");
    
        test.changeMe();
    }
}
```

    parseReplacedMethodSubElements(ele, bd.getMethodOverrides());


```java
/**
 * Parse replaced-method sub-elements of the given bean element.
 */
public void parseReplacedMethodSubElements(Element beanEle, MethodOverrides overrides) {
	NodeList nl = beanEle.getChildNodes();
	for (int i = 0; i < nl.getLength(); i++) {
		Node node = nl.item(i);
		if (isCandidateElement(node) && nodeNameEquals(node, REPLACED_METHOD_ELEMENT)) {
			Element replacedMethodEle = (Element) node;
			String name = replacedMethodEle.getAttribute(NAME_ATTRIBUTE);
			String callback = replacedMethodEle.getAttribute(REPLACER_ATTRIBUTE);
			ReplaceOverride replaceOverride = new ReplaceOverride(name, callback);
			// Look for arg-type match elements.
			List<Element> argTypeEles = DomUtils.getChildElementsByTagName(replacedMethodEle, ARG_TYPE_ELEMENT);
			for (Element argTypeEle : argTypeEles) {
				String match = argTypeEle.getAttribute(ARG_TYPE_MATCH_ATTRIBUTE);
				match = (StringUtils.hasText(match) ? match : DomUtils.getTextValue(argTypeEle));
				if (StringUtils.hasText(match)) {
					replaceOverride.addTypeIdentifier(match);
				}
			}
			replaceOverride.setSource(extractSource(replacedMethodEle));
			overrides.addOverride(replaceOverride);
		}
	}
}
```

我们可以看到无论是 look-up 还是 replaced-method 是构造了 MethodOverride ，并最终记录在了 AbstractBeanDefinition 中的 methodOverrides 属性中


#### 解析构造函数的参数constructor-arg

    对构造函数的解析式非常常用，也是非常复杂的 parseConstructorArgElements(ele, bd);
    
```java

/**
 * Parse constructor-arg sub-elements of the given bean element.
 */
public void parseConstructorArgElements(Element beanEle, BeanDefinition bd) {
	NodeList nl = beanEle.getChildNodes();
	for (int i = 0; i < nl.getLength(); i++) {
		Node node = nl.item(i);
		if (isCandidateElement(node) && nodeNameEquals(node, CONSTRUCTOR_ARG_ELEMENT)) {
			parseConstructorArgElement((Element) node, bd);
		}
	}
}


/**
 * Parse a constructor-arg element.
 * <p>
 * 首先提取index、type、name等属性
 * 根据是否配置了index属性解析流程不同
 */
public void parseConstructorArgElement(Element ele, BeanDefinition bd) {
	// 获取 index 属性
	String indexAttr = ele.getAttribute(INDEX_ATTRIBUTE);
	// 获取 type 属性
	String typeAttr = ele.getAttribute(TYPE_ATTRIBUTE);
	// 获取 name 属性
	String nameAttr = ele.getAttribute(NAME_ATTRIBUTE);
	if (StringUtils.hasLength(indexAttr)) {
		try {
			int index = Integer.parseInt(indexAttr);
			if (index < 0) {
				error("'index' cannot be lower than 0", ele);
			} else {
				try {
					this.parseState.push(new ConstructorArgumentEntry(index));
					// parsePropertyValue(ele, bd, null)方法读取constructor-arg的子元素
					Object value = parsePropertyValue(ele, bd, null);
					// 使用ConstructorArgumentValues.ValueHolder封装解析出来的元素
					ConstructorArgumentValues.ValueHolder valueHolder = new ConstructorArgumentValues.ValueHolder(value);
					if (StringUtils.hasLength(typeAttr)) {
						valueHolder.setType(typeAttr);
					}
					if (StringUtils.hasLength(nameAttr)) {
						valueHolder.setName(nameAttr);
					}
					valueHolder.setSource(extractSource(ele));
					if (bd.getConstructorArgumentValues().hasIndexedArgumentValue(index)) {
						error("Ambiguous constructor-arg entries for index " + index, ele);
					} else {
						bd.getConstructorArgumentValues().addIndexedArgumentValue(index, valueHolder);
					}
				} finally {
					this.parseState.pop();
				}
			}
		} catch (NumberFormatException ex) {
			error("Attribute 'index' of tag 'constructor-arg' must be an integer", ele);
		}
	} else {
		try {
			this.parseState.push(new ConstructorArgumentEntry());
			Object value = parsePropertyValue(ele, bd, null);
			ConstructorArgumentValues.ValueHolder valueHolder = new ConstructorArgumentValues.ValueHolder(value);
			if (StringUtils.hasLength(typeAttr)) {
				valueHolder.setType(typeAttr);
			}
			if (StringUtils.hasLength(nameAttr)) {
				valueHolder.setName(nameAttr);
			}
			valueHolder.setSource(extractSource(ele));
			bd.getConstructorArgumentValues().addGenericArgumentValue(valueHolder);
		} finally {
			this.parseState.pop();
		}
	}
}

```


上述代码的流程可以简单的总结为如下：

> * 首先提取index、type、name等属性
> * 根据是否配置了index属性解析流程不同

如果配置了index属性，解析流程如下：

> * 使用parsePropertyValue(ele, bd, null)方法读取constructor-arg的子元素
> * 使用ConstructorArgumentValues.ValueHolder封装解析出来的元素
> * 将index、type、name属性也封装进ValueHolder中，然后将ValueHoder添加到当前beanDefinition的ConstructorArgumentValues的indexedArgumentValues，而indexedArgumentValues是一个map类型

如果没有配置index属性，将index、type、name属性也封装进ValueHolder中，然后将ValueHoder添加到当前beanDefinition的ConstructorArgumentValues的genericArgumentValues中

    Object value = parsePropertyValue(ele, bd, null);
    
```java
/**
 * Get the value of a property element. May be a list etc.
 * Also used for constructor arguments, "propertyName" being null in this case.
 */
@Nullable
public Object parsePropertyValue(Element ele, BeanDefinition bd, @Nullable String propertyName) {
	String elementName = (propertyName != null) ?
			"<property> element for property '" + propertyName + "'" :
			"<constructor-arg> element";

	// Should only have one child element: ref, value, list, etc.
	NodeList nl = ele.getChildNodes();
	Element subElement = null;
	for (int i = 0; i < nl.getLength(); i++) {
		Node node = nl.item(i);
		// 略过description和meta属性
		if (node instanceof Element && !nodeNameEquals(node, DESCRIPTION_ELEMENT) &&
				!nodeNameEquals(node, META_ELEMENT)) {
			// Child element is what we're looking for.
			if (subElement != null) {
				error(elementName + " must not contain more than one sub-element", ele);
			} else {
				subElement = (Element) node;
			}
		}
	}

	// 解析ref属性
	boolean hasRefAttribute = ele.hasAttribute(REF_ATTRIBUTE);
	// 解析value属性
	boolean hasValueAttribute = ele.hasAttribute(VALUE_ATTRIBUTE);
	if ((hasRefAttribute && hasValueAttribute) ||
			((hasRefAttribute || hasValueAttribute) && subElement != null)) {
		error(elementName +
				" is only allowed to contain either 'ref' attribute OR 'value' attribute OR sub-element", ele);
	}

	if (hasRefAttribute) {
		String refName = ele.getAttribute(REF_ATTRIBUTE);
		if (!StringUtils.hasText(refName)) {
			error(elementName + " contains empty 'ref' attribute", ele);
		}

		// 使用RuntimeBeanReference来封装ref对应的bean
		RuntimeBeanReference ref = new RuntimeBeanReference(refName);
		ref.setSource(extractSource(ele));
		return ref;
	} else if (hasValueAttribute) {
		// 使用TypedStringValue 来封装value属性
		TypedStringValue valueHolder = new TypedStringValue(ele.getAttribute(VALUE_ATTRIBUTE));
		valueHolder.setSource(extractSource(ele));
		return valueHolder;
	} else if (subElement != null) {
		// 解析子元素
		return parsePropertySubElement(subElement, bd);
	} else {
		// Neither child element nor "ref" or "value" attribute found.
		error(elementName + " must specify a ref or value", ele);
		return null;
	}
}
```

上述代码的执行逻辑简单总结为：

> * 首先略过decription和meta属性
> * 提取constructor-arg上的ref和value属性，并验证是否存在
> * 存在ref属性时，用RuntimeBeanReference来封装ref
> * 存在value属性时，用TypedStringValue来封装
> * 存在子元素时，对于子元素的处理使用了方法 parsePropertySubElement(subElement, bd);，其代码如下：


```java

@Nullable
public Object parsePropertySubElement(Element ele, @Nullable BeanDefinition bd) {
	return parsePropertySubElement(ele, bd, null);
}

/**
 * Parse a value, ref or collection sub-element of a property or
 * constructor-arg element.
 *
 * @param ele              subelement of property element; we don't know which yet
 * @param defaultValueType the default type (class name) for any
 *                         {@code <value>} tag that might be created
 */
@Nullable
public Object parsePropertySubElement(Element ele, @Nullable BeanDefinition bd, @Nullable String defaultValueType) {
	// 判断是否是默认标签处理
	if (!isDefaultNamespace(ele)) {
		return parseNestedCustomElement(ele, bd);
	} else if (nodeNameEquals(ele, BEAN_ELEMENT)) { // 对于bean标签的处理
		BeanDefinitionHolder nestedBd = parseBeanDefinitionElement(ele, bd);
		if (nestedBd != null) {
			nestedBd = decorateBeanDefinitionIfRequired(ele, nestedBd, bd);
		}
		return nestedBd;
	} else if (nodeNameEquals(ele, REF_ELEMENT)) {
		// A generic reference to any name of any bean.
		String refName = ele.getAttribute(BEAN_REF_ATTRIBUTE);
		boolean toParent = false;
		if (!StringUtils.hasLength(refName)) {
			// A reference to the id of another bean in a parent context.
			refName = ele.getAttribute(PARENT_REF_ATTRIBUTE);
			toParent = true;
			if (!StringUtils.hasLength(refName)) {
				error("'bean' or 'parent' is required for <ref> element", ele);
				return null;
			}
		}
		if (!StringUtils.hasText(refName)) {
			error("<ref> element contains empty target attribute", ele);
			return null;
		}
		RuntimeBeanReference ref = new RuntimeBeanReference(refName, toParent);
		ref.setSource(extractSource(ele));
		return ref;
	} else if (nodeNameEquals(ele, IDREF_ELEMENT)) {  // idref元素处理
		return parseIdRefElement(ele);
	} else if (nodeNameEquals(ele, VALUE_ELEMENT)) { // value元素处理
		return parseValueElement(ele, defaultValueType);
	} else if (nodeNameEquals(ele, NULL_ELEMENT)) { // null元素处理
		// It's a distinguished null value. Let's wrap it in a TypedStringValue
		// object in order to preserve the source location.
		TypedStringValue nullHolder = new TypedStringValue(null);
		nullHolder.setSource(extractSource(ele));
		return nullHolder;
	} else if (nodeNameEquals(ele, ARRAY_ELEMENT)) { // array 元素处理
		return parseArrayElement(ele, bd);
	} else if (nodeNameEquals(ele, LIST_ELEMENT)) { // list 元素处理
		return parseListElement(ele, bd);
	} else if (nodeNameEquals(ele, SET_ELEMENT)) { // set 元素处理
		return parseSetElement(ele, bd);
	} else if (nodeNameEquals(ele, MAP_ELEMENT)) { // map 元素处理
		return parseMapElement(ele, bd);
	} else if (nodeNameEquals(ele, PROPS_ELEMENT)) { // props 元素处理
		return parsePropsElement(ele);
	} else {
		error("Unknown property sub-element: [" + ele.getNodeName() + "]", ele);
		return null;
	}
}
```


#### 解析properties子元素

    parsePropertyElements(ele, bd);
    
```java

/**
 * Parse property sub-elements of the given bean element.
 */
public void parsePropertyElements(Element beanEle, BeanDefinition bd) {
	NodeList nl = beanEle.getChildNodes();
	for (int i = 0; i < nl.getLength(); i++) {
		Node node = nl.item(i);
		if (isCandidateElement(node) && nodeNameEquals(node, PROPERTY_ELEMENT)) {
			parsePropertyElement((Element) node, bd);
		}
	}
}

/**
 * Parse a property element.
 */
public void parsePropertyElement(Element ele, BeanDefinition bd) {
	String propertyName = ele.getAttribute(NAME_ATTRIBUTE);
	if (!StringUtils.hasLength(propertyName)) {
		error("Tag 'property' must have a 'name' attribute", ele);
		return;
	}
	this.parseState.push(new PropertyEntry(propertyName));
	try {
		if (bd.getPropertyValues().contains(propertyName)) {
			error("Multiple 'property' definitions for property '" + propertyName + "'", ele);
			return;
		}
		Object val = parsePropertyValue(ele, bd, propertyName);
		PropertyValue pv = new PropertyValue(propertyName, val);
		parseMetaElements(ele, pv);
		pv.setSource(extractSource(ele));
		bd.getPropertyValues().addPropertyValue(pv);
	} finally {
		this.parseState.pop();
	}
}

```

在获取了propertie的属性后使用PropertyValue 进行封装，然后将其添加到BeanDefinition的propertyValueList中


#### 解析qualifier子元素

    对于 qualifier 元素的获取，我们接触更多的是注解的形式，在使用 Spring 架中进行自动注入时，Spring 器中匹配的候选 Bean 数目必须有且仅有一个，当找不到一个匹配的 Bean 时， Spring容器将抛出 BeanCreationException 异常， 并指出必须至少拥有一个匹配的 Bean。
    

```xml

<bean id="myTestBean" class="com.williamsun.spring.MyTestBean">
    <qualifier  type="org.Springframework.beans.factory.annotation.Qualifier" value="gf" />
</bean>

```

    parseQualifierElements(ele, bd);


```java
/**
 * Parse qualifier sub-elements of the given bean element.
 */
public void parseQualifierElements(Element beanEle, AbstractBeanDefinition bd) {
	NodeList nl = beanEle.getChildNodes();
	for (int i = 0; i < nl.getLength(); i++) {
		Node node = nl.item(i);
		if (isCandidateElement(node) && nodeNameEquals(node, QUALIFIER_ELEMENT)) {
			parseQualifierElement((Element) node, bd);
		}
	}
}

/**
 * Parse a qualifier element.
 */
public void parseQualifierElement(Element ele, AbstractBeanDefinition bd) {
	String typeName = ele.getAttribute(TYPE_ATTRIBUTE);
	if (!StringUtils.hasLength(typeName)) {
		error("Tag 'qualifier' must have a 'type' attribute", ele);
		return;
	}
	this.parseState.push(new QualifierEntry(typeName));
	try {
		AutowireCandidateQualifier qualifier = new AutowireCandidateQualifier(typeName);
		qualifier.setSource(extractSource(ele));
		String value = ele.getAttribute(VALUE_ATTRIBUTE);
		if (StringUtils.hasLength(value)) {
			qualifier.setAttribute(AutowireCandidateQualifier.VALUE_KEY, value);
		}
		NodeList nl = ele.getChildNodes();
		for (int i = 0; i < nl.getLength(); i++) {
			Node node = nl.item(i);
			if (isCandidateElement(node) && nodeNameEquals(node, QUALIFIER_ATTRIBUTE_ELEMENT)) {
				Element attributeEle = (Element) node;
				String attributeName = attributeEle.getAttribute(KEY_ATTRIBUTE);
				String attributeValue = attributeEle.getAttribute(VALUE_ATTRIBUTE);
				if (StringUtils.hasLength(attributeName) && StringUtils.hasLength(attributeValue)) {
					BeanMetadataAttribute attribute = new BeanMetadataAttribute(attributeName, attributeValue);
					attribute.setSource(extractSource(attributeEle));
					qualifier.addMetadataAttribute(attribute);
				} else {
					error("Qualifier 'attribute' tag must have a 'name' and 'value'", attributeEle);
					return;
				}
			}
		}
		bd.addQualifier(qualifier);
	} finally {
		this.parseState.pop();
	}
}

```



以上便完成了对 XML 文档到 GenericBeanDefinition 的转换， 就是说到这里， XML 中所有的配置都可以在 GenericBeanDefinition的实例类中应找到对应的配置。

GenericBeanDefinition 只是子类实现，而大部分的通用属性都保存在了 AbstractBeanDefinition 中，那么我们再次通过 AbstractBeanDefinition 的属性来回顾一 下我们都解析了哪些对应的配置。


```java

public abstract class AbstractBeanDefinition extends BeanMetadataAttributeAccessor
        implements BeanDefinition, Cloneable {
        
    // 此处省略静态变量以及final变量
    
    @Nullable
    private volatile Object beanClass;
    /**
     * bean的作用范围，对应bean属性scope
     */
    @Nullable
    private String scope = SCOPE_DEFAULT;
    /**
     * 是否是抽象，对应bean属性abstract
     */
    private boolean abstractFlag = false;
    /**
     * 是否延迟加载，对应bean属性lazy-init
     */
    private boolean lazyInit = false;
    /**
     * 自动注入模式，对应bean属性autowire
     */
    private int autowireMode = AUTOWIRE_NO;
    /**
     * 依赖检查，Spring 3.0后弃用这个属性
     */
    private int dependencyCheck = DEPENDENCY_CHECK_NONE;
    /**
     * 用来表示一个bean的实例化依靠另一个bean先实例化，对应bean属性depend-on
     */
    @Nullable
    private String[] dependsOn;
    /**
     * autowire-candidate属性设置为false，这样容器在查找自动装配对象时，
     * 将不考虑该bean，即它不会被考虑作为其他bean自动装配的候选者，
     * 但是该bean本身还是可以使用自动装配来注入其他bean的
     */
    private boolean autowireCandidate = true;
    /**
     * 自动装配时出现多个bean候选者时，将作为首选者，对应bean属性primary
     */
    private boolean primary = false;
    /**
     * 用于记录Qualifier，对应子元素qualifier
     */
    private final Map<String, AutowireCandidateQualifier> qualifiers = new LinkedHashMap<>(0);

    @Nullable
    private Supplier<?> instanceSupplier;
    /**
     * 允许访问非公开的构造器和方法，程序设置
     */
    private boolean nonPublicAccessAllowed = true;
    /**
     * 是否以一种宽松的模式解析构造函数，默认为true，
     * 如果为false，则在以下情况
     * interface ITest{}
     * class ITestImpl implements ITest{};
     * class Main {
     *     Main(ITest i) {}
     *     Main(ITestImpl i) {}
     * }
     * 抛出异常，因为Spring无法准确定位哪个构造函数程序设置
     */
    private boolean lenientConstructorResolution = true;
    /**
     * 对应bean属性factory-bean，用法：
     * <bean id = "instanceFactoryBean" class = "example.chapter3.InstanceFactoryBean" />
     * <bean id = "currentTime" factory-bean = "instanceFactoryBean" factory-method = "createTime" />
     */
    @Nullable
    private String factoryBeanName;
    /**
     * 对应bean属性factory-method
     */
    @Nullable
    private String factoryMethodName;
    /**
     * 记录构造函数注入属性，对应bean属性constructor-arg
     */
    @Nullable
    private ConstructorArgumentValues constructorArgumentValues;
    /**
     * 普通属性集合
     */
    @Nullable
    private MutablePropertyValues propertyValues;
    /**
     * 方法重写的持有者，记录lookup-method、replaced-method元素
     */
    @Nullable
    private MethodOverrides methodOverrides;
    /**
     * 初始化方法，对应bean属性init-method
     */
    @Nullable
    private String initMethodName;
    /**
     * 销毁方法，对应bean属性destroy-method
     */
    @Nullable
    private String destroyMethodName;
    /**
     * 是否执行init-method，程序设置
     */
    private boolean enforceInitMethod = true;
    /**
     * 是否执行destroy-method，程序设置
     */
    private boolean enforceDestroyMethod = true;
    /**
     * 是否是用户定义的而不是应用程序本身定义的，创建AOP时候为true，程序设置
     */
    private boolean synthetic = false;
    /**
     * 定义这个bean的应用，APPLICATION：用户，INFRASTRUCTURE：完全内部使用，与用户无关，
     * SUPPORT：某些复杂配置的一部分
     * 程序设置
     */
    private int role = BeanDefinition.ROLE_APPLICATION;
    /**
     * bean的描述信息
     */
    @Nullable
    private String description;
    /**
     * 这个bean定义的资源
     */
    @Nullable
    private Resource resource;
}
```