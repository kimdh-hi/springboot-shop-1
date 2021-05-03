package jpabook.jpashop.api;

import jpabook.jpashop.domain.Address;
import jpabook.jpashop.domain.Order;
import jpabook.jpashop.domain.OrderSearch;
import jpabook.jpashop.domain.OrderStatus;
import jpabook.jpashop.repository.OrderRepository;
import jpabook.jpashop.repository.order.simplequery.OrderSimpleQueryDto;
import jpabook.jpashop.repository.order.simplequery.OrderSimpleQueryRepository;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
public class OrderSimpleApiController {

    private final OrderRepository orderRepository;
    private final OrderSimpleQueryRepository orderSimpleQueryRepository;

    /**
     * 엔티티를 직접 리턴하는 경우 무한순환참조 문제 발생
     * 문제 해결을 위해 xToOne관계에 있는 상태 엔티티에 JsonIgnore을 설정
     *
     * Order -> Member Lazy로딩
     * Order -> Delivery Lazy로딩
     * 따라서 프록시 객체로 초기화 되는데, Json으로 변경을 담당하는 Jackson라이브러리는 프록시객체를 Json으로 변경 방법을 모름
     *
     * Hibernate5Mdule로 해결
     * Hibernate5Module 빈 등록 후 postman으로 확인하면 Order와 Delivery에 null이 들어온 것을 확인할 수 있음.
     */
    @GetMapping("/api/v1/orders")
    public List<Order> ordersV1() {
        List<Order> orders = orderRepository.findAllByString(new OrderSearch());
        for (Order order : orders) {
            order.getMember().getName(); // Lazy 강제 초기
            order.getDelivery().getAddress(); // Lazy 강제 초기화
        }
        return orders;
    }

    /**
     * v2 DTO로 반환
     * N+1 (1+N) 문제 발생
     * 2개 Order를 조회시 멤버 N번, 배송 N번 총 5번의 쿼리가 나가게 된다.
     */
    @GetMapping("/api/v2/orders")
    public OrderDtoWrapper<List<OrderDto>> ordersV2() {
        List<Order> orders = orderRepository.findAllByString(new OrderSearch());
        List<OrderDto> collect = orders.stream()
                .map(m -> new OrderDto(m))
                .collect(Collectors.toList());
        return new OrderDtoWrapper<List<OrderDto>>(collect.size(), collect);
    }

    /**
     * v3 fetch join
     * order와 연관관계를 맺는 테이블을 모두 한방 쿼리로 조회
     * Lazy 의미 없음. 모두 조회 (N+1 문제 해결)
     * 현재 예제에서는 Order의 Member와 Delivery를 패치조인 하여 세 테이블의 모든 필드를 조인하여 한번에 조회
     *
     * 기본적으로 모든 xToOne관계는 Lazy로 설정하고, 필요한 경우에만 fetch조인으로 객체를 묶어서
     * 조회할 것.
     */
    @GetMapping("/api/v3/orders")
    public OrderDtoWrapper<List<OrderDto>> orderV3() {
        List<Order> orders = orderRepository.findAllWithMemberDelivery();

        return new OrderDtoWrapper<List<OrderDto>>
                (orders.size(), orders.stream()
                .map(m->new OrderDto(m))
                .collect(Collectors.toList()));
    }

    /**
     * v4 dto로 조회
     * 패치조인과 동일하게 N+1문제는 해결됨
     * 패치조인과 다른 점으로는 필요한 필드만을 dto로 구성하여 패치 조인에 비해 select절을 줄일 수 있음
     * repository에서 new를 통해 쿼리 작성
     *
     * but, 재사용성이 떨어짐
     */
    @GetMapping("/api/v4/orders")
    public List<OrderSimpleQueryDto> orderV4() {
        return orderSimpleQueryRepository.findOrderDtos();
    }

    @Data
    static class OrderDto {
        private Long OrderId;
        private String name;
        private LocalDateTime orderDate;
        private OrderStatus orderStatus;
        private Address address;

        private OrderDto(Order o) {
            this.OrderId = o.getId();
            this.name = o.getMember().getName(); // Lazy 초기화
            this.orderDate = o.getOrderDate();
            this.orderStatus = o.getStatus();
            this.address = o.getDelivery().getAddress(); // Lazy 초기화
            // Lazy초기화 ?
            // o.getMember()의 PK로 영속성 컨텍스트에 Member가 있는 지 찾은 후 없다면 쿼리르 날림.
        }


    }

    @AllArgsConstructor
    @Data
    static class OrderDtoWrapper<T> {
        private int orderCount;
        private T data;
    }
}
