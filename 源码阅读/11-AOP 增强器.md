
#### AnnotationAwareAspectJAutoProxyCreator的层次结构

我们看到这个类实现了BeanPostProcessor接口，而实现BeanPostProcessor后，当Spring加载这个Bean时会在实例化前调用其postProcesssAfterIntialization方法，而我们对于AOP逻辑的分析也由此开始。
首先看下其父类AbstractAutoProxyCreator中的postProcessAfterInitialization方法：


```java
public Object postProcessAfterInitialization(@Nullable Object bean, String beanName) throws BeansException {
    if (bean != null) {
        //根据给定的bean的class和name构建出个key，格式：beanClassName_beanName  
        Object cacheKey = getCacheKey(bean.getClass(), beanName);
        if (!this.earlyProxyReferences.contains(cacheKey)) {
            //如果它适合被代理，则需要封装指定bean  
            return wrapIfNecessary(bean, beanName, cacheKey);
        }
    }
    return bean;
}
```

```java
protected Object wrapIfNecessary(Object bean, String beanName, Object cacheKey) {

		// 如果已经处理过
		if (StringUtils.hasLength(beanName) && this.targetSourcedBeans.contains(beanName)) {
			return bean;
		}
		// 无需增强
		if (Boolean.FALSE.equals(this.advisedBeans.get(cacheKey))) {
			return bean;
		}

		// 给定的bean类是否代表一个基础设施类，基础设施类不应代理，或者配置了指定bean不需要自动代理
		if (isInfrastructureClass(bean.getClass()) || shouldSkip(bean.getClass(), beanName)) {
			this.advisedBeans.put(cacheKey, Boolean.FALSE);
			return bean;
		}

		// Create proxy if we have advice.
		// 如果存在增强方法则创建代理
		// 真正创建代理的代码是从getAdvicesAndAdvisorsForBean开始的 (获取增强方法或者增强器) AbstractAdvisorAutoProxyCreator中实现的
		/**
		 * 创建代理主要包含了两个步骤：
		 * （1）获取增强方法或者增强器；
		 * （2）根据获取的增强进行代理。
		 */
		Object[] specificInterceptors = getAdvicesAndAdvisorsForBean(bean.getClass(), beanName, null);
		// 如果获取到了增强则需要针对增强创建代理
		if (specificInterceptors != DO_NOT_PROXY) {
			this.advisedBeans.put(cacheKey, Boolean.TRUE);
			// 根据获取的增强进行代理。
			Object proxy = createProxy(bean.getClass(), beanName, specificInterceptors, new SingletonTargetSource(bean));
			this.proxyTypes.put(cacheKey, proxy.getClass());
			return proxy;
		}

		this.advisedBeans.put(cacheKey, Boolean.FALSE);
		return bean;
}
```