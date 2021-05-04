package jpabook.jpashop.api;

import jpabook.jpashop.domain.*;
import jpabook.jpashop.repository.OrderRepository;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

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
    public List<Order> orderV1() {
        List<Order> all = orderRepository.findAllByString(new OrderSearch());
        for (Order order : all) { // Lazy 초기화
            order.getMember().getName();
            order.getDelivery().getAddress();

            List<OrderItem> orderItems = order.getOrderItems();
            orderItems.stream().forEach(oi->oi.getItem().getName()); // 컬렉션 Lazy초기화
        }
        return all;
    }

    /**
     * v2 DTO 변환
     * 값 타입 등을 제외한 엔티티는 노출 되어선 안 된다.
     * 엔티티 내에 엔티티가 있는 경우 안에 있는 엔티티까지 DTO로 변환할 것.
     */
    @GetMapping("/api/v2/orders")
    public DtoWrapper<List<OrderDto>> orderV2() {
        List<Order> result = orderRepository.findAllByString(new OrderSearch());
        List<OrderDto> collect = result.stream()
                .map(o -> new OrderDto(o))
                .collect(Collectors.toList());
        return new DtoWrapper(collect,collect.size());
    }

    /**
     * v3 패치 조인
     * 컬렉션 조회(일대다 엔티티)시 패치 조인의 최적화 (distinct)
     *
     * (일대다)컬렉션 패치 조인시 페이징을 사용하지 말자.
     */
    @GetMapping("/api/v3/orders")
    public List<OrderDto> orderV3() {
        List<Order> result = orderRepository.findAllWithItem();
        List<OrderDto> collect = result.stream()
                .map(o -> new OrderDto(o))
                .collect(Collectors.toList());
        return collect;
    }



    @Getter
    static class OrderDto{

        private Long orderId;
        private String name;
        private LocalDateTime orderDate;
        private OrderStatus orderStatus;
        private Address address;
        // OrderItem 엔티티에도 종속되지 않도록 DTO를 사용
        // 엔티티의 변경이 API에 영향을 끼치지 않고, 사용자에게 공개하고 싶은 데이터만 공개 가능 등 여러 이점
        private List<OrderItemDto> orderItem;

        public OrderDto(Order o) {
            orderId = o.getId();
            name = o.getMember().getName();
            orderDate = o.getOrderDate();
            orderStatus = o.getStatus();
            address = o.getDelivery().getAddress();
            orderItem = o.getOrderItems().stream()
                    .map(oi -> new OrderItemDto(oi))
                    .collect(Collectors.toList());
        }
    }

    @Getter
    static class OrderItemDto{
        private String itemName;
        private int orderPrice;
        private int count;

        public OrderItemDto(OrderItem oi){
            itemName = oi.getItem().getName();
            orderPrice = oi.getOrderPrice();
            count = oi.getCount();
        }
    }

    @Getter
    static class DtoWrapper<T>{
        T data;
        int count;

        public DtoWrapper(T data, int count) {
            this.data = data;
            this.count = count;
        }
    }

}
