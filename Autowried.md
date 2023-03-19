# Autowired


## 내부 동작
<img width="1958" alt="스크린샷 2023-03-18 오후 11 59 16" src="https://user-images.githubusercontent.com/60481383/226113794-068bc643-a6c9-414d-9ddb-278646c31a2b.png">

### 1. 생성자에 Autowired 붙인 경우
- 
```java
public abstract class AbstractAutowireCapableBeanFactory extends AbstractBeanFactory implements AutowireCapableBeanFactory {
    @Override
    @SuppressWarnings("unchecked")
    public <T> T createBean(Class<T> beanClass) throws BeansException {
        // Use non-singleton bean definition, to avoid registering bean as dependent bean.
        RootBeanDefinition bd = new CreateFromClassBeanDefinition(beanClass);
        bd.setScope(SCOPE_PROTOTYPE);
        bd.allowCaching = ClassUtils.isCacheSafe(beanClass, getBeanClassLoader());
        return (T) createBean(beanClass.getName(), bd, null);
    }

    @Override
    protected Object createBean(String beanName, RootBeanDefinition mbd, @Nullable Object[] args) throws BeanCreationException {
        // ... 생략
        try {
            
            Object beanInstance = doCreateBean(beanName, mbdToUse, args);
            // ... 생략
            return beanInstance;
        } catch (BeanCreationException | ImplicitlyAppearedSingletonException ex) {
            throw ex;
        } catch (Throwable ex) {
            throw new BeanCreationException(mbdToUse.getResourceDescription(), beanName, "Unexpected exception during bean creation", ex);
        }
    }

    protected Object doCreateBean(String beanName, RootBeanDefinition mbd, @Nullable Object[] args) throws BeanCreationException {
        // Instantiate the bean.
        BeanWrapper instanceWrapper = null;
        
        if (instanceWrapper == null) {
            // 
            instanceWrapper = createBeanInstance(beanName, mbd, args);
        }
        // ... 생략


        // Allow post-processors to modify the merged bean definition.
        synchronized (mbd.postProcessingLock) {
            if (!mbd.postProcessed) {
                try {
                    applyMergedBeanDefinitionPostProcessors(mbd, beanType, beanName);
                }
                catch (Throwable ex) {
                    throw new BeanCreationException(mbd.getResourceDescription(), beanName,
                            "Post-processing of merged bean definition failed", ex);
                }
                mbd.markAsPostProcessed();
            }
        }

        // Eagerly cache singletons to be able to resolve circular references
        // even when triggered by lifecycle interfaces like BeanFactoryAware.
        boolean earlySingletonExposure = (mbd.isSingleton() && this.allowCircularReferences && isSingletonCurrentlyInCreation(beanName));
        
        if (earlySingletonExposure) {
            if (logger.isTraceEnabled()) {
                logger.trace("Eagerly caching bean '" + beanName +
                        "' to allow for resolving potential circular references");
            }
            addSingletonFactory(beanName, () -> getEarlyBeanReference(beanName, mbd, bean));
        }

        // Initialize the bean instance.
        Object exposedObject = bean;
        try {
            populateBean(beanName, mbd, instanceWrapper);
            exposedObject = initializeBean(beanName, exposedObject, mbd);
        } catch (Throwable ex) {
            if (ex instanceof BeanCreationException bce && beanName.equals(bce.getBeanName())) {
                throw bce;
            } else {
                throw new BeanCreationException(mbd.getResourceDescription(), beanName, ex.getMessage(), ex);
            }
        }

        if (earlySingletonExposure) {
            Object earlySingletonReference = getSingleton(beanName, false);
            if (earlySingletonReference != null) {
                if (exposedObject == bean) {
                    exposedObject = earlySingletonReference;
                } else if (!this.allowRawInjectionDespiteWrapping && hasDependentBean(beanName)) {
                    String[] dependentBeans = getDependentBeans(beanName);
                    Set<String> actualDependentBeans = new LinkedHashSet<>(dependentBeans.length);
                    for (String dependentBean : dependentBeans) {
                        if (!removeSingletonIfCreatedForTypeCheckOnly(dependentBean)) {
                            actualDependentBeans.add(dependentBean);
                        }
                    }
                    if (!actualDependentBeans.isEmpty()) {
                        throw new BeanCurrentlyInCreationException(beanName,
                                "Bean with name '" + beanName + "' has been injected into other beans [" +
                                        StringUtils.collectionToCommaDelimitedString(actualDependentBeans) +
                                        "] in its raw version as part of a circular reference, but has eventually been " +
                                        "wrapped. This means that said other beans do not use the final version of the " +
                                        "bean. This is often the result of over-eager type matching - consider using " +
                                        "'getBeanNamesForType' with the 'allowEagerInit' flag turned off, for example.");
                    }
                }
            }
        }

        // Register bean as disposable.
        try {
            registerDisposableBeanIfNecessary(beanName, bean, mbd);
        } catch (BeanDefinitionValidationException ex) {
            throw new BeanCreationException(
                    mbd.getResourceDescription(), beanName, "Invalid destruction signature", ex);
        }

        return exposedObject;
    }

    protected BeanWrapper createBeanInstance(String beanName, RootBeanDefinition mbd, @Nullable Object[] args) {
        // .. 생략

        // Shortcut when re-creating the same bean...
        boolean resolved = false;
        boolean autowireNecessary = false;
        if (args == null) {
            synchronized (mbd.constructorArgumentLock) {
                if (mbd.resolvedConstructorOrFactoryMethod != null) {
                    resolved = true;
                    autowireNecessary = mbd.constructorArgumentsResolved;
                }
            }
        }
        if (resolved) {
            if (autowireNecessary) {
                return autowireConstructor(beanName, mbd, null, null);
            }
            else {
                return instantiateBean(beanName, mbd);
            }
        }

        // Candidate constructors for autowiring?
        Constructor<?>[] ctors = determineConstructorsFromBeanPostProcessors(beanClass, beanName);
        if (ctors != null || mbd.getResolvedAutowireMode() == AUTOWIRE_CONSTRUCTOR ||
                mbd.hasConstructorArgumentValues() || !ObjectUtils.isEmpty(args)) {
            return autowireConstructor(beanName, mbd, ctors, args);
        }

        // Preferred constructors for default construction?
        ctors = mbd.getPreferredConstructors();
        if (ctors != null) {
            return autowireConstructor(beanName, mbd, ctors, null);
        }

        // No special handling: simply use no-arg constructor.
        return instantiateBean(beanName, mbd);
    }
}


```



