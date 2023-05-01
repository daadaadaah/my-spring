


## 스프링 프로젝트를 실행시키면 내부에서 어떤 일이 발생할까?
- main을 실행시키면, `SpringApplication.run(PracticeHelloSpringApplication.class, args)`가 실행된다.
```java
@SpringBootApplication
public class PracticeHelloSpringApplication {

	public static void main(String[] args) {
		SpringApplication.run(PracticeHelloSpringApplication.class, args);
	}
}
```
- `SpringApplication.run(PracticeHelloSpringApplication.class, args)`가 실행되면, 먼저 `SpringApplication`은 다음과 같이 생성자에서 초기화 작업이 이루어진다.
```java
public class SpringApplication {
    // ... 생략
    public SpringApplication(ResourceLoader resourceLoader, Class<?>... primarySources) {
        this.resourceLoader = resourceLoader;
        
        // 1) 메인 클래스가 null 인지 체크
        Assert.notNull(primarySources, "PrimarySources must not be null");
        this.primarySources = new LinkedHashSet<>(Arrays.asList(primarySources));
        
        // ClassPath로부터 애플리케이션 타입을 추론
        this.webApplicationType = WebApplicationType.deduceFromClasspath();
        
        // BootstrapRegistryInitializer 를 불러오고 셋해줌
        this.bootstrapRegistryInitializers = new ArrayList<>(getSpringFactoriesInstances(BootstrapRegistryInitializer.class));
        
        // ApplicationContextInitializer 를 찾아서 셋해줌
        setInitializers((Collection) getSpringFactoriesInstances(ApplicationContextInitializer.class));
        
        // ApplicationListener를  찾아서 셋해줌
        setListeners((Collection) getSpringFactoriesInstances(ApplicationListener.class));
        
        // 메인 클래스를 추런
        this.mainApplicationClass = deduceMainApplicationClass();
    }
}
```

- 그 다음 `run(Class<?> primarySource, String... args)`이 실행된다.
```java
public class SpringApplication {
    // return 값으로, `ConfigurableApplicationContext`이 생성된다. 
    public static ConfigurableApplicationContext run(Class<?> primarySource, String... args) {
        return run(new Class<?>[] { primarySource }, args);
    }
}
```

- `run(Class<?> primarySource, String... args)`이 실행 되면, `new SpringApplication(primarySources).run(args)`이 실행 된다.
```java
public class SpringApplication {
    // ... 생략
    public static ConfigurableApplicationContext run(Class<?>[] primarySources, String[] args) {
        return new SpringApplication(primarySources).run(args);
    }

}
```
- `run(args)`은 다음과 같은 단계를 거친 후 `context(=ApplicationContext)`를 `return`한다.
1. StopWatch로 실행 시간 측정 시작 
2. BootStrapContext 생성 
3. Java AWT Headless Property 설정 
4. 스프링 애플리케이션 리스너 조회 및 starting 처리 
5. Arguments 래핑 및 Environment 준비 
6. 배너 출력 
7. ApplicationContext 생성 
8. Context 준비 단계 
9. Context Refresh 단계 
10. Context Refresh 후처리 단계 
11. 실행 시간 출력 및 리스너 started 처리 
12. Runners 실행

```java
public class SpringApplication {
    // ... 생략
    public ConfigurableApplicationContext run(String... args) {
        // 1. StopWatch로 실행 시간 측정 시작
        long startTime = System.nanoTime();
        
        // 2. BootStrapContext 생성
        DefaultBootstrapContext bootstrapContext = createBootstrapContext();
        
        ConfigurableApplicationContext context = null;
        
        // 3. Java AWT Headless Property 설정
        configureHeadlessProperty();
        
        // 4. 스프링 애플리케이션 리스너 조회 및 starting 처리
        SpringApplicationRunListeners listeners = getRunListeners(args);
        listeners.starting(bootstrapContext, this.mainApplicationClass);
        
        try {
            // 5. Argumetns 래핑 및 Environment 준비
            ApplicationArguments applicationArguments = new DefaultApplicationArguments(args);
            ConfigurableEnvironment environment = prepareEnvironment(listeners, bootstrapContext, applicationArguments);
            
            // 6. 배너 출력
            Banner printedBanner = printBanner(environment);
            
            // 7. ApplicationContext 생성
            context = createApplicationContext();
            context.setApplicationStartup(this.applicationStartup);
            
            // 8. Context 준비 단계
            prepareContext(bootstrapContext, context, environment, listeners, applicationArguments, printedBanner);
            
            // 9. Context Refresh 단계
            refreshContext(context);
            
            // 10. Context Refresh 후처리 단계
            afterRefresh(context, applicationArguments);
            
            // 11. 실행시간 출력 및 리스너 start 처리
            Duration timeTakenToStartup = Duration.ofNanos(System.nanoTime() - startTime);
            if (this.logStartupInfo) {
                new StartupInfoLogger(this.mainApplicationClass).logStarted(getApplicationLog(), timeTakenToStartup);
            }
            listeners.started(context, timeTakenToStartup);
            
            // 12. Runners 실행
            callRunners(context, applicationArguments);
        }
        catch (Throwable ex) {
            if (ex instanceof AbandonedRunException) {
                throw ex;
            }
            handleRunFailure(context, ex, listeners);
            throw new IllegalStateException(ex);
        }
        try {
            if (context.isRunning()) {
                Duration timeTakenToReady = Duration.ofNanos(System.nanoTime() - startTime);
                listeners.ready(context, timeTakenToReady);
            }
        }
        catch (Throwable ex) {
            if (ex instanceof AbandonedRunException) {
                throw ex;
            }
            handleRunFailure(context, ex, null);
            throw new IllegalStateException(ex);
        }
        return context;
    }
}
```
### 1. StopWatch로 실행 시간 측정 시작



### 2. BootStrapContext 생성
```java
public class SpringApplication {
    // ... 생략
    public ConfigurableApplicationContext run(String... args) {
        
        // 2. BootStrapContext 생성
        DefaultBootstrapContext bootstrapContext = createBootstrapContext();
        // ... 생략
    }
}
```
- `createBootstrapContext()`이 실행되면, `BootstrapContext`가 생성된다.
- `BootstrapContext`란 `ApplicationContext`가 준비될 때까지 환경 변수들을 관리하는 스프링의 Environment 객체를 후처리하기 위한 임시 Context 이다.
```java
public class SpringApplication {
    private final List<BootstrapRegistryInitializer> bootstrapRegistryInitializers;

    public SpringApplication(ResourceLoader resourceLoader, Class<?>... primarySources) {
        this.bootstrapRegistryInitializers = new ArrayList<>(getSpringFactoriesInstances(BootstrapRegistryInitializer.class));
    }
    
    // ... 생략
    private DefaultBootstrapContext createBootstrapContext() {
        // 1) DefaultBootstrapContext 객체를 생성해준다.
        DefaultBootstrapContext bootstrapContext = new DefaultBootstrapContext();
        
        // 2) SpringApplication 생성자에서 spring.factories에서 불러왔던 bootstrapRegistryInitializers를 모두 DefaultBootstrapContext에 초기화해준다.
        this.bootstrapRegistryInitializers.forEach((initializer) -> initializer.initialize(bootstrapContext));
        
        return bootstrapContext;
    }
    // ... 생략
}
```

### 3. Java AWT Headless Property 설정
```java
public class SpringApplication {
    // ... 생략
    public ConfigurableApplicationContext run(String... args) {

        // 3. Java AWT Headless Property 설정
        configureHeadlessProperty();
        // ... 생략
    }

    private void configureHeadlessProperty() {
        System.setProperty(SYSTEM_PROPERTY_JAVA_AWT_HEADLESS, System.getProperty(SYSTEM_PROPERTY_JAVA_AWT_HEADLESS, Boolean.toString(this.headless)));
    }
}
```
- Java AWT Headless 모드는 모니터나 마우스, 키보드 등의 디스플레이 장치가 없는 서버 환경에서 UI 클래스를 사용할 수 있도록 하는 옵션이다. 
- 예를 들어 서버에서 이미지를 만들어 반환해주어야 하는 경우에 이미지 관련 클래스 등이 필요할 수 있다. 
- 만약 Headless 모드를 주지 않으면 해당 클래스의 사용이 불가능하고 에러가 발생하는데, 이때 Headless 모드를 true로 주면 사용 불가능한 UI 클래스들을 특별 객체로 만들어준다. 
- 대표적으로 java.awt.Toolkit의 경우 특별하게 headless 전용 객체로 만들어서 디스플레이 장치가 없어도 사용할 수 있게 해준다. 
- 만약 headless 모드인데 디스플레이 장치가 필수인 기능(화면에 띄우는 기능 등)를 호출한다면 Headless 에러를 던진다.
- SpringBoot에서는 기본적으로 headless 모드를 true라서 java.awt 등의 패키지를 사용해 이미지 관련 처리를 할 수 있다.

