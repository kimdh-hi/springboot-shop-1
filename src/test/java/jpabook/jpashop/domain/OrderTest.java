package jpabook.jpashop.domain;

import jpabook.jpashop.domain.item.Book;
import jpabook.jpashop.exception.NotEnoughSotckException;
import jpabook.jpashop.repository.ItemRepository;
import jpabook.jpashop.repository.OrderRepository;
import jpabook.jpashop.service.OrderService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@Transactional
class OrderTest {

    @PersistenceContext
    EntityManager em;


    @Autowired OrderService orderService;
    @Autowired OrderRepository orderRepository;
    @Autowired ItemRepository itemRepository;

    @Test
    @DisplayName("주문 테스트")
    public void 주문테스트() throws Exception {
        //given
        Member member = createMember();
        Book book = createBook("book1", 10000, 100);

        //when
        int order_cnt = 2;
        Long orderId = orderService.order(member.getId(), book.getId(), order_cnt);
        Order findOrder = orderRepository.findOne(orderId);

        //then
        assertEquals(OrderStatus.ORDER, findOrder.getStatus(),"상품 주문시 상태는 ORDER");
        assertEquals(findOrder.getOrderItems().size(), 1, "주문 상품 종류수 검증");
        assertEquals(10000 * order_cnt, findOrder.getTotalPrice(),"주문 가격 검증");
        assertEquals(book.getStockQuantity(), 100-order_cnt, "주문 수량만큼 재고 감소 검증");
    }

    @Test
    public void 주문_수량_초과() throws Exception {
        //given
        Member member = createMember();
        Book book = createBook("book1", 10000, 5);

        //when

        //then
        Assertions.assertThrows(NotEnoughSotckException.class, () -> {
            int order_cnt = 6;
            orderService.order(member.getId(), book.getId(), order_cnt);
        });
    }

    @Test
    public void 주문취소() throws Exception {
        //given
        Member member = createMember();
        Book book = createBook("book1", 10000, 5);

        //when
        int order_cnt = 2;
        Long orderId = orderService.order(member.getId(), book.getId(), order_cnt);
        Order findOrder = orderRepository.findOne(orderId);
        findOrder.cancel();
        int quantity = itemRepository.findOne(book.getId()).getStockQuantity();
        //then
        Assertions.assertEquals(quantity,5);
    }

    private Member createMember() {
        Member member = new Member();
        member.setName("kim");
        member.setAddress(new Address("aaa","bbb","ccc"));
        em.persist(member);
        return member;
    }

    private Book createBook(String name, int price, int stockQuantity) {
        Book book = new Book();
        book.setAuthor(name);
        book.setPrice(price);
        book.setStockQuantity(stockQuantity);
        em.persist(book);
        return book;
    }
}