### 2. 필드나 메서드에 Autowired 붙인 경우
- @Autowired 애노테이션은 `BeanPostProcessor`라는 라이프 사이클 인터페이스의 구현체인 `AutowiredAnnotationBeanPostProcessor`에 의해 의존성 주입이 이루어진다.
- BeanPostProcessor의 구현체인 `AutowiredAnnotationBeanPostProcessor`가 빈의 초기화 라이프 사이클 이전, 즉 빈이 생성되기 전에 @Autowired가 붙어있으면 해당하는 빈을 찾아서 주입해주는 작업을 하는 것이다.
- `AutowiredAnnotationBeanPostProcessor`는 하나의 빈으로써 spring IoC 컨테이너에 등록되어 있다.
- `processInjection` 메서드에서 InjectMetadata 클래스의 `inject()` 메서드를 호출하여 객체를 주입한다.

```java
public abstract class SpringBeanAutowiringSupport {

	private static final Log logger = LogFactory.getLog(SpringBeanAutowiringSupport.class);

    public SpringBeanAutowiringSupport() {
		processInjectionBasedOnCurrentContext(this);
	}
    
	public static void processInjectionBasedOnCurrentContext(Object target) {
		Assert.notNull(target, "Target object must not be null");
		WebApplicationContext cc = ContextLoader.getCurrentWebApplicationContext();
		if (cc != null) {
			AutowiredAnnotationBeanPostProcessor bpp = new AutowiredAnnotationBeanPostProcessor();
			bpp.setBeanFactory(cc.getAutowireCapableBeanFactory());
			bpp.processInjection(target);
		} else {
		}
	}
    
	public static void processInjectionBasedOnServletContext(Object target, ServletContext servletContext) {
		Assert.notNull(target, "Target object must not be null");
		WebApplicationContext cc = WebApplicationContextUtils.getRequiredWebApplicationContext(servletContext);
		AutowiredAnnotationBeanPostProcessor bpp = new AutowiredAnnotationBeanPostProcessor();
		bpp.setBeanFactory(cc.getAutowireCapableBeanFactory());
		bpp.processInjection(target);
	}
}
```

