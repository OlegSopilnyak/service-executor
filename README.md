# service-executor
Pool of SOAP services to execute<br/>
Artifact to economize computer resources.<br>
Service registered in pool once then consumer can get Call-entity to execute<br/>
SOAP service operation call.<br/>

Language **Java8**<br>
Libraries **Spring Core, Spring Context, JDOM2**<br>
Build tool **gradle**.

There are two things to run services pool.<br/>

1. ServiceMeta - special class to describe service for pool.<br/>
Example for Service "PeriodictableSoap".
_ServiceBuilder builder = new ServiceBuilderImpl().interfaceClass(PeriodictableSoap.class);<br/>
OperationBuilder oBuilder = builder.id("TestWebFacade").operationBuilder();<br/>
builder.operations(<br/>
        oBuilder.name("getAtoms").result(String.class).build(),<br/>
        oBuilder.name("getAtomicWeight").parameter(String.class).result(String.class).build(),<br/>
        oBuilder.name("getAtomicNumber").parameter(String.class).result(String.class).build(),<br/>
        oBuilder.name("getElementSymbol").parameter(String.class).result(String.class).build(),<br/>
        oBuilder.name("toString").result(String.class).build()<br/>
);<br/>
return builder.build();<br/>_ which returns ServiceMeta instance.<br/>
2. Service instance builder.<br/>
Builder for (clazz) -> new Periodictable().getPeriodictableSoap() <br>

3.  Then get its together<br>
_ServiceMeta meta = makeWebServiceMeta();<br>
pool = new ServiceInstancesPool(meta, (clazz) -> new Periodictable().getPeriodictableSoap());_
    
    Pool have important parameters to adjust:<br/>
    - **minimumInstances** - minimal quantity of service instances which will start;
    - **maximumInstances** - maximum capacity of pool;
    - **exclusiveDelayValue** - delay in milliseconds between remote call. 
4. Integration test **ServiceInstancePoolIT** demonstrate the usage of library.<br>    
    
`    If you have any question, please contact me directly by oleg.sopilnyak@gmail.com`