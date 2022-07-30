package io.github.aparx.eventbus.subscriber;

import io.github.aparx.eventbus.Event;
import io.github.aparx.eventbus.audience.ListenerHandle;
import org.junit.jupiter.api.*;

import java.util.Arrays;

/**
 * @author aparx (Vinzent Zeband)
 * @version 09:46 CET, 30.07.2022
 * @since 1.0
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class SubscriberCollectionsTests {

    // TODO this test is not a good test and reliant on the environment
    //   and order of tests. It must be changed for the future if possible.

    static EventSubscriber<Event> A = EventSubscribers.newCallbackSubscriber(EventCallback.empty());
    static EventSubscriber<Event> B = EventSubscribers.newCallbackSubscriber(EventCallback.empty());
    static EventSubscriber<Event> C = EventSubscribers.newCallbackSubscriber(EventCallback.empty());
    static EventSubscriber<Event> D = EventSubscribers.newCallbackSubscriber(EventCallback.empty());
    static EventSubscriber<TestEvent> E = EventSubscribers.newCallbackSubscriber(TestEvent.class, EventCallback.empty());
    static TestSubscriber F = new TestSubscriber();
    static SubscriberCollection<Event, EventSubscriber<? extends Event>> CON
            = SubscriberCollections.newOfMultimapFactory(
            SubscriberCollections.MultimapFactory.newUnsortedHashset());

    static class TestSubscriber extends EventSubscriber<TestEvent> {

        public TestSubscriber() {
            super(TestEvent.class);
        }

        @Override
        public void call(ListenerHandle origin, TestEvent event) throws Throwable {

        }
    }

    static class TestEvent extends Event {

    }

    @Test
    @Order(1)
    public void test_add() {
        Assertions.assertTrue(CON.add(A));
        Assertions.assertTrue(CON.add(B));
        Assertions.assertTrue(CON.add(C));
        Assertions.assertTrue(CON.add(D));
    }

    @Test
    @Order(2)
    public void test_remove() {
        Assertions.assertTrue(CON.remove(B));
        Assertions.assertTrue(CON.remove(D));
    }

    @Test
    @Order(3)
    public void test_contains() {
        Assertions.assertTrue(CON.contains(A));
        Assertions.assertTrue(CON.contains(C));
        Assertions.assertFalse(CON.contains(B));
        Assertions.assertFalse(CON.contains(D));
    }

    @Test
    @Order(4)
    public void test_size() {
        Assertions.assertEquals(2, CON.size());
    }

    @Test
    @Order(5)
    public void test_add2() {
        Assertions.assertTrue(CON.add(B));
        Assertions.assertTrue(CON.add(D));
    }

    @Test
    @Order(6)
    public void test_contains2() {
        Assertions.assertTrue(CON.contains(A));
        Assertions.assertTrue(CON.contains(B));
        Assertions.assertTrue(CON.contains(C));
        Assertions.assertTrue(CON.contains(D));
    }

    @Test
    @Order(7)
    public void test_removeAll1() {
        Assertions.assertTrue(CON.removeAll(Event.class));
        Assertions.assertFalse(CON.contains(A));
        Assertions.assertFalse(CON.contains(B));
        Assertions.assertFalse(CON.contains(C));
        Assertions.assertFalse(CON.contains(D));
        Assertions.assertEquals(0, CON.size());
        Assertions.assertTrue(CON.isEmpty());
        test_add();
    }

    @Test
    @Order(8)
    public void test_removeAll2() {
        Assertions.assertTrue(CON.removeAll(CON.getGroups()));
        Assertions.assertFalse(CON.contains(A));
        Assertions.assertFalse(CON.contains(B));
        Assertions.assertFalse(CON.contains(C));
        Assertions.assertFalse(CON.contains(D));
        Assertions.assertEquals(0, CON.size());
        Assertions.assertTrue(CON.isEmpty());
        test_add();
    }

    @Test
    @Order(9)
    public void test_removeAll3() {
        Assertions.assertTrue(CON.removeAll(Arrays.asList(A, C)));
        Assertions.assertFalse(CON.contains(A));
        Assertions.assertTrue(CON.contains(B));
        Assertions.assertFalse(CON.contains(C));
        Assertions.assertTrue(CON.contains(D));
        Assertions.assertEquals(2, CON.size());
        Assertions.assertFalse(CON.isEmpty());
    }

    @Test
    @Order(10)
    public void test_clear() {
        CON.clear();
        Assertions.assertTrue(CON.isEmpty());
        Assertions.assertEquals(0, CON.size());
    }

    @Test
    @Order(11)
    public void test_containsAll() {
        test_add();
        Assertions.assertTrue(CON.containsAll(Arrays.asList(A, B, C, D)));
        Assertions.assertFalse(CON.isEmpty());
        Assertions.assertEquals(4, CON.size());
    }

    @Test
    @Order(12)
    public void test_retainAll() {
        Assertions.assertTrue(CON.retainAll(Arrays.asList(B, D)));
        Assertions.assertTrue(CON.contains(B));
        Assertions.assertTrue(CON.contains(D));
        Assertions.assertFalse(CON.contains(A));
        Assertions.assertFalse(CON.contains(C));
        Assertions.assertEquals(2, CON.size());
    }

    @Test
    @Order(13)
    public void test_removeAll4() {
        test_clear();
        test_add();
        Assertions.assertTrue(CON.add(E));
        Assertions.assertTrue(CON.contains(E));
        Assertions.assertEquals(5, CON.size());
        Assertions.assertTrue(CON.removeAll(Event.class));
        Assertions.assertEquals(1, CON.size());
        Assertions.assertFalse(CON.contains(A));
        Assertions.assertFalse(CON.contains(B));
        Assertions.assertFalse(CON.contains(C));
        Assertions.assertFalse(CON.contains(D));
        Assertions.assertTrue(CON.contains(E));
    }

    @Test
    @Order(14)
    public void test_getGroup() {
        test_clear();
        test_add();
        Assertions.assertTrue(CON.add(E));
        var coll1 = CON.getGroup(TestEvent.class);
        Assertions.assertFalse(coll1.isEmpty());
        Assertions.assertEquals(1, coll1.size());
        Assertions.assertTrue(coll1.contains(E));
        Assertions.assertFalse(coll1.containsAll(Arrays.asList(A, B, C, D)));
        var coll2 = CON.getGroup(Event.class);
        Assertions.assertFalse(coll2.isEmpty());
        Assertions.assertEquals(4, coll2.size());
        Assertions.assertTrue(coll2.containsAll(Arrays.asList(A, B, C, D)));
        Assertions.assertFalse(coll2.contains(E));
    }

    @Test
    @Order(15)
    public void test_getDerivedOf() {
        test_clear();
        test_add();
        Assertions.assertTrue(CON.add(E));
        var derived1 = CON.getDerivedOf(TestEvent.class, EventSubscriber.class);
        Assertions.assertFalse(derived1.isEmpty());
        Assertions.assertEquals(1, derived1.size());
        Assertions.assertTrue(derived1.contains(E));
        Assertions.assertFalse(derived1.containsAll(Arrays.asList(A, B, C, D)));

        var derived2 = CON.getDerivedOf(Event.class, EventSubscriber.class);
        Assertions.assertFalse(derived2.isEmpty());
        Assertions.assertEquals(5, derived2.size());
        Assertions.assertTrue(derived2.containsAll(Arrays.asList(A, B, C, D, E)));

        Assertions.assertTrue(CON.add(F));
        Assertions.assertTrue(CON.contains(F));

        var derived3 = CON.getDerivedOf(Event.class, TestSubscriber.class);
        Assertions.assertTrue(derived3.contains(F));
        Assertions.assertFalse(derived3.isEmpty());
        Assertions.assertEquals(1, derived3.size());
        Assertions.assertFalse(derived3.containsAll(Arrays.asList(A, B, C, D, E)));
    }

    @Test
    @Order(16)
    public void test_addAll() {
        test_clear();
        CON.addAll(Arrays.asList(A, B, C, D, E, F));
        Assertions.assertTrue(CON.contains(A));
        Assertions.assertTrue(CON.contains(B));
        Assertions.assertTrue(CON.contains(C));
        Assertions.assertTrue(CON.contains(D));
        Assertions.assertTrue(CON.containsAll(Arrays.asList(E, F)));
        Assertions.assertEquals(6, CON.size());
    }

}
