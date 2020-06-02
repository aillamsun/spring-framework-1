[toc]

---

### 概述

分析spring的源码之前我们先来简单回顾下spring核心功能的简单使用

### 容器的基本用法

bean是spring最核心的东西，spring就像是一个大水桶，而bean就是水桶中的水，水桶脱离了水也就没有什么用处了，我们简单看下bean的定义，代码如下：

```java

package org.williamsun.spring.test;

/**
 * @Description TODO
 * @Created by gang.sun on 2020/4/10.
 */
public class TestBean {

	private String name = "williamsun";

	
	public TestBean(String name) {
		this.name = name;
	}

	public TestBean() {
	}

	@Override
	public String toString() {
		return "MyTestBean{" +
				"name='" + name + '\'' +
				'}';
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	
}

```

源码很简单，bean没有特别之处，spring的的目的就是让我们的bean成为一个纯粹的的POJO，这就是spring追求的，接下来就是在配置文件中定义这个bean，配置文件如下：

```xml
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
	   xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
	   xsi:schemaLocation="http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans.xsd">

	<bean id="myTestBean" class="org.williamsun.spring.test.TestBean">
		<property name="name" value="sungang"/>
	</bean>
</beans>

```

在上面的配置中我们可以看到bean的声明方式，在spring中的bean定义有N种属性，但是我们只要像上面这样简单的声明就可以使用了。 
具体测试代码如下：

```java
package org.williamsun.spring.test;

import org.junit.Test;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.xml.XmlBeanFactory;
import org.springframework.core.io.ClassPathResource;
import org.williamsun.spring.test.TestBean;

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

```

运行上述测试代码就可以看到输出结果如下：  sungang


### 功能分析



### 核心类

接下来我们先了解下spring-bean最核心的两个类：DefaultListableBeanFactory和XmlBeanDefinitionReader

#### DefaultListableBeanFactory

    XmlBeanFactory继承自DefaultListableBeanFactory，而DefaultListableBeanFactory是整个bean加载的核心部分，是Spring注册及加载bean的默认实现，而对于XmlBeanFactory与DefaultListableBeanFactory不同的地方其实是在XmlBeanFactory中使用了自定义的XML读取器XmlBeanDefinitionReader，实现了个性化的BeanDefinitionReader读取，DefaultListableBeanFactory继承了AbstractAutowireCapableBeanFactory并实现了ConfigurableListableBeanFactory以及BeanDefinitionRegistry接口
    
上面类图中各个类及接口的作用如下：

> * AliasRegistry：定义对alias的简单增删改等操作
> * SimpleAliasRegistry：主要使用map作为alias的缓存，并对接口AliasRegistry进行实现
> * SingletonBeanRegistry：定义对单例的注册及获取
> * BeanFactory：定义获取bean及bean的各种属性
> * DefaultSingletonBeanRegistry：默认对接口SingletonBeanRegistry各函数的实现
> * HierarchicalBeanFactory：继承BeanFactory，也就是在BeanFactory定义的功能的基础上增加了对parentFactory的支持
> * BeanDefinitionRegistry：定义对BeanDefinition的各种增删改操作
> * FactoryBeanRegistrySupport：在DefaultSingletonBeanRegistry基础上增加了对FactoryBean的特殊处理功能
> * ConfigurableBeanFactory：提供配置Factory的各种方法
> * ListableBeanFactory：根据各种条件获取bean的配置清单
> * AbstractBeanFactory：综合FactoryBeanRegistrySupport和ConfigurationBeanFactory的功能
> * AutowireCapableBeanFactory：提供创建bean、自动注入、初始化以及应用bean的后处理器
> * AbstractAutowireCapableBeanFactory：综合AbstractBeanFactory并对接口AutowireCapableBeanFactory进行实现
> * ConfigurableListableBeanFactory：BeanFactory配置清单，指定忽略类型及接口等
> * DefaultListableBeanFactory：综合上面所有功能，主要是对Bean注册后的处理
XmlBeanFactory对DefaultListableBeanFactory类进行了扩展，主要用于从XML文档中读取BeanDefinition，对于注册及获取Bean都是使用从父类DefaultListableBeanFactory继承的方法去实现，而唯独与父类不同的个性化实现就是增加了XmlBeanDefinitionReader类型的reader属性。在XmlBeanFactory中主要使用reader属性对资源文件进行读取和注册

#### XmlBeanDefinitionReader

XML配置文件的读取是Spring中重要的功能，因为Spring的大部分功能都是以配置作为切入点的，可以从XmlBeanDefinitionReader中梳理一下资源文件读取、解析及注册的大致脉络，首先看看各个类的功能

