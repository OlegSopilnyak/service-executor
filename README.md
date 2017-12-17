# service-executor
#Pool of SOAP services to execute ones<br/>
###Artifact to economize computer resources.<br>
Service should registered in pool once then consumer can get Call-entity to execute<br/>
SOAP service operation call.<br/>

Language **Java8**<br>
Libraries **Spring Core, Spring Context, JDOM2**<br>
Build tool **gradle**.

###There are two things to run services pool.<br/>

- ######ServiceMeta - special class to describe service for pool.<br/>
Example of build `ServiceMeta` for Service "**PeriodictableSoap**".  
+     ServiceBuilder builder = new ServiceBuilderImpl().interfaceClass(PeriodictableSoap.class);  
+     OperationBuilder oBuilder = builder.id("TestWebFacade").operationBuilder();  
+     builder.operations(
        oBuilder.name("getAtoms").result(String.class).build(),  
        oBuilder.name("getAtomicWeight").parameter(String.class).result(String.class).build(),  
        oBuilder.name("getAtomicNumber").parameter(String.class).result(String.class).build(),  
        oBuilder.name("getElementSymbol").parameter(String.class).result(String.class).build(),  
        oBuilder.name("toString").result(String.class).build()
+     );  
+     return builder.build();
which returns `ServiceMeta` instance.
- ######Service instance builder.<br/>
Builder for service instance, lambda **(clazz) -> new Periodictable().getPeriodictableSoap()**
-  ######Then get them all together<br>
+     ServiceMeta meta = makeWebServiceMeta();  
+     ``pool = new ServiceInstancesPool(meta, (clazz) -> new Periodictable().getPeriodictableSoap());
    
    Pool have important parameters to adjust:<br/>
    - **minimumInstances** - minimal quantity of service instances which will start;
    - **maximumInstances** - maximum capacity of pool;
    - **exclusiveDelayValue** - delay in milliseconds between remote call. <br>
- ###### Integration test **ServiceInstancePoolIT** demonstrate the usage of library.      
    
`    If you have any question, please contact me directly by oleg.sopilnyak@gmail.com`