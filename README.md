# service-executor
## Pool of SOAP services to execute ones
### Artifact to economize computer resources.
Service should registered in pool once then consumer can get Call-entity anytime 
to execute WS-RPC via SOAP protocol or any other one.  

Language **Java8**
Libraries **Spring Core, Spring Context, JDOM2**
Build tool **gradle**.

### There are two things to run services pool.  

- ##### ServiceMeta - special class to describe service for pool.    
Example of build `ServiceMeta` for Service "**PeriodictableSoap**".  

       ServiceBuilder builder = new ServiceBuilderImpl().interfaceClass(PeriodictableSoap.class);  
      OperationBuilder oBuilder = builder.id("TestWebFacade").operationBuilder();  
      builder.operations(
        oBuilder.name("getAtoms").result(String.class).build(),  
        oBuilder.name("getAtomicWeight").parameter(String.class).result(String.class).build(),  
        oBuilder.name("getAtomicNumber").parameter(String.class).result(String.class).build(),  
        oBuilder.name("getElementSymbol").parameter(String.class).result(String.class).build(),  
        oBuilder.name("toString").result(String.class).build()
     );  
     return builder.build();
which returns `ServiceMeta` instance.
- ##### Service instance builder.
Builder for service instance, lambda **{(clazz) -> new Periodictable().getPeriodictableSoap();}**
-  ##### Then get them all together  
       ServiceMeta meta = makeWebServiceMeta();  
       pool = new ServiceInstancesPool(meta, (clazz) -> new Periodictable().getPeriodictableSoap());
    
    Pool have important parameters to adjust:
    - **minimumInstances** - minimal quantity of service instances which will start;
    - **maximumInstances** - maximum capacity of pool;
    - **exclusiveDelayValue** - delay in milliseconds between remote call.
- ##### Integration test **ServiceInstancePoolIT** demonstrate the usage of library.      
    
`    If you have any question, please contact me directly by oleg.sopilnyak@gmail.com`