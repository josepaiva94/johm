package redis.clients.johm;

import java.util.List;
import java.util.Set;

import org.junit.Test;

import redis.clients.jedis.exceptions.JedisDataException;
import redis.clients.johm.issues.Issue9Item;
import redis.clients.johm.models.Book;
import redis.clients.johm.models.Country;
import redis.clients.johm.models.FaultyModel;
import redis.clients.johm.models.Item;
import redis.clients.johm.models.User;

public class BasicPersistenceTest extends JOhmTestBase {
    @Test
    public void save() {
        User user = new User();
        user.setName("foo");
        user.setRoom("vroom");
        user = JOhm.save(user);

        assertNotNull(user);
        User savedUser = JOhm.get(User.class, user.getId());
        assertEquals(user.getName(), savedUser.getName());
        assertNull(savedUser.getRoom());
        assertEquals(user.getId(), savedUser.getId());
        assertEquals(user.getAge(), savedUser.getAge());
    }

    @Test
    public void saveWithArray() {
        Item item0 = new Item();
        item0.setName("Foo0");
        JOhm.save(item0);

        Item item1 = new Item();
        item1.setName("Foo1");
        JOhm.save(item1);

        Item item2 = new Item();
        item2.setName("Foo2");
        JOhm.save(item2);

        User user = new User();
        user.setName("foo");
        user.setRoom("vroom");
        user.setThreeLatestPurchases(new Item[] { item0, item1, item2 });
        user = JOhm.save(user);

        assertNotNull(user);
        User savedUser = JOhm.get(User.class, user.getId());
        assertEquals(user.getName(), savedUser.getName());
        assertNull(savedUser.getRoom());
        assertEquals(user.getId(), savedUser.getId());
        assertEquals(user.getAge(), savedUser.getAge());

        Item[] saved = savedUser.getThreeLatestPurchases();
        assertEquals(3, saved.length);
        assertEquals(item0.getId(), saved[0].getId());
        assertEquals(item0.getName(), saved[0].getName());
        assertEquals(item1.getId(), saved[1].getId());
        assertEquals(item1.getName(), saved[1].getName());
        assertEquals(item2.getId(), saved[2].getId());
        assertEquals(item2.getName(), saved[2].getName());

        assertTrue(JOhm.delete(User.class, savedUser.getId(), true, true));
        assertTrue(JOhm.delete(Item.class, item0.getId()));
        assertTrue(JOhm.delete(Item.class, item1.getId()));
        assertTrue(JOhm.delete(Item.class, item2.getId()));
    }

    @Test
    public void saveWithOtherValueTypes() {
        User user1 = new User();
        user1.setName("foo");
        user1.setRoom("vroom");
        user1.setAge(99);
        user1.setSalary(9999.99f);
        user1.setInitial('f');
        user1 = JOhm.save(user1);

        User user2 = new User();
        user2.setName("foo2");
        user2.setRoom("vroom2");
        user2.setAge(9);
        user2.setInitial('f');
        user2 = JOhm.save(user2);

        User user3 = new User();
        user3.setName("foo3");
        user3.setRoom("vroom3");
        user3.setAge(19);
        user3.setSalary(9999.9f);
        user3.setInitial('f');
        user3 = JOhm.save(user3);

        assertNotNull(user1);
        // assertEquals(1, user1.getId());
        User savedUser1 = JOhm.get(User.class, user1.getId());
        assertEquals(user1.getName(), savedUser1.getName());
        assertNull(savedUser1.getRoom());
        assertEquals(user1.getId(), savedUser1.getId());
        assertEquals(user1.getAge(), savedUser1.getAge());
        assertEquals(user1.getInitial(), savedUser1.getInitial());
        assertEquals(user1.getSalary(), savedUser1.getSalary(), 0D);

        assertNotNull(user2);
        // assertEquals(2, user2.getId());
        User savedUser2 = JOhm.get(User.class, user2.getId());
        assertEquals(user2.getName(), savedUser2.getName());
        assertNull(savedUser2.getRoom());
        assertEquals(user2.getId(), savedUser2.getId());
        assertEquals(user2.getInitial(), savedUser2.getInitial());
        assertEquals(user2.getAge(), savedUser2.getAge());

        assertNotNull(user3);
        // assertEquals(3, user3.getId());
        User savedUser3 = JOhm.get(User.class, user3.getId());
        assertEquals(user3.getName(), savedUser3.getName());
        assertNull(savedUser3.getRoom());
        assertEquals(user3.getId(), savedUser3.getId());
        assertEquals(user3.getAge(), savedUser3.getAge());
        assertEquals(user3.getInitial(), savedUser3.getInitial());
        assertEquals(user3.getSalary(), savedUser3.getSalary(), 0D);

        // cleanup now
        assertTrue(JOhm.delete(User.class, user1.getId()));
        assertNull(JOhm.get(User.class, user1.getId()));
        assertTrue(JOhm.delete(User.class, user2.getId()));
        assertNull(JOhm.get(User.class, user2.getId()));
        assertTrue(JOhm.delete(User.class, user3.getId()));
        assertNull(JOhm.get(User.class, user3.getId()));
    }