> * ResourceLoader：定义资源加载器，主要应用于根据给定的资源文件地址返回对应的Resource
> * BeanDefinitionReader：主要定义资源文件读取并转换为BeanDefinition的各个功能
> * EnvironmentCapable：定义获取Environment方法
> * DocumentLoader：定义从资源文件加载到转换为Document的功能
> * AbstractBeanDefinitionReader：对EnvironmentCapable、BeanDefinitionReader类定义的功能进行实现
> * BeanDefinitionDocumentReader：定义读取Document并注册BeanDefinition功能
> * BeanDefinitionParserDelegate：定义解析Element的各种方法

### 容器的基础XmlBeanFactory

 通过上面的内容我们对spring的容器已经有了大致的了解，接下来我们详细探索每个步骤的详细实现，接下来要分析的功能都是基于如下代码：

```java
BeanFactory bf = new XmlBeanFactory( new ClassPathResource("spring-config.xml"));
```

```java
/**
 * Create a new XmlBeanFactory with the given input stream,
 * which must be parsable using DOM.
 *
 * @param resource          XML resource to load bean definitions from
 * @param parentBeanFactory parent bean factory
 * @throws BeansException in case of loading or parsing errors
 */
public XmlBeanFactory(Resource resource, BeanFactory parentBeanFactory) throws BeansException {
	super(parentBeanFactory);
	// 资源加载的真正实现
	this.reader.loadBeanDefinitions(resource);
}
```


首先调用ClassPathResource的构造函数来构造Resource资源文件的实例对象，这样后续的资源处理就可以用Resource提供的各种服务来操作了。有了Resource后就可以对BeanFactory进行初始化操作


上面函数中的代码this.reader.loadBeanDefinitions(resource)才是资源加载的真正实现，但是在XmlBeanDefinitionReader加载数据前还有一个调用父类构造函数初始化的过程：super(parentBeanFactory)，我们按照代码层级进行跟踪，首先跟踪到如下父类代码：

```java

public DefaultListableBeanFactory(@Nullable BeanFactory parentBeanFactory) {
    super(parentBeanFactory);
}

/**
 * Create a new DefaultListableBeanFactory with the given parent.
 *
 * @param parentBeanFactory the parent BeanFactory
 */
public DefaultListableBeanFactory(@Nullable BeanFactory parentBeanFactory) {
	//父类AbstractAutowireCapableBeanFactory的构造函数
	super(parentBeanFactory);
}

/**
 * Create a new AbstractAutowireCapableBeanFactory with the given parent.
 *
 * @param parentBeanFactory parent bean factory, or {@code null} if none
 */
public AbstractAutowireCapableBeanFactory(@Nullable BeanFactory parentBeanFactory) {
	this();
	setParentBeanFactory(parentBeanFactory);
}


/**
 * Create a new AbstractAutowireCapableBeanFactory.
 */
public AbstractAutowireCapableBeanFactory() {
	super();
	// 自动装配时忽略给定的依赖接口
	// 那么，这样做的目的是什么呢？会产生什么样的效果呢？
	ignoreDependencyInterface(BeanNameAware.class);
	ignoreDependencyInterface(BeanFactoryAware.class);
	ignoreDependencyInterface(BeanClassLoaderAware.class);
}


```

这里有必要提及 ignoreDependencylnterface方法,ignoreDependencylnterface  的主要功能是 忽略给定接口的向动装配功能，那么，这样做的目的是什么呢？会产生什么样的效果呢？


#### 真正解析bena开始

```java

/**
 * Load bean definitions from the specified XML file.
 *
 * @param encodedResource the resource descriptor for the XML file,
 *                        allowing to specify an encoding to use for parsing the file
 * @return the number of bean definitions found
 * @throws BeanDefinitionStoreException in case of loading or parsing errors
 */
public int loadBeanDefinitions(EncodedResource encodedResource) throws BeanDefinitionStoreException {
	Assert.notNull(encodedResource, "EncodedResource must not be null");
	if (logger.isInfoEnabled()) {
		logger.info("Loading XML bean definitions from " + encodedResource.getResource());
	}

	Set<EncodedResource> currentResources = this.resourcesCurrentlyBeingLoaded.get();
	if (currentResources == null) {
		currentResources = new HashSet<>(4);
		this.resourcesCurrentlyBeingLoaded.set(currentResources);
	}
	if (!currentResources.add(encodedResource)) {
		throw new BeanDefinitionStoreException(
				"Detected cyclic loading of " + encodedResource + " - check your import definitions!");
	}
	try {
		InputStream inputStream = encodedResource.getResource().getInputStream();
		try {
			InputSource inputSource = new InputSource(inputStream);
			if (encodedResource.getEncoding() != null) {
				inputSource.setEncoding(encodedResource.getEncoding());
			}
			// 调用函数doLoadBeanDefinitions
			return doLoadBeanDefinitions(inputSource, encodedResource.getResource());
		} finally {
			inputStream.close();
		}
	} catch (IOException ex) {
		throw new BeanDefinitionStoreException(
				"IOException parsing XML document from " + encodedResource.getResource(), ex);
	} finally {
		currentResources.remove(encodedResource);
		if (currentResources.isEmpty()) {
			this.resourcesCurrentlyBeingLoaded.remove();
		}
	}
}
	
```


