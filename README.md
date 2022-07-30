# aparx' eventbus (dev branch)
Advanced and lightweight eventbus library for the Java language. 
<br/>It embraces the separation of concerns design principle and adds modularity to fully customize the behaviour.

## Documentation
The documentation follows.

### Example
```java
        EventBus bus = new EventBus(
                EventProcessors.newPolymorphicPublisher(),
                EventProcessors.newDefaultMethodCollector());
        bus.register(new TestListener());
        bus.publish(new PolymorphicEvent(0, "Hello!"));
```
* A method within `TestListener` must have one parameter only, being of type `Event`<br>and not static.
As initially stated, ***everything*** can be customized and is modular.

## Download
The downloads follow with the first public release.

## WARNING❗
This library is not production nor test or alpha ready at all. 
Not even core functions are implemented yet.
<br/>Please wait until a stable alpha or even version is pre-released.
<br/>You are ensured to encounter bugs very quickly.
<br/>Also the documentations are completely missing yet.
