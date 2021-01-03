#Posted on
https://gitter.im/TestFX/TestFX#
at 1.1.2021 by Hu1buerger
#Issue

Hey yall. I am migrating an ant project to gradle. I am uncertain which testFx is used. Therfore i tried all that i found on maven. But i am running gradle with

``` gradle
testCompile group: 'org.testfx', name: 'testfx-core', version: '4.0.1-alpha'
testCompile group: 'org.testfx', name: 'testfx-junit', version: '4.0.1-alpha'
```

But i am getting either

```
error: cannot access Predicate
public class ClassIdentifierTest extends ApplicationTest implements Closeable {
^
```

or

```
error: cannot access ApplicationFixture
```

The issue dosnt seem to be documented on the internet or the README