### 4. 스프링 애플리케이션 리스너 조회 및 starting 처리
- 여기서는 애플리케이션 컨텍스트를 준비할 때 호출되어야하는 리스너들을 조회한 후 BootStrapContext의 리스너로써 실행하게 해준다. 
- 예를 들어, 생성 시간이 긴 객체들의 경우 객체 생성을 위한 리스너를 만들어 등록하면,
- BootStrapContext가 애플리케이션 컨텍스트를 준비함과 동시에 객체를 생성하도록 함으로써 Lazy하게 접근가능하는데 사용할 수 있다.
```java
public class SpringApplication {
    // ... 생략
    public ConfigurableApplicationContext run(String... args) {

        // 4. 스프링 애플리케이션 리스너 조회 및 starting 처리
        SpringApplicationRunListeners listeners = getRunListeners(args);
        listeners.starting(bootstrapContext, this.mainApplicationClass);
        // ... 생략
    }

    private SpringApplicationRunListeners getRunListeners(String[] args) {
        ArgumentResolver argumentResolver = ArgumentResolver.of(SpringApplication.class, this);
        argumentResolver = argumentResolver.and(String[].class, args);
        List<SpringApplicationRunListener> listeners = getSpringFactoriesInstances(SpringApplicationRunListener.class,
                argumentResolver);
        SpringApplicationHook hook = applicationHook.get();
        SpringApplicationRunListener hookListener = (hook != null) ? hook.getRunListener(this) : null;
        if (hookListener != null) {
            listeners = new ArrayList<>(listeners);
            listeners.add(hookListener);
        }
        return new SpringApplicationRunListeners(logger, listeners, this.applicationStartup);
    }
}
```

### 5. Arguments 래핑 및 Environment 준비
```java
public class SpringApplication {
    // ... 생략
    public ConfigurableApplicationContext run(String... args) {
        
        // 5. Arguments 래핑 및 Environment 준비
        ApplicationArguments applicationArguments = new DefaultApplicationArguments(args);
        ConfigurableEnvironment environment = prepareEnvironment(listeners, bootstrapContext, applicationArguments);
        // ... 생략
    }

    private ConfigurableEnvironment prepareEnvironment(SpringApplicationRunListeners listeners, DefaultBootstrapContext bootstrapContext, ApplicationArguments applicationArguments) {
        // 1) 애플리케이션 타입에 맞게 Environment 구현체를 생성해준다.
        ConfigurableEnvironment environment = getOrCreateEnvironment();
        
        // 2) 만들어진 environment에 Property 또는 Profile 등을 세팅해준다.
        configureEnvironment(environment, applicationArguments.getSourceArgs());
        ConfigurationPropertySources.attach(environment);
        listeners.environmentPrepared(bootstrapContext, environment);
        DefaultPropertiesPropertySource.moveToEnd(environment);
        Assert.state(!environment.containsProperty("spring.main.environment-prefix"), "Environment prefix cannot be set via properties.");
        
        // 3) 그렇게 셋팅한 environment에 SpringApplication에 바인딩해준다.
        bindToSpringApplication(environment);
        if (!this.isCustomEnvironment) {
            EnvironmentConverter environmentConverter = new EnvironmentConverter(getClassLoader());
            environment = environmentConverter.convertEnvironmentIfNecessary(environment, deduceEnvironmentClass());
        }
        ConfigurationPropertySources.attach(environment);
        return environment;
    }
    
    private ConfigurableEnvironment getOrCreateEnvironment() {
        if (this.environment != null) {
            return this.environment;
        }
        ConfigurableEnvironment environment = this.applicationContextFactory.createEnvironment(this.webApplicationType);
        if (environment == null && this.applicationContextFactory != ApplicationContextFactory.DEFAULT) {
            environment = ApplicationContextFactory.DEFAULT.createEnvironment(this.webApplicationType);
        }
        return (environment != null) ? environment : new ApplicationEnvironment();
    }
}
```


### 6. 배너 출력
```java
public class SpringApplication {
    // ... 생략
    public ConfigurableApplicationContext run(String... args) {

        // 6. 배너 출력
        Banner printedBanner = printBanner(environment);
        // ... 생략
    }
}
```


### 7. ApplicationContext 생성 
```java
public class SpringApplication {
    // ... 생략
    public ConfigurableApplicationContext run(String... args) {

        // 7. ApplicationContext 생성
        context = createApplicationContext();
        context.setApplicationStartup(this.applicationStartup);

        // ... 생략
    }

    // ... 생략
    protected ConfigurableApplicationContext createApplicationContext() {
        return this.applicationContextFactory.create(this.webApplicationType);
    }
    
    // ... 생략
    private ApplicationContextFactory applicationContextFactory = ApplicationContextFactory.DEFAULT;

    // ... 생략
    public void setApplicationContextFactory(ApplicationContextFactory applicationContextFactory) {
        this.applicationContextFactory = (applicationContextFactory != null) ? applicationContextFactory : ApplicationContextFactory.DEFAULT;
    }

}
```
- `createApplicationContext()`이 실행 되면, `this.applicationContextFactory.create(this.webApplicationType)`이 실행되어, `webApplicationType`에 따라 다른 `ApplicationContext`가 생성된다.
- 즉, 직접 생상하지 않고 Factory 클래스에게 `ApplicationContext 생성`을 위임하였다.
> cf. `webApplicationType`에 따라 `ApplicationContextFactory`도 다르다.
> 1) `DefaultApplicationContextFactory`
```java
class DefaultApplicationContextFactory implements ApplicationContextFactory {
    // ... 생략
    @Override
    public ConfigurableApplicationContext create(WebApplicationType webApplicationType) {
        try {
            return getFromSpringFactories(
                    webApplicationType, 
                    ApplicationContextFactory::create,
                    this::createDefaultApplicationContext
            );
        }
        catch (Exception ex) {
            throw new IllegalStateException("Unable create a default ApplicationContext instance, "
                    + "you may need a custom ApplicationContextFactory", ex);
        }
    }

    private <T> T getFromSpringFactories(WebApplicationType webApplicationType, BiFunction<ApplicationContextFactory, WebApplicationType, T> action, Supplier<T> defaultResult) {
        for (ApplicationContextFactory candidate : SpringFactoriesLoader.loadFactories(ApplicationContextFactory.class,
                getClass().getClassLoader())) {
            T result = action.apply(candidate, webApplicationType);
            if (result != null) {
                return result;
            }
        }
        return (defaultResult != null) ? defaultResult.get() : null;
    }

    private ConfigurableApplicationContext createDefaultApplicationContext() {
        if (!AotDetector.useGeneratedArtifacts()) {
            return new AnnotationConfigApplicationContext();
        }
        return new GenericApplicationContext();
    }
}
```
> 2) `ReactiveWebServerApplicationContextFactory`
```java
class ReactiveWebServerApplicationContextFactory implements ApplicationContextFactory {
	// ... 생략
	@Override
	public ConfigurableApplicationContext create(WebApplicationType webApplicationType) {
		return (webApplicationType != WebApplicationType.REACTIVE) ? null : createContext();
	}

	private ConfigurableApplicationContext createContext() {
		if (!AotDetector.useGeneratedArtifacts()) {
			return new AnnotationConfigReactiveWebServerApplicationContext();
		}
		return new ReactiveWebServerApplicationContext();
	}
}
```
> 3) `ServletWebServerApplicationContextFactory`
```java
class ServletWebServerApplicationContextFactory implements ApplicationContextFactory {
    // ... 생략
	@Override
	public ConfigurableApplicationContext create(WebApplicationType webApplicationType) {
		return (webApplicationType != WebApplicationType.SERVLET) ? null : createContext();
	}

	private ConfigurableApplicationContext createContext() {
		if (!AotDetector.useGeneratedArtifacts()) {
			return new AnnotationConfigServletWebServerApplicationContext();
		}
		return new ServletWebServerApplicationContext();
	}
}
```