再次转入了可复用方法loadBeanDefinitions(new EncodedResource(resource))，这个方法内部才是真正的数据准备阶段，代码如下：

```java
/**
 * Actually load bean definitions from the specified XML file.
 *
 * @param inputSource the SAX InputSource to read from
 * @param resource    the resource descriptor for the XML file
 * @return the number of bean definitions found
 * @throws BeanDefinitionStoreException in case of loading or parsing errors
 * @see #doLoadDocument
 * @see #registerBeanDefinitions
 */
protected int doLoadBeanDefinitions(InputSource inputSource, Resource resource)
		throws BeanDefinitionStoreException {
	try {
		// 调用 doLoadDocument() 方法，根据 xml 文件获取 Document 实例
		Document doc = doLoadDocument(inputSource, resource);
		// 根据获取的 Document 实例注册 Bean 信息
		return registerBeanDefinitions(doc, resource);
	} catch (BeanDefinitionStoreException ex) {
		throw ex;
	} catch (SAXParseException ex) {
		throw new XmlBeanDefinitionStoreException(resource.getDescription(),
				"Line " + ex.getLineNumber() + " in XML document from " + resource + " is invalid", ex);
	} catch (SAXException ex) {
		throw new XmlBeanDefinitionStoreException(resource.getDescription(),
				"XML document from " + resource + " is invalid", ex);
	} catch (ParserConfigurationException ex) {
		throw new BeanDefinitionStoreException(resource.getDescription(),
				"Parser configuration exception parsing XML from " + resource, ex);
	} catch (IOException ex) {
		throw new BeanDefinitionStoreException(resource.getDescription(),
				"IOException parsing XML document from " + resource, ex);
	} catch (Throwable ex) {
		throw new BeanDefinitionStoreException(resource.getDescription(),
				"Unexpected exception parsing XML document from " + resource, ex);
	}
}

```

#### 解析及注册BeanDefinitions

当把文件转换成Document后，接下来就是对bean的提取及注册，当程序已经拥有了XML文档文件的Document实例对象时，就会被引入到XmlBeanDefinitionReader.registerBeanDefinitions这个方法:

```java

/**
 * Register the bean definitions contained in the given DOM document.
 * Called by {@code loadBeanDefinitions}.
 * <p>Creates a new instance of the parser class and invokes
 * {@code registerBeanDefinitions} on it.
 *
 * @param doc      the DOM document
 * @param resource the resource descriptor (for context information)
 * @return the number of bean definitions found
 * @throws BeanDefinitionStoreException in case of parsing errors
 * @see #loadBeanDefinitions
 * @see #setDocumentReaderClass
 * @see BeanDefinitionDocumentReader#registerBeanDefinitions
 */
public int registerBeanDefinitions(Document doc, Resource resource) throws BeanDefinitionStoreException {
	// 创建 DefaultBeanDefinitionDocumentReader
	BeanDefinitionDocumentReader documentReader = createBeanDefinitionDocumentReader();
	int countBefore = getRegistry().getBeanDefinitionCount();
	// 调用 DefaultBeanDefinitionDocumentReader#registerBeanDefinitions
	documentReader.registerBeanDefinitions(doc, createReaderContext(resource));
	return getRegistry().getBeanDefinitionCount() - countBefore;
}
```

其中的doc参数即为上节读取的document，而BeanDefinitionDocumentReader是一个接口，而实例化的工作是在createBeanDefinitionDocumentReader()中完成的，而通过此方法，BeanDefinitionDocumentReader真正的类型其实已经是DefaultBeanDefinitionDocumentReader了，进入DefaultBeanDefinitionDocumentReader后，发现这个方法的重要目的之一就是提取root，以便于再次将root作为参数继续BeanDefinition的注册，如下代码：

