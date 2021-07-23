package jpabook.jpashop.api;

import jpabook.jpashop.domain.*;
import jpabook.jpashop.repository.OrderRepository;
import jpabook.jpashop.repository.order.query.OrderFlatDto;
import jpabook.jpashop.repository.order.query.OrderQueryDto;
import jpabook.jpashop.repository.order.query.OrderQueryRepository;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

import static java.util.stream.Collectors.toList;

@RestController
@RequiredArgsConstructor
public class OrderApiController {

    private final OrderRepository orderRepository;
    private final OrderQueryRepository orderQueryRepository;

    /**
     * v1 엔티티 직접 노출
     * @JsonIgnore, Hibernate5Module를 통해 무한순환참조 방지
     * OrderItem 컬렉션 내 item의 name까지 모두 Lazy초기화 수행하여 출력
     */
    @GetMapping("/api/v1/orders")
    public List<Order> orderV1() {
        List<Order> result = orderRepository.findAllByString(new OrderSearch());
        for (Order order : result) { // Lazy 초기화
            order.getMember().getName();
            order.getDelivery().getAddress();

            List<OrderItem> orderItems = order.getOrderItems();
            orderItems.stream().forEach(oi->oi.getItem().getName()); // 컬렉션 Lazy초기화
        }
        return result;
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
                .collect(toList());
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
                .collect(toList());
        return collect;
    }

    /**
     * v3.1 페이징 + 컬레션 엔티티 해결
     *
     * xToOne관계는 그냥 패치 조인 사용해도 문제 없음 --> 패치조인으로 row수가 증가되지 않기 대문에 페이징에 영향 없음
     * xToMany관계(컬렉션)는 패치 조인을 하지 않고 그냥 Lazy로딩을 수행 (프로시 초기화를 통해)
     * hibernate.default_batch_fetch_size를 통해 컬렉션 엔티티 Lazy로딩을 최적화 (in쿼리를 통해 한번에 가져옴)
     *
     * 쿼리의 양이 늘어난다고 반드시 성능이 나빠지는 것은 아님
     * 쿼리의 양은 늘어나지만 보다 정규화된 데이터를 만들어 양을 줄일 수 있음
     */
    @GetMapping("/api/v3.1/orders")
    public List<OrderDto> orderV3_page(
            @RequestParam(value="offset", defaultValue = "1") int offset,
            @RequestParam(value="limit", defaultValue = "100") int limit) {
        List<Order> result = orderRepository.findAllWithMemberDelivery(offset, limit);
        List<OrderDto> collect = result.stream()
                .map(o -> new OrderDto(o))
                .collect(toList());
        return collect;
    }

    /**
     * v4 JPA에서 DTO 직접 조회
     * 컬렉션까지 모두 DTO로 직접 조회
     */
    @GetMapping("/api/v4/orders")
    public List<OrderQueryDto> orderV4() {
        return orderQueryRepository.findOrderQueryDtos();
    }

    /**
     * v5 JPA에서 DTO 직접 조회 (v4에서 N+1문제 해결)
     */
    @GetMapping("/api/v5/orders")
    public List<OrderQueryDto> orderV5() {
        return orderQueryRepository.findAllByDto_Optimization();
    }


    /**
     * v6 JPA에서 DTO 직접 조회 (플랫 데이터 최적화)
     * ToOne, ToMany 모두 한 개 DTO를 사용하여 조회
     * 쿼리가 한 번만 수행되는 장점, but 컬렉션 join시 row가 증가(중복), 중복이 발생하기 때문에 Order기준 페이징 또한 불가
     */
    @GetMapping("/api/v6/orders")
    public List<OrderFlatDto> orderV6() {
        return orderQueryRepository.findAllByDto_Flat();

//        return flats.stream()
//                .collect(groupingBy(o -> new OrderQueryDto(o.getOrderId(), o.getName(), o.getOrderDate(), o.getOrderStatus(), o.getAddress()),
//                        mapping(o -> new OrderItemQueryDto(o.getOrderId(), o.getItemName(), o.getOrderPrice(), o.getCount()), toList())
//                )).entrySet().stream()
//                .map(e -> new OrderQueryDto(e.getKey().getOrderId(), e.getKey().getName(), e.getKey().getOrderDate(), e.getKey().getOrderStatus(), e.getKey().getAddress(), e.getValue()))
//                .collect(toList());
    }


    @Getter
    static class OrderDto{
        private Long orderId;
        private String name;
        private LocalDateTime orderDate;
        private OrderStatus orderStatus;
        private Address address;
        private List<OrderItemDto> orderItem;

        public OrderDto(Order o) {
            orderId = o.getId();
            name = o.getMember().getName();
            orderDate = o.getOrderDate();
            orderStatus = o.getStatus();
            address = o.getDelivery().getAddress();
            orderItem = o.getOrderItems().stream()
                    .map(oi -> new OrderItemDto(oi))
                    .collect(toList());
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