### 8. Context 준비 단계
```java
public class SpringApplication {
    // ... 생략
    public ConfigurableApplicationContext run(String... args) {

        // 8. Context 준비 단계
        prepareContext(bootstrapContext, context, environment, listeners, applicationArguments, printedBanner);
        // ... 생략
    }
}
```
- Context 생성 후에 해줘야하는 후처리 작업, Bean들을 등록하는 refresh 단계를 위한 전처리 작업 등이 수행된다.
```java
public class SpringApplication {
    // ... 생략
    private void prepareContext(DefaultBootstrapContext bootstrapContext, ConfigurableApplicationContext context,
                                ConfigurableEnvironment environment, SpringApplicationRunListeners listeners,
                                ApplicationArguments applicationArguments, Banner printedBanner) {
        // 1) Environment를 Application Context에 설정한다.
        context.setEnvironment(environment);
        postProcessApplicationContext(context);
        
        // SpringApplication 생성 단계에서 찾았던 initializer들을 initialize 해주는 등의 작업을 한다.
        addAotGeneratedInitializerIfNecessary(this.initializers);
        applyInitializers(context);

        //  애플리케이션 컨텍스트가 생성되었고, 
        //  initializer들의 initialize까지 진행되었으므로 더이상 BootStrapContext는 불필요하여 BootStrapContext를 종료해주고 있다.
        listeners.contextPrepared(context);
        bootstrapContext.close(context);
        
        if (this.logStartupInfo) {
            logStartupInfo(context.getParent() == null);
            logStartupProfileInfo(context);
        }

        // beanNameGenarator(빈 이름 지정 클래스), resourceLoader(리소스를 불러오는 클래스), conversionService(프로퍼티의 타입 변환) 등이 생성되었으면 싱글톤 Bean으로 등록한다.
        ConfigurableListableBeanFactory beanFactory = context.getBeanFactory();
        
        // 래핑한 ApplicationArguments를 Bean으로 등록 와 배너 클래스를 Bean 으로 등록한다.
        beanFactory.registerSingleton("springApplicationArguments", applicationArguments);
        
        // 배너 클래스를 Bean 으로 등록한다.
        if (printedBanner != null) {
            beanFactory.registerSingleton("springBootBanner", printedBanner);
        }
        
        if (beanFactory instanceof AbstractAutowireCapableBeanFactory autowireCapableBeanFactory) {
            // 순환 참조 여부 설정 : 기본값 false이며 가급적이면 기본값을 사용하도록 하는 것이 좋다.
            autowireCapableBeanFactory.setAllowCircularReferences(this.allowCircularReferences);
            if (beanFactory instanceof DefaultListableBeanFactory listableBeanFactory) {
                // Bean 정보 오버라이딩 여부 설정 : 기본값 false이며 가급적이면 기본값을 사용하도록 하는 것이 좋다.
                listableBeanFactory.setAllowBeanDefinitionOverriding(this.allowBeanDefinitionOverriding);
            }
        }
        
        // LazyInitialize Bean을 처리하는 빈 팩토리 후처리기를 등록
        if (this.lazyInitialization) {
            context.addBeanFactoryPostProcessor(new LazyInitializationBeanFactoryPostProcessor());
        }
        context.addBeanFactoryPostProcessor(new PropertySourceOrderingBeanFactoryPostProcessor(context));
        
        // 소스들을 불러오고
        if (!AotDetector.useGeneratedArtifacts()) {
            // Load the sources
            Set<Object> sources = getAllSources();
            Assert.notEmpty(sources, "Sources must not be empty");
            load(context, sources.toArray(new Object[0]));
        }
        
        // 컨텍스트에 리스너들을 연결하면서 Context 준비 단계를 마무리한다.
        listeners.contextLoaded(context);
    }
}
```

### 9. Context Refresh 단계
```java
public class SpringApplication {
    // ... 생략
    public ConfigurableApplicationContext run(String... args) {

        // 9. Context Refresh 단계
        refreshContext(context);
        // ... 생략
    }}
```
- `refreshContext()`에서는 우리가 만든 `Bean`들을 찾아서 등록하고, 웹 서버를 만들어 실행하는 등의 핵심 작업들이 진행된다.
- 즉, 이 단계를 거치면, 모든 객체들이 싱글톤으로 인스터스화 된다.
- 만약, 에러가 발생하면, 등록된 모든 빈들을 제거한다.
```java
public class SpringApplication {
    // ... 생략
    private void refreshContext(ConfigurableApplicationContext context) {
        if (this.registerShutdownHook) {
            shutdownHook.registerApplicationContext(context);
        }
        refresh(context);
    }

    protected void refresh(ConfigurableApplicationContext applicationContext) {
        applicationContext.refresh();
    }

}
```

```java
public class ServletWebServerApplicationContext extends GenericWebApplicationContext implements ConfigurableWebServerApplicationContext {
    // ... 생략
    @Override
    public final void refresh() throws BeansException, IllegalStateException {
        try {
            super.refresh();
        }
        catch (RuntimeException ex) {
            WebServer webServer = this.webServer;
            if (webServer != null) {
                webServer.stop();
            }
            throw ex;
        }
    }

    @Override
    protected void onRefresh() {
        super.onRefresh();
        try {
            createWebServer();
        }
        catch (Throwable ex) {
            throw new ApplicationContextException("Unable to start web server", ex);
        }
    }
} 
```
- refresh는 start-up 메소드로써 싱글톤 빈으로 등록할 클래스들을 찾아서 생성하고 후처리하는 단계이다. 
- 여기서 후처리는 @Value, @PostConstruct, @Autowired 등을 처리하는 것이다.
- `super.fresh();`는 다음과 같은 순서로 진행된다.
1. refresh 준비 단계 
2. BeanFactory 준비 단계 
3. BeanFactory의 후처리 진행 
4. BeanFactoryPostProcessor 실행 
5. BeanPostProcessor 등록 
6. MessageSource 및 Event Multicaster 초기화 
7. onRefresh(웹 서버 생성)
8. ApplicationListener 조회 및 등록 
9. Bean들의 인스턴스화 및 후처리 
10. refresh 마무리 단계

```java
public abstract class AbstractApplicationContext extends DefaultResourceLoader implements ConfigurableApplicationContext {
    //... 생략
    @Override
    public void refresh() throws BeansException, IllegalStateException {
        synchronized (this.startupShutdownMonitor) {
            StartupStep contextRefresh = this.applicationStartup.start("spring.context.refresh");

            // 1. refresh 준비 단계
            prepareRefresh();

            // 2. BeanFactory 준비 단계
            ConfigurableListableBeanFactory beanFactory = obtainFreshBeanFactory();

            // 3. BeanFactory의 후처리 진행
            prepareBeanFactory(beanFactory);

            try {
                // 4. BeanFactoryPostProcessor 실행
                postProcessBeanFactory(beanFactory);

                StartupStep beanPostProcess = this.applicationStartup.start("spring.context.beans.post-process");

                // 5. BeanPostProcessor 등록
                invokeBeanFactoryPostProcessors(beanFactory);

                // 6. MessageSource 및 Event Multicaster 초기화
                registerBeanPostProcessors(beanFactory);
                beanPostProcess.end();

                // 6. MessageSource 및 Event Multicaster 초기화 
                initMessageSource();
                initApplicationEventMulticaster();

                // 7. onRefresh(예: 서블릿 - 웹 서버 생성)
                onRefresh();

                // 8. ApplicationListener 조회 및 등록
                registerListeners();

                // 9. Bean들의 인스턴스화 및 후처리 
                finishBeanFactoryInitialization(beanFactory);

                // 10. refresh 마무리 단계
                finishRefresh();
            } catch (BeansException ex) {
                if (logger.isWarnEnabled()) {
                    logger.warn("Exception encountered during context initialization - " +
                            "cancelling refresh attempt: " + ex);
                }

                // Destroy already created singletons to avoid dangling resources.
                destroyBeans();

                // Reset 'active' flag.
                cancelRefresh(ex);

                // Propagate exception to caller.
                throw ex;
            } finally {
                // Reset common introspection caches in Spring's core, since we
                // might not ever need metadata for singleton beans anymore...
                resetCommonCaches();
                contextRefresh.end();
            }
        }
    }
}
```