    @Test
    public void delete() {
        User user = new User();
        JOhm.save(user);
        Long id = user.getId();

        assertNotNull(JOhm.get(User.class, id));
        assertTrue(JOhm.delete(User.class, id));
        assertNull(JOhm.get(User.class, id));

        user = new User();
        JOhm.save(user);
        id = user.getId();

        assertNotNull(JOhm.get(User.class, id));
        assertTrue(JOhm.delete(User.class, id));
        assertNull(JOhm.get(User.class, id));
    }

    @Test
    public void shouldNotPersistFieldsWithoutAttributeAnnotation() {
        User user = new User();
        user.setName("foo");
        user.setRoom("3A");
        JOhm.save(user);

        User savedUser = JOhm.get(User.class, user.getId());
        assertEquals(user.getName(), savedUser.getName());
        assertNull(savedUser.getRoom());
    }

    @Test(expected = MissingIdException.class)
    public void shouldFailWhenReferenceWasNotSaved() {
        User user = new User();
        user.setName("bar");
        user.setCountry(new Country());
        JOhm.save(user);
    }

    @Test(expected = JOhmException.class)
    public void shouldNotPersistWithoutModel() {
        Nest<String> dummyNest = new Nest<String>();
        JOhm.save(dummyNest);
    }

    @Test(expected = JOhmException.class)
    public void shouldNotPersistModelWithOtherJOhmIdAnnotations() {
        FaultyModel badModel = new FaultyModel();
        badModel.setType("horribleId");
        JOhm.save(badModel);
    }

    @Test
    public void shouldHandleReferences() {
        User user = new User();
        user.setName("foo");
        user.setRoom("3A");
        JOhm.save(user);

        User savedUser = JOhm.get(User.class, user.getId());
        assertNull(savedUser.getCountry());

        Country somewhere = new Country();
        somewhere.setName("Somewhere");
        JOhm.save(somewhere);

        user = new User();
        user.setName("bar");
        user.setCountry(somewhere);
        JOhm.save(user);

        savedUser = JOhm.get(User.class, user.getId());
        assertNotNull(savedUser.getCountry());
        assertEquals(somewhere.getId(), savedUser.getCountry().getId());
        assertEquals(somewhere.getName(), savedUser.getCountry().getName());
    }

    @Test
    public void getAll() {
        User user = new User();
        user.setName("foo");
        JOhm.save(user);
        user = new User();
        user.setName("foo1");
        JOhm.save(user);

        Set<User> users = JOhm.getAll(User.class);
        assertEquals(2, users.size());
    }
    
    @Test(expected = JOhmException.class)
    public void getAllNotSupported() {
        Book book = new Book();
        book.setName("title");
        JOhm.save(book);
        book = new Book();
        book.setName("another title");
        JOhm.save(book);

        JOhm.getAll(Book.class);
    }
    
    @Test
    public void shouldExpireModel() throws InterruptedException {
        Book book = new Book();
        book.setName("expritation title");
        JOhm.save(book);
        JOhm.expire(book, 1);
        Book savedBook = JOhm.get(Book.class, book.getId());
        assertEquals(book.getName(), savedBook.getName());

        // wait of expire
        Thread.sleep(1500L);
        savedBook = JOhm.get(Book.class, book.getId());
        assertNull(savedBook);
    }
    
    @Test
    public void shouldNotExpireIndex() throws InterruptedException {
        
        User user = new User();
        user.setName("i_will_not_be_expired");
        user = JOhm.save(user);
        JOhm.expire(user, 1);
        User savedUser = JOhm.get(User.class, user.getId());
        assertEquals(user.getName(), savedUser.getName());

        // wait of expire
        Thread.sleep(1500L);
        savedUser = JOhm.get(User.class, user.getId());
        assertNull(savedUser);
        
        Nest userNest = new Nest("User");
        userNest.setPool(jedisPool);
        assertEquals("User", userNest.key());
        assertFalse(userNest.cat(user.getId()).exists());
        assertTrue(userNest.cat("name").cat("i_will_not_be_expired").exists());
    }
    
    @Test
    public void shouldExpireIndex() throws InterruptedException {
        
        User user = new User();
        user.setName("i_will_be_expired");
        user = JOhm.save(user);
        JOhm.expire(user, 1, true); // call which will expire model and indexes
        User savedUser = JOhm.get(User.class, user.getId());
        assertEquals(user.getName(), savedUser.getName());

        // wait of expire
        Thread.sleep(1500L);
        savedUser = JOhm.get(User.class, user.getId());
        assertNull(savedUser);
        
        Nest userNest = new Nest("User");
        userNest.setPool(jedisPool);
        assertEquals("User", userNest.key());
        assertFalse(userNest.cat(user.getId()).exists());
        assertFalse(userNest.cat("name").cat("i_will_be_expired").exists());
    }
    