```java
public class AutowiredAnnotationBeanPostProcessor implements SmartInstantiationAwareBeanPostProcessor,
        MergedBeanDefinitionPostProcessor, BeanRegistrationAotProcessor, PriorityOrdered, BeanFactoryAware {

    public void processInjection(Object bean) throws BeanCreationException {
        // 1. Bean 클래스 정보를 읽어온다.
        Class<?> clazz = bean.getClass();
        
        // 2. 자동으로 의존관계를 설정할 메타데이터를 얻는다.
        InjectionMetadata metadata = findAutowiringMetadata(clazz.getName(), clazz, null);
        try {
            // 3. InjectMetadata 클래스의 inject() 메서드를 호출하여 객체를 주입한다.
            metadata.inject(bean, null, null);
        }
        catch (BeanCreationException ex) {
            throw ex;
        }
        catch (Throwable ex) {
            throw new BeanCreationException(
                    "Injection of autowired dependencies failed for class [" + clazz + "]", ex);
        }
    }
    

    protected void inject(Object target, @Nullable String requestingBeanName, @Nullable PropertyValues pvs)
            throws Throwable {

        if (this.isField) {
            Field field = (Field) this.member;
            ReflectionUtils.makeAccessible(field);
            field.set(target, getResourceToInject(target, requestingBeanName));
        }
        else {
            if (checkPropertySkipping(pvs)) {
                return;
            }
            try {
                Method method = (Method) this.member;
                ReflectionUtils.makeAccessible(method);
                method.invoke(target, getResourceToInject(target, requestingBeanName));
            }
            catch (InvocationTargetException ex) {
                throw ex.getTargetException();
            }
        }
    }
}
 




```
- InjectMetadata 클래스의 inject() 메서드를 호출되면, `field` 인지 `method`인지에 따라 분기하여 로직이 흘러간다.
- field 의 경우 `field.set()`을 통해, method 의 경우 `method.invoke()`를 통해 Injection이 이뤄진다.



```java
public class InjectionMetadata {
    // .. 생략
    public void inject(Object target, @Nullable String beanName, @Nullable PropertyValues pvs) throws Throwable {
        Collection<InjectedElement> checkedElements = this.checkedElements;
        Collection<InjectedElement> elementsToIterate =
                (checkedElements != null ? checkedElements : this.injectedElements);
        if (!elementsToIterate.isEmpty()) {
            for (InjectedElement element : elementsToIterate) {
                // 주입
                element.inject(target, beanName, pvs);
            }
        }
    }

    protected void inject(Object target, @Nullable String requestingBeanName, @Nullable PropertyValues pvs)
            throws Throwable {

        if (this.isField) {
            Field field = (Field) this.member;
            // 접근제어자가 private 임에도 불구하고, Autowiring 되는 객체에 접근이 가능하도록 한다.
            ReflectionUtils.makeAccessible(field);
            field.set(target, getResourceToInject(target, requestingBeanName));
        } else {
            if (checkPropertySkipping(pvs)) {
                return;
            }
            try {
                Method method = (Method) this.member;
                // 접근제어자가 private 임에도 불구하고, Autowiring 되는 객체에 접근이 가능하도록 한다.
                ReflectionUtils.makeAccessible(method);
                method.invoke(target, getResourceToInject(target, requestingBeanName));
            }
            catch (InvocationTargetException ex) {
                throw ex.getTargetException();
            }
        }
    }
}
```

# Reference
- https://levelup.gitconnected.com/in-depth-analysis-of-the-implementation-of-the-autowired-annotation-45dd9fc4bbdd
- https://github.com/HwangWonGyu/news/pull/20#discussion_r566839551
- https://beststar-1.tistory.com/40
- https://blog.minseong.kim/autowired-deep-dive.html
- https://www.fatalerrors.org/a/0tlx0zg.html
- https://yatyat2.tistory.com/169