### 9-1. refresh 준비 단계
```java
public abstract class AbstractApplicationContext extends DefaultResourceLoader implements ConfigurableApplicationContext {
    //... 생략
    @Override
    public void refresh() throws BeansException, IllegalStateException {
        synchronized (this.startupShutdownMonitor) {
            // ... 생략
            // 1. refresh 준비 단계
            prepareRefresh();
            // ... 생략
        }
    }
}
```
- prepareRefresh 내부에서는 애플리케이션 컨텍스트의 상태를 active로 변경하는 등의 준비 작업을 하고 있다.
```java
public abstract class AbstractApplicationContext extends DefaultResourceLoader implements ConfigurableApplicationContext {
    //... 생략
    protected void prepareRefresh() {
        // Switch to active.
        this.startupDate = System.currentTimeMillis();
        this.closed.set(false);
        this.active.set(true);
        
        // .. 생략

        // propertySource 초기화
        initPropertySources();

        // Validate that all properties marked as required are resolvable:
        // see ConfigurablePropertyResolver#setRequiredProperties
        getEnvironment().validateRequiredProperties();

        // pre-refresh 애플리케이션 리스너들 초기화
        if (this.earlyApplicationListeners == null) {
            this.earlyApplicationListeners = new LinkedHashSet<>(this.applicationListeners);
        }
        else {
            // Reset local application listeners to pre-refresh state.
            this.applicationListeners.clear();
            this.applicationListeners.addAll(this.earlyApplicationListeners);
        }

        // ApplicationEvents 를 초기화
        this.earlyApplicationEvents = new LinkedHashSet<>();
    }
}
```
- 여기서 애플리케이션 컨텍스트의 상태 중 active를 `true`로 바꾸는 것은 중요하다. 
- 왜냐하면, 빈 팩토리에서 빈을 꺼내는 작업은 active 상태가 `true`일 때만 가능하기 때문이다.
- 대표적으로 `getBean` 메소드를 보면 active 상태가 아니면 throw 하도록 되어있다.
- 물론 아직 빈이 인스턴스화되지 않았기 때문에 빈은 존재하지 않지만 그래도 빈을 찾는 행위 자체는 가능해진 것이다.
```java
public abstract class AbstractApplicationContext extends DefaultResourceLoader implements ConfigurableApplicationContext {
    // ... 생략
    @Override
    public <T> T getBean(Class<T> requiredType) throws BeansException {
        assertBeanFactoryActive();
        return getBeanFactory().getBean(requiredType);
    }
    // ... 생략
    protected void assertBeanFactoryActive() {
        if (!this.active.get()) {
            if (this.closed.get()) {
                throw new IllegalStateException(getDisplayName() + " has been closed already");
            }
            else {
                throw new IllegalStateException(getDisplayName() + " has not been refreshed yet");
            }
        }
    }
}
```

### 9-2. BeanFactory 준비 단계
- 실제 스프링의 빈들은 beanFactory에서 관리되며, 애플리케이션 컨텍스트는 빈 관련 요청이 오면 이를 beanFactory로 위임한다.
- prepareBeanFactory 메소드에서는 beanFactory가 동작하기 위한 준비 작업들이 진행된다.
```java
public abstract class AbstractApplicationContext extends DefaultResourceLoader implements ConfigurableApplicationContext {
    //... 생략
    @Override
    public void refresh() throws BeansException, IllegalStateException {
        synchronized (this.startupShutdownMonitor) {
            // ... 생략
            // 2. BeanFactory 준비 단계
            ConfigurableListableBeanFactory beanFactory = obtainFreshBeanFactory();
            prepareBeanFactory(beanFactory);
            // ... 생략
        }
    }

    protected ConfigurableListableBeanFactory obtainFreshBeanFactory() {
        refreshBeanFactory();
        return getBeanFactory();
    }

    protected void prepareBeanFactory(ConfigurableListableBeanFactory beanFactory) {
        // Tell the internal bean factory to use the context's class loader etc.
        // Bean 클래스들을 불러오기 위한 클래스 로더를 set 해준다.
        beanFactory.setBeanClassLoader(getClassLoader());
        
        // BeanExpressionResolver와 PropertyEditory를 Bean으로 추가한다.
        beanFactory.setBeanExpressionResolver(new StandardBeanExpressionResolver(beanFactory.getBeanClassLoader()));
        beanFactory.addPropertyEditorRegistrar(new ResourceEditorRegistrar(this, getEnvironment()));

        // 의존성 주입을 무시할 인터페이스들(Aware 인터페이스들)을 등록
        beanFactory.addBeanPostProcessor(new ApplicationContextAwareProcessor(this));
        beanFactory.ignoreDependencyInterface(EnvironmentAware.class);
        beanFactory.ignoreDependencyInterface(EmbeddedValueResolverAware.class);
        beanFactory.ignoreDependencyInterface(ResourceLoaderAware.class);
        beanFactory.ignoreDependencyInterface(ApplicationEventPublisherAware.class);
        beanFactory.ignoreDependencyInterface(MessageSourceAware.class);
        beanFactory.ignoreDependencyInterface(ApplicationContextAware.class);
        beanFactory.ignoreDependencyInterface(ApplicationStartupAware.class);

        // BeanFactory와 ApplicationContext 같은 특별한 타입들을 빈으로 등록하기 위한 작업을 진행한 후에 환경변수들을 빈으로 등록하며 마무리한다.
        beanFactory.registerResolvableDependency(BeanFactory.class, beanFactory);
        beanFactory.registerResolvableDependency(ResourceLoader.class, this);
        beanFactory.registerResolvableDependency(ApplicationEventPublisher.class, this);
        beanFactory.registerResolvableDependency(ApplicationContext.class, this);

        // Register early post-processor for detecting inner beans as ApplicationListeners.
        beanFactory.addBeanPostProcessor(new ApplicationListenerDetector(this));

        // Detect a LoadTimeWeaver and prepare for weaving, if found.
        if (!NativeDetector.inNativeImage() && beanFactory.containsBean(LOAD_TIME_WEAVER_BEAN_NAME)) {
            beanFactory.addBeanPostProcessor(new LoadTimeWeaverAwareProcessor(beanFactory));
            // Set a temporary ClassLoader for type matching.
            beanFactory.setTempClassLoader(new ContextTypeMatchClassLoader(beanFactory.getBeanClassLoader()));
        }

        // Register default environment beans.
        if (!beanFactory.containsLocalBean(ENVIRONMENT_BEAN_NAME)) {
            beanFactory.registerSingleton(ENVIRONMENT_BEAN_NAME, getEnvironment());
        }
        if (!beanFactory.containsLocalBean(SYSTEM_PROPERTIES_BEAN_NAME)) {
            beanFactory.registerSingleton(SYSTEM_PROPERTIES_BEAN_NAME, getEnvironment().getSystemProperties());
        }
        if (!beanFactory.containsLocalBean(SYSTEM_ENVIRONMENT_BEAN_NAME)) {
            beanFactory.registerSingleton(SYSTEM_ENVIRONMENT_BEAN_NAME, getEnvironment().getSystemEnvironment());
        }
        if (!beanFactory.containsLocalBean(APPLICATION_STARTUP_BEAN_NAME)) {
            beanFactory.registerSingleton(APPLICATION_STARTUP_BEAN_NAME, getApplicationStartup());
        }
    }

}
```
> cf. Aware 인터페이스는 스프링 프레임워크의 내부 동작에 접근하기 위한 인터페이스이다. 
> Aware 인터페이스에는 접근할 대상을 주입해주는 수정자 메소드(setter)가 존재한다. 
> 예를 들어 ApplicationContextAware에는 setApplicationContext메소드가 있어서 ApplicationContext를 주입할 수 있다. 
> 이러한 Aware 인터페이스는 Spring2.0에 추가되었는데, 과거에는 ApplicationContext나 BeanFactory 등의 구현체를 주입받을 수 없었다. 
> 그래서 해당 구현체들에 접근하려면 Aware 인터페이스를 만들어 주입해주어야 했다. 
> 하지만 지금은 바로 주입이 가능하므로 주로 사용되지는 않지만, 객체가 생성된 이후에 주입해주어야 할 때 사용할 수 있다.
> 그런데 위의 코드에서는 Aware 인터페이스를 의존성 주입 대상에서 Ignore 해주지만 확인해보면 Aware 인터페이스들이 빈으로 등록되어 있다. 추가적인 공부 필요!