    @Test
    public void shouldDeleteIndexesAndNotLeaveThemOrphaned() {
        User user = new User();
        user.setName("i_will_be_deleted");
        user = JOhm.save(user);

        List<Object> users = JOhm.find(User.class, "name", "i_will_be_deleted");
        assertEquals(1, users.size());
        
        JOhm.delete(User.class, user.getId());
        
        Nest usersNest = new Nest("User");
        usersNest.setPool(jedisPool);
        assertEquals("User", usersNest.key());
        assertFalse(usersNest.cat(user.getId()).exists());
        assertFalse(usersNest.cat("name").cat("i_will_be_deleted").exists());
        
        users = JOhm.find(User.class, "name", "i_will_be_deleted");
        assertEquals(0, users.size());
    }
    
    @Test
    public void shouldNotDeleteIndexesWhenThereAreMultipleEntries() {
        User user1 = new User();
        String indexName = "IndexDeleteTest";
        user1.setName(indexName);
        user1 = JOhm.save(user1);
        
        User user2 = new User();
        user2.setName(indexName);
        user2 = JOhm.save(user2);

        List<Object> users = JOhm.find(User.class, "name", indexName);
        assertEquals(2, users.size());
        
        JOhm.delete(User.class, user1.getId());
        
        Nest usersNest = new Nest("User");
        usersNest.setPool(jedisPool);

        // first user
        assertEquals("User", usersNest.key());
        assertFalse(usersNest.cat(user1.getId()).exists());
        assertTrue(usersNest.cat("name").cat(indexName).exists());
        
        // second user
        assertEquals("User", usersNest.key());
        assertTrue(usersNest.cat(user2.getId()).exists());
        assertTrue(usersNest.cat("name").cat(indexName).exists());
        
        users = JOhm.find(User.class, "name", indexName);
        assertEquals(1, users.size());
        
        JOhm.delete(User.class, user2.getId());
        
        assertFalse("Index: IndexDeleteTest should not exist", usersNest.cat("name").cat(indexName).exists());
        users = JOhm.find(User.class, "name", indexName);
        assertEquals(0, users.size());
    }
    
    @Test
    public void whenSavingExistingModelIndexesShouldNotBeLeftOrphaned() {
        User user = new User();
        String indexName = "i_will_be_then_deleted_in_the_end";
        user.setName(indexName);
        user.setRoom("A");
        user = JOhm.save(user);

        Nest usersNest = new Nest("User");
        usersNest.setPool(jedisPool);
        assertEquals("User", usersNest.key());
        assertTrue(usersNest.cat(user.getId()).exists());
        assertTrue(usersNest.cat("name").cat(indexName).exists());
        
        List<Object> users = JOhm.find(User.class, "name", indexName);
        assertEquals(1, users.size());
        
        String newIndexName = "new_index";
        user.setName(newIndexName);
        user.setRoom("B");
        JOhm.save(user, true);
        
        assertTrue(usersNest.cat(user.getId()).exists());
        assertTrue(usersNest.cat("name").cat(newIndexName).exists());
        assertFalse(usersNest.cat("name").cat(indexName).exists());
        
        JOhm.delete(User.class, user.getId());
        
        assertFalse(usersNest.cat(user.getId()).exists());
        assertFalse(usersNest.cat("name").cat(newIndexName).exists());
        assertFalse(usersNest.cat("name").cat(indexName).exists());
        
        users = JOhm.find(User.class, "name", indexName);
        assertEquals(0, users.size());
        
        users = JOhm.find(User.class, "name", newIndexName);
        assertEquals(0, users.size());
    }

    @Test
    public void testSelectDb() {
        JOhm.selectDb(0);
        User user = new User();
        user.setName("foo");
        user = JOhm.save(user);
        JOhm.selectDb(7);
        assertNull(JOhm.get(User.class, user.getId()));
        JOhm.selectDb(0); // by default
        assertNotNull(JOhm.get(User.class, user.getId()));
    }

    @Test
    public void testFlushDb() {
        User user = new User();
        user.setName("foo");
        JOhm.selectDb(7);
        user = JOhm.save(user);

        JOhm.selectDb(1);
        User user2 = new User();
        user2.setName("bar");
        user2 = JOhm.save(user2);

        JOhm.flushDb();
        assertNull(JOhm.get(User.class, user2.getId()));

        JOhm.selectDb(7);
        assertNotNull(JOhm.get(User.class, user.getId()));
        JOhm.flushDb();
        assertNull(JOhm.get(User.class, user.getId()));
    }

    @Test(expected = JedisDataException.class)
    public void testIssue9() {
        Issue9Item item = new Issue9Item(1337l);
        JOhm.save(item);
    }
}