```java

/**
 * This implementation parses bean definitions according to the "spring-beans" XSD
 * (or DTD, historically).
 * <p>Opens a DOM Document; then initializes the default settings
 * specified at the {@code <beans/>} level; then parses the contained bean definitions.
 */
@Override
public void registerBeanDefinitions(Document doc, XmlReaderContext readerContext) {
	this.readerContext = readerContext;
	logger.debug("Loading bean definitions");
	Element root = doc.getDocumentElement();
	// 解析逻辑的核心方法doRegisterBeanDefinitions
	doRegisterBeanDefinitions(root);
}


```

通过这里我们看到终于到了解析逻辑的核心方法doRegisterBeanDefinitions，接着跟踪源码如下：

```java
/**
 * Register each bean definition within the given root {@code <beans/>} element.
 */
protected void doRegisterBeanDefinitions(Element root) {
	// Any nested <beans> elements will cause recursion in this method. In
	// order to propagate and preserve <beans> default-* attributes correctly,
	// keep track of the current (parent) delegate, which may be null. Create
	// the new (child) delegate with a reference to the parent for fallback purposes,
	// then ultimately reset this.delegate back to its original (parent) reference.
	// this behavior emulates a stack of delegates without actually necessitating one.
	BeanDefinitionParserDelegate parent = this.delegate;
	this.delegate = createDelegate(getReaderContext(), root, parent);

	if (this.delegate.isDefaultNamespace(root)) {
		// 首先要解析profile属性，然后才开始XML的读取
		String profileSpec = root.getAttribute(PROFILE_ATTRIBUTE);
		if (StringUtils.hasText(profileSpec)) {
			String[] specifiedProfiles = StringUtils.tokenizeToStringArray(profileSpec, BeanDefinitionParserDelegate.MULTI_VALUE_ATTRIBUTE_DELIMITERS);
			if (!getReaderContext().getEnvironment().acceptsProfiles(specifiedProfiles)) {
				if (logger.isInfoEnabled()) {
					logger.info("Skipped XML bean definition file due to specified profiles [" + profileSpec +
							"] not matching: " + getReaderContext().getResource());
				}
				return;
			}
		}
	}

	preProcessXml(root);
	// 最终解析动作落地
	parseBeanDefinitions(root, this.delegate);
	postProcessXml(root);

	this.delegate = parent;
}
```

我们看到首先要解析profile属性，然后才开始XML的读取，具体的代码如下：

```java
/**
 * Parse the elements at the root level in the document:
 * "import", "alias", "bean".
 *
 * @param root the DOM root element of the document
 *             <p>
 *             我们知道在 Spring 有两种 Bean 声明方式：
 *             1 配置文件式声明：<bean id="myTestBean" class="com.xx.xx.MyTestBean"/>
 *             2 自定义注解方式：<tx:annotation-driven>
 *             <p>
 *             两种方式的读取和解析都存在较大的差异，所以采用不同的解析方法
 *             <p>
 *             1 如果根节点或者子节点采用默认命名空间的话，则调用 parseDefaultElement() 进行解析
 *             2 否则调用 delegate.parseCustomElement() 方法进行自定义解析
 */
protected void parseBeanDefinitions(Element root, BeanDefinitionParserDelegate delegate) {
	if (delegate.isDefaultNamespace(root)) {
		NodeList nl = root.getChildNodes();
		for (int i = 0; i < nl.getLength(); i++) {
			Node node = nl.item(i);
			if (node instanceof Element) {
				Element ele = (Element) node;
				//
				if (delegate.isDefaultNamespace(ele)) {
					//
					parseDefaultElement(ele, delegate);
				} else {
					delegate.parseCustomElement(ele);
				}
			}
		}
	} else {
		//
		delegate.parseCustomElement(root);
	}
}
```


最终解析动作落地在两个方法处：parseDefaultElement(ele, delegate) 和 delegate.parseCustomElement(root)。我们知道在 Spring 有两种 Bean 声明方式：

> * 配置文件式声明：<bean id="myTestBean" class="com.chenhao.spring.MyTestBean"/>
> * 自定义注解方式：<tx:annotation-driven>

两种方式的读取和解析都存在较大的差异，所以采用不同的解析方法，如果根节点或者子节点采用默认命名空间的话，则调用 parseDefaultElement() 进行解析，否则调用 delegate.parseCustomElement() 方法进行自定义解析。

而判断是否默认命名空间还是自定义命名空间的办法其实是使用node.getNamespaceURI()获取命名空间，并与Spring中固定的命名空间http://www.springframework.org/schema/beans进行对比，如果一致则认为是默认，否则就认为是自定义。 