### 9-3. BeanFactory의 후처리 진행
```java
public abstract class AbstractApplicationContext extends DefaultResourceLoader implements ConfigurableApplicationContext {
    //... 생략
    @Override
    public void refresh() throws BeansException, IllegalStateException {
        synchronized (this.startupShutdownMonitor) {
            // ... 생략
            // 3. BeanFactory의 후처리 진행
            postProcessBeanFactory(beanFactory);
            // ... 생략
        }
    }
}
```
- 빈 팩토리의 준비가 끝났으면 빈 팩토리를 후처리한다. 
- 예를 들어 서블릿 기반의 웹 애플리케이션이라면 서블릿 관련 클래스들 역시 빈으로 등록되어야 하므로 빈 팩토리에 추가 작업이 필요하다. 
- 이러한 이유로 스프링은 이 과정에 다시 한번 템플릿 메소드 패턴을 적용해서 각각의 애플리케이션 타입에 맞는 후처리를 진행하도록 하고 있다.
- 빈 팩토리의 후처리가 마무리되면 이제 빈 팩토리가 동작할 준비가 끝났다.

### 9-4. BeanFactoryPostProcessor 실행
```java
public abstract class AbstractApplicationContext extends DefaultResourceLoader implements ConfigurableApplicationContext {
    //... 생략
    @Override
    public void refresh() throws BeansException, IllegalStateException {
        synchronized (this.startupShutdownMonitor) {
            // ... 생략
            // 4. BeanFactoryPostProcessor 실행
            invokeBeanFactoryPostProcessors(beanFactory);
            // ... 생략
        }
    }

    protected void invokeBeanFactoryPostProcessors(ConfigurableListableBeanFactory beanFactory) {
        PostProcessorRegistrationDelegate.invokeBeanFactoryPostProcessors(beanFactory, getBeanFactoryPostProcessors());

        // ... 생략
    }
}
```
- BeanFactoryPostProcessor를 실행하는 것은 BeanFactory의 후처리와 다르다.
- BeanFactory의 후처리는 BeanFactory에 대한 추가 작업을 하는 것이고, BeanFactoryPostProcessor는 빈을 탐색하는 것처럼 빈 팩토리가 준비된 후에 해야하는 후처리기들을 실행하는 것이다.
- 대표적으로 싱글톤 객체로 인스턴스화할 빈을 탐색하는 작업을 진행된다.
- 스프링은 인스턴스화를 진행할 빈의 목록(BeanDefinition)을 로딩하는 작업과 실제 인스턴스화를 하는 작업을 나눠서 처리하는데, 인스턴스로 만들 빈의 목록을 찾는 단계가 여기에 속한다.
- 빈 목록은 @Configuration 클래스를 파싱해서 가져오는데, BeanFactoryPostProcessor의 구현체 중 하나인 ConfigurationClassPostProcessor가 진행한다.

```java
final class PostProcessorRegistrationDelegate {
    // ... 생략
    public static void invokeBeanFactoryPostProcessors(
            ConfigurableListableBeanFactory beanFactory, List<BeanFactoryPostProcessor> beanFactoryPostProcessors) {
        
        Set<String> processedBeans = new HashSet<>();

        if (beanFactory instanceof BeanDefinitionRegistry registry) {
            List<BeanFactoryPostProcessor> regularPostProcessors = new ArrayList<>();
            List<BeanDefinitionRegistryPostProcessor> registryProcessors = new ArrayList<>();

            for (BeanFactoryPostProcessor postProcessor : beanFactoryPostProcessors) {
                if (postProcessor instanceof BeanDefinitionRegistryPostProcessor registryProcessor) {

                    // ... 생략
                    registryProcessor.postProcessBeanDefinitionRegistry(registry);
                    registryProcessors.add(registryProcessor);
                }
                else {
                    // ... 생략
                }
            }
            // ... 생략
        } else {
            // ... 생략
        }
        // ... 생략
    }

}
```
```java
public class ConfigurationClassPostProcessor implements BeanDefinitionRegistryPostProcessor,
		BeanRegistrationAotProcessor, BeanFactoryInitializationAotProcessor, PriorityOrdered,
		ResourceLoaderAware, ApplicationStartupAware, BeanClassLoaderAware, EnvironmentAware {

    @Override
    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) {
        // ... 생략
        processConfigBeanDefinitions(registry);
    }
    
    public void processConfigBeanDefinitions(BeanDefinitionRegistry registry) {
        List<BeanDefinitionHolder> configCandidates = new ArrayList<>();
        String[] candidateNames = registry.getBeanDefinitionNames();

        // 1. 파싱을 진행할 설정 빈 이름을 찾음(메인 클래스만 남게됨)
        for (String beanName : candidateNames) {
            BeanDefinition beanDef = registry.getBeanDefinition(beanName);
            if (beanDef.getAttribute(ConfigurationClassUtils.CONFIGURATION_CLASS_ATTRIBUTE) != null) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Bean definition has already been processed as a configuration class: " + beanDef);
                }
            }
            else if (ConfigurationClassUtils.checkConfigurationClassCandidate(beanDef, this.metadataReaderFactory)) {
                configCandidates.add(new BeanDefinitionHolder(beanDef, beanName));
            }
        }

        //  2. 파싱할 클래스가 없으면 종료함
        if (configCandidates.isEmpty()) {
            return;
        }

        // 3. @Order를 참조해 파싱할 클래스들을 정렬함
        configCandidates.sort((bd1, bd2) -> {
            int i1 = ConfigurationClassUtils.getOrder(bd1.getBeanDefinition());
            int i2 = ConfigurationClassUtils.getOrder(bd2.getBeanDefinition());
            return Integer.compare(i1, i2);
        });

        // 4. 빈 이름 생성 전략을 찾아서 처리함
        SingletonBeanRegistry sbr = null;
        if (registry instanceof SingletonBeanRegistry) {
            sbr = (SingletonBeanRegistry) registry;
            if (!this.localBeanNameGeneratorSet) {
                BeanNameGenerator generator = (BeanNameGenerator) sbr.getSingleton(
                        AnnotationConfigUtils.CONFIGURATION_BEAN_NAME_GENERATOR);
                if (generator != null) {
                    this.componentScanBeanNameGenerator = generator;
                    this.importBeanNameGenerator = generator;
                }
            }
        }

        if (this.environment == null) {
            this.environment = new StandardEnvironment();
        }

        // 5. 파서를 생성하고 모든 @Configuration 클래스를 파싱함
        ConfigurationClassParser parser = new ConfigurationClassParser(
                this.metadataReaderFactory, this.problemReporter, this.environment,
                this.resourceLoader, this.componentScanBeanNameGenerator, registry);

        Set<BeanDefinitionHolder> candidates = new LinkedHashSet<>(configCandidates);
        Set<ConfigurationClass> alreadyParsed = new HashSet<>(configCandidates.size());
        do {
            StartupStep processConfig = this.applicationStartup.start("spring.context.config-classes.parse");
            parser.parse(candidates);
            parser.validate();

            Set<ConfigurationClass> configClasses = new LinkedHashSet<>(parser.getConfigurationClasses());
            configClasses.removeAll(alreadyParsed);

            // Read the model and create bean definitions based on its content
            if (this.reader == null) {
                this.reader = new ConfigurationClassBeanDefinitionReader(
                        registry, this.sourceExtractor, this.resourceLoader, this.environment,
                        this.importBeanNameGenerator, parser.getImportRegistry());
            }
            this.reader.loadBeanDefinitions(configClasses);
            alreadyParsed.addAll(configClasses);
            processConfig.tag("classCount", () -> String.valueOf(configClasses.size())).end();

            candidates.clear();
            if (registry.getBeanDefinitionCount() > candidateNames.length) {
                String[] newCandidateNames = registry.getBeanDefinitionNames();
                Set<String> oldCandidateNames = Set.of(candidateNames);
                Set<String> alreadyParsedClasses = new HashSet<>();
                for (ConfigurationClass configurationClass : alreadyParsed) {
                    alreadyParsedClasses.add(configurationClass.getMetadata().getClassName());
                }
                for (String candidateName : newCandidateNames) {
                    if (!oldCandidateNames.contains(candidateName)) {
                        BeanDefinition bd = registry.getBeanDefinition(candidateName);
                        if (ConfigurationClassUtils.checkConfigurationClassCandidate(bd, this.metadataReaderFactory) &&
                                !alreadyParsedClasses.contains(bd.getBeanClassName())) {
                            candidates.add(new BeanDefinitionHolder(bd, candidateName));
                        }
                    }
                }
                candidateNames = newCandidateNames;
            }
        }
        while (!candidates.isEmpty());

        // Register the ImportRegistry as a bean in order to support ImportAware @Configuration classes
        if (sbr != null && !sbr.containsSingleton(IMPORT_REGISTRY_BEAN_NAME)) {
            sbr.registerSingleton(IMPORT_REGISTRY_BEAN_NAME, parser.getImportRegistry());
        }

        // Store the PropertySourceDescriptors to contribute them Ahead-of-time if necessary
        this.propertySourceDescriptors = parser.getPropertySourceDescriptors();

        if (this.metadataReaderFactory instanceof CachingMetadataReaderFactory cachingMetadataReaderFactory) {
            // Clear cache in externally provided MetadataReaderFactory; this is a no-op
            // for a shared cache since it'll be cleared by the ApplicationContext.
            cachingMetadataReaderFactory.clearCache();
        }
    }
}
```
- 위의 코드는 클래스를 파싱해서 우리가 선언한 빈들을 찾는 로직이다.

