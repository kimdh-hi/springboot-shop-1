package jpabook.jpashop.service;

import jpabook.jpashop.domain.*;
import jpabook.jpashop.domain.item.Item;
import jpabook.jpashop.repository.ItemRepository;
import jpabook.jpashop.repository.MemberRepository;
import jpabook.jpashop.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final MemberRepository memberRepository;
    private final ItemRepository itemRepository;

    /* 주문 */
    @Transactional
    public Long order(Long memberId, Long itemId, int count) {
        Member findMember = memberRepository.findOne(memberId);
        Item findItem = itemRepository.findOne(itemId);

        // 배송정보
        Delivery delivery = new Delivery();
        delivery.setAddress(findMember.getAddress());
        delivery.setStatus(DeliveryStatus.READY);

        // 주문상품정보
        OrderItem orderItem = OrderItem.createOrderItem(findItem, findItem.getPrice(), count);

        // 주문정보
        Order order = Order.createOrder(findMember,delivery,orderItem);

        // 저장
        orderRepository.save(order);

        return order.getId();
    }

    // 주문취소
    @Transactional
    public void cancelOrder(Long orderId) {
        Order findOrder = orderRepository.findOne(orderId);
        findOrder.cancel();
    }
}
