package jpabook.jpashop.repository.order.query;

import jpabook.jpashop.domain.OrderItem;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class OrderQueryRepository {

    private final EntityManager em;

    /*
        (1) 컬렉션 부분을 제외하고는 단순하게 DTO로 직접 조회
        (2) 컬렉션 부분은 (1)의 결과에 루프를 돌며 직접 채워줌

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

    private List<OrderQueryDto> findOrders() {
        return em.createQuery(
                "select new jpabook.jpashop.repository.order.query.OrderQueryDto(o.id, m.name, o.orderDate, o.status, d.address) " +
                        "from Order o " +
                        "join o.member m " +
                        "join o.delivery d", OrderQueryDto.class).getResultList();
    }

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
