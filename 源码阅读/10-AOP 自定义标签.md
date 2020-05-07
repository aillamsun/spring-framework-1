
### AOP自定义标签

```java
 <aop:aspectj-autoproxy/>
```


Spring实现了对所有类的test方法进行增强，使辅助功能可以独立于核心业务之外，方便与程序的扩展和解耦。 
那么，Spring是如何实现AOP的呢？首先我们知道，SPring是否支持注解的AOP是由一个配置文件控制的，也就是<aop:aspectj-autoproxy/>，当在配置文件中声明了这句配置的时候，Spring就会支持注解的AOP，那么我们的分析就从这句注解开始。


之前讲过Spring中的自定义注解，如果声明了自定义的注解，那么就一定会在程序中的某个地方注册了对应的解析器。我们搜索 aspectj-autoproxy 这个代码，尝试找到注册的地方，全局搜索后我们发现了在org.springframework.aop.config包下的AopNamespaceHandler中对应着这样一段函数：


```java
@Override
public void init() {
    // In 2.0 XSD as well as in 2.1 XSD.
    registerBeanDefinitionParser("config", new ConfigBeanDefinitionParser());
    registerBeanDefinitionParser("aspectj-autoproxy", new AspectJAutoProxyBeanDefinitionParser());
    registerBeanDefinitionDecorator("scoped-proxy", new ScopedProxyBeanDefinitionDecorator());

    // Only in 2.0 XSD: moved to context namespace as of 2.1
    registerBeanDefinitionParser("spring-configured", new SpringConfiguredBeanDefinitionParser());
}
```