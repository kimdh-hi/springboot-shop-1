package jpabook.jpashop;

import jpabook.jpashop.domain.*;
import jpabook.jpashop.domain.item.Book;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import javax.persistence.EntityManager;


/**
 * 더미 데이터 삽입
 * userA
 *      JPA1 BOOK
 *      JPA2 BOOK
 * userB
 *      SPRING1 BOOK
 *      SPRING2 BOOK
 */
//@Component
@RequiredArgsConstructor
public class dbInit {

    private final initService initService;

    @PostConstruct
    public void init() {
        initService.init1();
        initService.init2();
    }

    @Component
    @RequiredArgsConstructor
    @Transactional
    static class initService {

        private final EntityManager em;

        public void init1() {
            Member memberA = createMember("userA", new Address("서울", "서울로", "123-123"));
            em.persist(memberA);

            Book book1 = createBook("JPA1", 10000, 100);
            Book book2 = createBook("JPA2", 20000, 100);
            em.persist(book1);
            em.persist(book2);

            Delivery delivery = createDelivery(memberA);

            OrderItem orderItem1 = OrderItem.createOrderItem(book1, book1.getPrice(), 3);
            OrderItem orderItem2 = OrderItem.createOrderItem(book2, book2.getPrice(), 5);
            Order order = Order.createOrder(memberA, delivery, orderItem1, orderItem2);
            em.persist(order);
        }

        public void init2() {
            Member memberB = createMember("userB", new Address("인천", "인천로", "123-5234"));
            em.persist(memberB);

            Book book1 = createBook("SPRING1", 10000, 100);
            Book book2 = createBook("SPRING1", 20000, 100);
            em.persist(book1);
            em.persist(book2);

            Delivery delivery = createDelivery(memberB);

            OrderItem orderItem1 = OrderItem.createOrderItem(book1, book1.getPrice(), 1);
            OrderItem orderItem2 = OrderItem.createOrderItem(book2, book2.getPrice(), 2);
            Order order = Order.createOrder(memberB, delivery, orderItem1, orderItem2);
            em.persist(order);
        }

        private Member createMember(String name, Address address) {
            Member member = new Member();
            member.setName(name);
            member.setAddress(address);
            return member;
        }

        private Book createBook(String name, int price, int stock) {
            Book book = new Book();
            book.setName(name);
            book.setPrice(price);
            book.setStockQuantity(stock);
            return book;
        }

        private Delivery createDelivery(Member member){
            Delivery delivery = new Delivery();
            delivery.setAddress(member.getAddress());
            return delivery;
        }
    }
}
