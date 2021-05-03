package jpabook.jpashop.api;

import jpabook.jpashop.domain.Order;
import jpabook.jpashop.domain.OrderItem;
import jpabook.jpashop.domain.OrderSearch;
import jpabook.jpashop.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class OrderApiController {

    private final OrderRepository orderRepository;

    /**
     * v1 엔티티 직접 노출
     * @JsonIgnore, Hibernate5Module를 통해 무한순환참조 방지
     * OrderItem 컬렉션 내 item의 name까지 모두 Lazy초기화 수행하여 출력
     */
    @GetMapping("/api/v1/orders")
    public List<Order> orderv1() {
        List<Order> all = orderRepository.findAllByString(new OrderSearch());
        for (Order order : all) { // Lazy 초기화
            order.getMember().getName();
            order.getDelivery().getAddress();

            List<OrderItem> orderItems = order.getOrderItems();
            orderItems.stream().forEach(oi->oi.getItem().getName()); // 컬렉션 Lazy초기화
        }
        return all;
    }


}
