package jpabook.jpashop.repository.order.query;

import jpabook.jpashop.domain.OrderItem;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 패치 조인보다 select절을 최적화함으로 어느정도 성능 향상을 기대할 수 있지만 코드의 양과 재사용성 측면에서 Trade off 발생
 */
@Repository
@RequiredArgsConstructor
public class OrderQueryRepository {

    private final EntityManager em;

    /*
        (1). 컬렉션 부분을 제외하고는 단순하게 DTO로 직접 조회
        (2). 컬렉션 부분은 (1)의 결과에 루프를 돌며 직접 채워줌

        N+1문제가 발생함
        ToOne인 것은 모두 join하여 가져옴 (ToOne은 조인해도 로우가 증가되지 않기 때문에 상관 X)
        ToMany인 컬렉션은 따로 조회해야 하기 때문에 N번의 추가 조회가 수행됨
     */
    public List<OrderQueryDto> findOrderQueryDtos() {
        List<OrderQueryDto> result = findOrders(); // 컬렉션(OrderItems)는 들고 오지 않은 상태

        result.forEach(o -> { // 컬렉션 부분을 직접 채워 넣어야 함
           List<OrderItemQueryDto> orderItems = findOrderItems(o.getOrderId());
           o.setOrderItems(orderItems);
        });
        return result;
    }

    /*
        ToOne관계를 먼저 조회회하여 얻은 결과에서 식별자를 리스트화하여 ToMany관계 조회시 in절의 파라미터로 사용
            => 2번의 쿼리 (ToOne 한 번, ToMany 한 번)
        후에 Map을 통해 값을 매칭해줌으로 성능향상 (메모리 상에서 매칭)
    */
    public List<OrderQueryDto> findAllByDto_Optimization() {

        List<OrderQueryDto> result = findOrders(); // 컬렉션이 아닌 엔티티 조회

        List<Long> orderIds = result.stream() // in절에서 사용하기 위해 id값을 리스트화
                .map(o -> o.getOrderId())
                .collect(Collectors.toList());

        // 일단 in절을 사용해서 모두 가져오고 컬렉션의 매칭은 메모리에서 수행하도록
        List<OrderItemQueryDto> orderItems = em.createQuery(
                "select new jpabook.jpashop.repository.order.query.OrderItemQueryDto(oi.order.id, i.name, oi.orderPrice, oi.count)" +
                        " from OrderItem oi" +
                        " join oi.item i" +
                        " where oi.order.id in :orderIds", OrderItemQueryDto.class) // in절 사용
                .setParameter("orderIds", orderIds)
                .getResultList();

        // Order의 ID값을 키 값으로, 컬렉션을 값으로 하는 Map 생성
        // 메모리에서 조회된 값을 매칭
        Map<Long, List<OrderItemQueryDto>> orderItemMap = orderItems.stream()
                .collect(Collectors.groupingBy(orderItemQueryDto -> orderItemQueryDto.getOrderId()));

        // 컬렉션 채워넣기
        result.forEach(o -> o.setOrderItems(orderItemMap.get(o.getOrderId())));

        return result;
    }

    public List<OrderFlatDto> findAllByDto_Flat() {

        return em.createQuery(
                "select new jpabook.jpashop.repository.order.query.OrderFlatDto(o.id, m.name, o.orderDate, o.status, d.address, i.name, oi.orderPrice, oi.count) " +
                        " from Order o" +
                        " join o.member m" +
                        " join o.delivery d" +
                        " join o.orderItems oi" +
                        " join oi.item i", OrderFlatDto.class)
                .getResultList();
    }

    // 컬렉션이 아닌 엔티티 조회 ToOne관계
    private List<OrderQueryDto> findOrders() {
        return em.createQuery(
                "select new jpabook.jpashop.repository.order.query.OrderQueryDto(o.id, m.name, o.orderDate, o.status, d.address) " +
                        "from Order o " +
                        "join o.member m " +
                        "join o.delivery d", OrderQueryDto.class).getResultList();
    }

    // 컬렉션 엔티티 조회 ToMany관계
    private List<OrderItemQueryDto> findOrderItems(Long orderId) {
        return em.createQuery(
                "select new jpabook.jpashop.repository.order.query.OrderItemQueryDto(oi.order.id, i.name, oi.orderPrice, oi.count)" +
                        " from OrderItem oi" +
                        " join oi.item i" +
                        " where oi.order.id = :orderId", OrderItemQueryDto.class)
                .setParameter("orderId", orderId)
                .getResultList();
    }


}