- 
- 
- 
- 
- 즉, 이 단계는 BeanFactory가 준비되고 빈을 인스턴스화하기 전의 중간 단계로써 빈의 목록을 불러오고, 불러온 빈의 메타 정보를 조작하기 위한 BeanFactoryPostProcessor를 객체로 만들어 실행시키는 것이다.





### 9-5. BeanPostProcessor 등록
```java
public abstract class AbstractApplicationContext extends DefaultResourceLoader implements ConfigurableApplicationContext {
    //... 생략
    @Override
    public void refresh() throws BeansException, IllegalStateException {
        synchronized (this.startupShutdownMonitor) {
            // ... 생략
            StartupStep beanPostProcess = this.applicationStartup.start("spring.context.beans.post-process");

            // 5. BeanPostProcessor 등록
            registerBeanPostProcessors(beanFactory);
            beanPostProcess.end();
            // ... 생략
        }
    }
}
```
- 그 다음에는 빈들이 생성되고 나서 빈의 내용이나 빈 자체를 변경하기 위한 빈 후처리기인 BeanPostProcessor를 등록해주고 있다. 
- 대표적으로 @Value, @PostConstruct, @Autowired 등이 BeanPostProcessor에 의해 처리되며 이를 위한 BeanPostProcessor 구현체들이 등록된다. 
- 대표적으로 CommonAnnotationBeanPostProcessor는 @PostConstruct와 @PreDestroy를 처리하기 위해 등록된다. 
- 그러므로 의존성 주입은 빈 후처리기에 의해 처리되는 것이며, 실제 빈 대신에 프록시 빈으로 교체되는 작업 역시 빈 후처리기에 의해 처리된다. 
- BeanPostProcessor는 빈이 생성된 후 초기화 메소드가 호출되기 직전과 호출된 직후에 처리가능한 2개의 메소드를 제공하고 있다.





### 9-6. MessageSource 및 Event Multicaster 초기화
```java
public abstract class AbstractApplicationContext extends DefaultResourceLoader implements ConfigurableApplicationContext {
    //... 생략
    @Override
    public void refresh() throws BeansException, IllegalStateException {
        synchronized (this.startupShutdownMonitor) {
            // ... 생략
            // 6. MessageSource 및 Event Multicaster 초기화
            initMessageSource();
            initApplicationEventMulticaster();
            // ... 생략
        }
    }
}
```
- BeanPostProcessor를 등록한 후에는 다국어 처리를 위한 MessageSource와 ApplicationListener에 event를 publish하기 위한 Event Multicaster를 초기화하고 있다.
### 9-7. onRefresh(웹 서버 생성)
```java
public abstract class AbstractApplicationContext extends DefaultResourceLoader implements ConfigurableApplicationContext {
    //... 생략
    @Override
    public void refresh() throws BeansException, IllegalStateException {
        synchronized (this.startupShutdownMonitor) {
            // ... 생략
            // 7. onRefresh(웹 서버 생성)
            onRefresh();

            // ... 생략
        }
    }
}
```




### 9-8. ApplicationListener 조회 및 등록
```java
public abstract class AbstractApplicationContext extends DefaultResourceLoader implements ConfigurableApplicationContext {
    //... 생략
    @Override
    public void refresh() throws BeansException, IllegalStateException {
        synchronized (this.startupShutdownMonitor) {
            // ... 생략
            // 8. ApplicationListener 조회 및 등록
            registerListeners();

            // ... 생략
        }
    }

    protected void registerListeners() {
        // Register statically specified listeners first.
        for (ApplicationListener<?> listener : getApplicationListeners()) {
            getApplicationEventMulticaster().addApplicationListener(listener);
        }

        // Do not initialize FactoryBeans here: We need to leave all regular beans
        // uninitialized to let post-processors apply to them!
        String[] listenerBeanNames = getBeanNamesForType(ApplicationListener.class, true, false);
        for (String listenerBeanName : listenerBeanNames) {
            getApplicationEventMulticaster().addApplicationListenerBean(listenerBeanName);
        }

        // Publish early application events now that we finally have a multicaster...
        Set<ApplicationEvent> earlyEventsToProcess = this.earlyApplicationEvents;
        this.earlyApplicationEvents = null;
        if (!CollectionUtils.isEmpty(earlyEventsToProcess)) {
            for (ApplicationEvent earlyEvent : earlyEventsToProcess) {
                getApplicationEventMulticaster().multicastEvent(earlyEvent);
            }
        }
    }
}
```
- 그 다음에는 ApplicationListener의 구현체들을 찾아서 EventMultiCaster에 등록해주고 있다.




### 9-9. Bean들의 인스턴스화 및 후처리
- 이제 등록된 빈 정의(BeanDefinition)를 바탕으로 객체를 생성할 차례이다. BeanDefinition에는 빈 이름, 스코프 등과 같은 정보가 있어서 이를 바탕으로 객체를 생성하게 된다.

```java
public abstract class AbstractApplicationContext extends DefaultResourceLoader implements ConfigurableApplicationContext {
    //... 생략
    @Override
    public void refresh() throws BeansException, IllegalStateException {
        synchronized (this.startupShutdownMonitor) {
            // ... 생략
            // 9. 빈들의 인스턴스화 및 후처리
            finishBeanFactoryInitialization(beanFactory);

            // ... 생략
        }
    }

    protected void finishBeanFactoryInitialization(ConfigurableListableBeanFactory beanFactory) {
        // .. 생략

        // Allow for caching all bean definition metadata, not expecting further changes.
        beanFactory.freezeConfiguration();

        // 객체를 생성하는 부분
        beanFactory.preInstantiateSingletons();
    }
}
```
- 객체를 생성하는 부분은 가장 마지막 줄의 `preInstantiateSingletons`이다.
- 그 전에 더 이상 남은 빈 팩토리 작업과 누락된 빈 정보가 없으므로 빈 팩토리의 설정과 `BeanDefinitionNames`를 freeze 해주고 있다.
- 그리고 객체를 생성하는데, BeanFactory의 구현체인 `DefaultListableBeanFactory`를 통해 해당 로직을 살펴보자.
```java
public class DefaultListableBeanFactory extends AbstractAutowireCapableBeanFactory implements ConfigurableListableBeanFactory, BeanDefinitionRegistry, Serializable {
    @Override
    public void preInstantiateSingletons() throws BeansException {
        if (logger.isTraceEnabled()) {
            logger.trace("Pre-instantiating singletons in " + this);
        }

        // Iterate over a copy to allow for init methods which in turn register new bean definitions.
        // While this may not be part of the regular factory bootstrap, it does otherwise work fine.
        List<String> beanNames = new ArrayList<>(this.beanDefinitionNames);

        // Trigger initialization of all non-lazy singleton beans...
        for (String beanName : beanNames) {
            RootBeanDefinition bd = getMergedLocalBeanDefinition(beanName);
            if (!bd.isAbstract() && bd.isSingleton() && !bd.isLazyInit()) {
                if (isFactoryBean(beanName)) {
                    Object bean = getBean(FACTORY_BEAN_PREFIX + beanName);
                    if (bean instanceof SmartFactoryBean<?> smartFactoryBean && smartFactoryBean.isEagerInit()) {
                        getBean(beanName);
                    }
                } else {
                    getBean(beanName);
                }
            }
        }

        // Trigger post-initialization callback for all applicable beans...
        for (String beanName : beanNames) {
            //.. 생략
        }
    }
}
```
- `DefaultListableBeanFactory`의 `getBean` 내부에서는 요청을 `AbstractBeanFactory`의 `doGetBean`으로 위임하고 있다.
- `doGetBean` 내부에서는 해당 빈 이름으로 만들어진 빈이 존재하는지를 검사하여 있으면 꺼내서 반환하고 없으면 생성해서 반환해주고 있다. 
```java
public abstract class AbstractBeanFactory extends FactoryBeanRegistrySupport implements ConfigurableBeanFactory {
    @Override
    public Object getBean(String name) throws BeansException {
        return doGetBean(name, null, null, false);
    }

    protected <T> T doGetBean(String name, @Nullable Class<T> requiredType, @Nullable Object[] args, boolean typeCheckOnly) throws BeansException {

        String beanName = transformedBeanName(name);
        Object beanInstance;

        // 빈이 이미 등록되었거나 캐싱된 빈이 존재하는지 검사
        Object sharedInstance = getSingleton(beanName);
        if (sharedInstance != null && args == null) {
            // .. 생략
            // 빈이 이미 등록되었거나 캐싱된 빈이 존재하는 경우 생성X
            beanInstance = getObjectForBeanInstance(sharedInstance, name, beanName, null);
        } else {
            // .. 생략
            try {
                // .. 생략

                // BeanDefinition에서 scope 정보를 참조해 빈을 생성함
                if (mbd.isSingleton()) {
                    sharedInstance = getSingleton(beanName, () -> {
                        try {
                            return createBean(beanName, mbd, args);
                        } catch (BeansException ex) {
                            // Explicitly remove instance from singleton cache: It might have been put there
                            // eagerly by the creation process, to allow for circular reference resolution.
                            // Also remove any beans that received a temporary reference to the bean.
                            destroySingleton(beanName);
                            throw ex;
                        }
                    });
                    beanInstance = getObjectForBeanInstance(sharedInstance, name, beanName, mbd);
                } else if (mbd.isPrototype()) {
                    // 프로토타입인 경우에 다른 객체 생성 로직 존재
                    // ... 생략
                } else {
                    // ... 생략
                }
            } catch (BeansException ex) {
                beanCreation.tag("exception", ex.getClass().toString());
                beanCreation.tag("message", String.valueOf(ex.getMessage()));
                cleanupAfterBeanCreationFailure(beanName);
                throw ex;
            } finally {
                beanCreation.end();
            }
        }

        return adaptBeanInstance(name, beanInstance, requiredType);
    }
}
```
- 현재 우리가 보고 있는 클래스는 AbstractBeanFactory인데, 위의 코드에서 싱글톤 객체를 만드는 createBean 부분은 추상 메소드이며 세부 구현은 AbstractAutowireCapableBeanFactory를 참조해야 한다.

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

        if (logger.isTraceEnabled()) {
            logger.trace("Creating instance of bean '" + beanName + "'");
        }
        
        RootBeanDefinition mbdToUse = mbd;

        // Make sure bean class is actually resolved at this point, and
        // clone the bean definition in case of a dynamically resolved Class
        // which cannot be stored in the shared merged bean definition.
        Class<?> resolvedClass = resolveBeanClass(mbd, beanName);
        if (resolvedClass != null && !mbd.hasBeanClass() && mbd.getBeanClassName() != null) {
            mbdToUse = new RootBeanDefinition(mbd);
            mbdToUse.setBeanClass(resolvedClass);
        }

        // Prepare method overrides.
        try {
            mbdToUse.prepareMethodOverrides();
        } catch (BeanDefinitionValidationException ex) {
            throw new BeanDefinitionStoreException(mbdToUse.getResourceDescription(),
                    beanName, "Validation of method overrides failed", ex);
        }

        try {
            // Give BeanPostProcessors a chance to return a proxy instead of the target bean instance.
            Object bean = resolveBeforeInstantiation(beanName, mbdToUse);
            if (bean != null) {
                return bean;
            }
        } catch (Throwable ex) {
            throw new BeanCreationException(mbdToUse.getResourceDescription(), beanName,
                    "BeanPostProcessor before instantiation of bean failed", ex);
        }

        try {
            Object beanInstance = doCreateBean(beanName, mbdToUse, args);
            if (logger.isTraceEnabled()) {
                logger.trace("Finished creating instance of bean '" + beanName + "'");
            }
            return beanInstance;
        } catch (BeanCreationException | ImplicitlyAppearedSingletonException ex) {
            // A previously detected exception with proper bean creation context already,
            // or illegal singleton state to be communicated up to DefaultSingletonBeanRegistry.
            throw ex;
        } catch (Throwable ex) {
            throw new BeanCreationException(
                    mbdToUse.getResourceDescription(), beanName, "Unexpected exception during bean creation", ex);
        }
    }

    protected Object doCreateBean(String beanName, RootBeanDefinition mbd, @Nullable Object[] args)
            throws BeanCreationException {

        // Instantiate the bean.
        BeanWrapper instanceWrapper = null;
        
        // ... 생략
        if (instanceWrapper == null) {
            instanceWrapper = createBeanInstance(beanName, mbd, args);
        }

        // ... 생략
        try {
            // ... 생략
            populateBean(beanName, mbd, instanceWrapper);
            
        } catch (Throwable ex) {
            // ... 생략
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

    protected void populateBean(String beanName, RootBeanDefinition mbd, @Nullable BeanWrapper bw) {
        if (bw == null) {
            if (mbd.hasPropertyValues()) {
                throw new BeanCreationException(
                        mbd.getResourceDescription(), beanName, "Cannot apply property values to null instance");
            }
            else {
                // Skip property population phase for null instance.
                return;
            }
        }

        if (bw.getWrappedClass().isRecord()) {
            if (mbd.hasPropertyValues()) {
                throw new BeanCreationException(
                        mbd.getResourceDescription(), beanName, "Cannot apply property values to a record");
            }
            else {
                // Skip property population phase for records since they are immutable.
                return;
            }
        }

        // Give any InstantiationAwareBeanPostProcessors the opportunity to modify the
        // state of the bean before properties are set. This can be used, for example,
        // to support styles of field injection.
        if (!mbd.isSynthetic() && hasInstantiationAwareBeanPostProcessors()) {
            for (InstantiationAwareBeanPostProcessor bp : getBeanPostProcessorCache().instantiationAware) {
                if (!bp.postProcessAfterInstantiation(bw.getWrappedInstance(), beanName)) {
                    return;
                }
            }
        }

        PropertyValues pvs = (mbd.hasPropertyValues() ? mbd.getPropertyValues() : null);

        // 1) 
        int resolvedAutowireMode = mbd.getResolvedAutowireMode();
        if (resolvedAutowireMode == AUTOWIRE_BY_NAME || resolvedAutowireMode == AUTOWIRE_BY_TYPE) {
            MutablePropertyValues newPvs = new MutablePropertyValues(pvs);
            // Add property values based on autowire by name if applicable.
            if (resolvedAutowireMode == AUTOWIRE_BY_NAME) {
                autowireByName(beanName, mbd, bw, newPvs);
            }
            // Add property values based on autowire by type if applicable.
            if (resolvedAutowireMode == AUTOWIRE_BY_TYPE) {
                autowireByType(beanName, mbd, bw, newPvs);
            }
            pvs = newPvs;
        }
        
        // 2) ㅇㅇ
        if (hasInstantiationAwareBeanPostProcessors()) {
            if (pvs == null) {
                pvs = mbd.getPropertyValues();
            }
            for (InstantiationAwareBeanPostProcessor bp : getBeanPostProcessorCache().instantiationAware) {
                
                // 실제 의존선 주입 발생
                PropertyValues pvsToUse = bp.postProcessProperties(pvs, bw.getWrappedInstance(), beanName);
                
                if (pvsToUse == null) {
                    return;
                }
                pvs = pvsToUse;
            }
        }
        // ... 생략
    }

    protected void autowireByName(
            String beanName, AbstractBeanDefinition mbd, BeanWrapper bw, MutablePropertyValues pvs) {

        String[] propertyNames = unsatisfiedNonSimpleProperties(mbd, bw);
        for (String propertyName : propertyNames) {
            if (containsBean(propertyName)) {
                Object bean = getBean(propertyName);
                pvs.add(propertyName, bean);
                registerDependentBean(propertyName, beanName);
                if (logger.isTraceEnabled()) {
                    logger.trace("Added autowiring by name from bean name '" + beanName +
                            "' via property '" + propertyName + "' to bean named '" + propertyName + "'");
                }
            }
            else {
                if (logger.isTraceEnabled()) {
                    logger.trace("Not autowiring property '" + propertyName + "' of bean '" + beanName +
                            "' by name: no matching bean found");
                }
            }
        }
    }

    protected void autowireByType(
            String beanName, AbstractBeanDefinition mbd, BeanWrapper bw, MutablePropertyValues pvs) {

        TypeConverter converter = getCustomTypeConverter();
        if (converter == null) {
            converter = bw;
        }

        Set<String> autowiredBeanNames = new LinkedHashSet<>(4);
        String[] propertyNames = unsatisfiedNonSimpleProperties(mbd, bw);
        for (String propertyName : propertyNames) {
            try {
                PropertyDescriptor pd = bw.getPropertyDescriptor(propertyName);
                // Don't try autowiring by type for type Object: never makes sense,
                // even if it technically is an unsatisfied, non-simple property.
                if (Object.class != pd.getPropertyType()) {
                    MethodParameter methodParam = BeanUtils.getWriteMethodParameter(pd);
                    // Do not allow eager init for type matching in case of a prioritized post-processor.
                    boolean eager = !(bw.getWrappedInstance() instanceof PriorityOrdered);
                    DependencyDescriptor desc = new AutowireByTypeDependencyDescriptor(methodParam, eager);
                    Object autowiredArgument = resolveDependency(desc, beanName, autowiredBeanNames, converter);
                    if (autowiredArgument != null) {
                        pvs.add(propertyName, autowiredArgument);
                    }
                    for (String autowiredBeanName : autowiredBeanNames) {
                        registerDependentBean(autowiredBeanName, beanName);
                        
                        
                        
                        if (logger.isTraceEnabled()) {
                            logger.trace("Autowiring by type from bean name '" + beanName + "' via property '" +
                                    propertyName + "' to bean named '" + autowiredBeanName + "'");
                        }
                    }
                    autowiredBeanNames.clear();
                }
            }
            catch (BeansException ex) {
                throw new UnsatisfiedDependencyException(mbd.getResourceDescription(), beanName, propertyName, ex);
            }
        }
    }

}
```

```java
public class AutowiredAnnotationBeanPostProcessor implements SmartInstantiationAwareBeanPostProcessor,
        MergedBeanDefinitionPostProcessor, BeanRegistrationAotProcessor, PriorityOrdered, BeanFactoryAware {
    @Override
    public PropertyValues postProcessProperties(PropertyValues pvs, Object bean, String beanName) {
        InjectionMetadata metadata = findAutowiringMetadata(beanName, bean.getClass(), pvs);
        try {
            metadata.inject(bean, beanName, pvs);
        }
        catch (BeanCreationException ex) {
            throw ex;
        }
        catch (Throwable ex) {
            throw new BeanCreationException(beanName, "Injection of autowired dependencies failed", ex);
        }
        return pvs;
    }
}
```

```java
public class InjectionMetadata {
    public void inject(Object target, @Nullable String beanName, @Nullable PropertyValues pvs) throws Throwable {
        Collection<InjectedElement> checkedElements = this.checkedElements;
        Collection<InjectedElement> elementsToIterate =
                (checkedElements != null ? checkedElements : this.injectedElements);
        if (!elementsToIterate.isEmpty()) {
            for (InjectedElement element : elementsToIterate) {
                // inject 발생
                element.inject(target, beanName, pvs);
            }
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



```java
public class DefaultSingletonBeanRegistry extends SimpleAliasRegistry implements SingletonBeanRegistry {
    public void registerDependentBean(String beanName, String dependentBeanName) {
        String canonicalName = canonicalName(beanName);

        synchronized (this.dependentBeanMap) {
            Set<String> dependentBeans = this.dependentBeanMap.computeIfAbsent(canonicalName, k -> new LinkedHashSet<>(8));
            if (!dependentBeans.add(dependentBeanName)) {
                return;
            }
        }

        synchronized (this.dependenciesForBeanMap) {
            Set<String> dependenciesForBean = this.dependenciesForBeanMap.computeIfAbsent(dependentBeanName, k -> new LinkedHashSet<>(8));
            dependenciesForBean.add(canonicalName);
        }
    }
}


```


### 9-10. refresh 마무리 단계
```java
public abstract class AbstractApplicationContext extends DefaultResourceLoader implements ConfigurableApplicationContext {
    //... 생략
    @Override
    public void refresh() throws BeansException, IllegalStateException {
        synchronized (this.startupShutdownMonitor) {
            // ... 생략
            // 10. refresh 마무리 단계
            finishRefresh();
            // ... 생략
        }
    }
}
```
- 모든 빈들을 인스턴스화하였다면 이제 refresh를 마무리하는 단계이다. 
- 여기서는 애플리케이션 컨텍스트를 준비하기 위해 사용되었던 resourceCache를 제거하고, Lifecycle Processor를 초기화하여 refresh를 전파하고, 최종 이벤트를 전파하며 마무리된다.
- Lifecycle Processor에는 웹서버와 관련된 부분이 있어서 refresh가 전파되면, 웹서버가 실행된다.

### 10. Context Refresh 후처리 단계
```java
public class SpringApplication {
    // ... 생략
    public ConfigurableApplicationContext run(String... args) {
        // 10. Context Refresh 후처리 단계
        afterRefresh(context, applicationArguments);
        // ... 생략
    }

    protected void afterRefresh(ConfigurableApplicationContext context, ApplicationArguments args) {
    }
}
```
- 애플리케이션 컨텍스트의 refresh 단계가 마무리되고 나서 후처리하는 단계이다. 
- 현재 `afterRefresh` 메서드는 비어있는 상태이다.
- 과거에는 애플리케이션 컨텍스트 생성 후에 초기화 작업을 위한 ApplicationRunner, CommandLineRunner를 호출하는 callRunners()가 내부에 존재했는데, 현재는 별도의 단계로 빠져있다.

### 11. 실행 시간 출력 및 리스너 started 처리
```java
public class SpringApplication {
    // ... 생략
    public ConfigurableApplicationContext run(String... args) {

        // 11. 실행시간 출력 및 리스너 start 처리
        Duration timeTakenToStartup = Duration.ofNanos(System.nanoTime() - startTime);
        if (this.logStartupInfo) {
            new StartupInfoLogger(this.mainApplicationClass).logStarted(getApplicationLog(), timeTakenToStartup);
        }
        listeners.started(context, timeTakenToStartup);
        // ... 생략
    }}
```
- 이후에 애플리케이션을 시작하는데 걸린 시간을 로그로 남기고 리스너들을 started 처리하고 있다.

### 12. Runners 실행
```java
public class SpringApplication {
    // ... 생략
    public ConfigurableApplicationContext run(String... args) {
        
        // 12. Runners 실행
        callRunners(context, applicationArguments);
        // ... 생략
    }

    private void callRunners(ApplicationContext context, ApplicationArguments args) {
        List<Object> runners = new ArrayList<>();
        runners.addAll(context.getBeansOfType(ApplicationRunner.class).values());
        runners.addAll(context.getBeansOfType(CommandLineRunner.class).values());
        AnnotationAwareOrderComparator.sort(runners);
        for (Object runner : new LinkedHashSet<>(runners)) {
            if (runner instanceof ApplicationRunner applicationRunner) {
                callRunner(applicationRunner, args);
            }
            if (runner instanceof CommandLineRunner commandLineRunner) {
                callRunner(commandLineRunner, args);
            }
        }
    }
}
```
- Runner들을 호출하는 단계이다. 
- 우리는 때로 애플리케이션이 실행된 이후에 초기화 작업을 필요로 하는 경우가 있다. 
- 그럴때 사용할 수 있는 선택지 중 하나가 Runner를 등록하는 방법이다. 
- Runner에는 총 2가지가 존재하는데, String을 파라미터로 필요로하는 넘기는 경우에는 `CommandLineRunner`를, 다른 타입을 파라미터로 필요로 하는 경우에는 `ApplicationRunner`를 사용할 수 있다.



# Reference
- https://mangkyu.tistory.com/213