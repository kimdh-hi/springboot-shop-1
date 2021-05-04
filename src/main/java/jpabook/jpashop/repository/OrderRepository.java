package jpabook.jpashop.repository;

import jpabook.jpashop.domain.Order;
import jpabook.jpashop.domain.OrderSearch;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.*;
import java.util.ArrayList;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class OrderRepository {

    private final EntityManager em;

    public void save(Order order) {
        em.persist(order);
    }

    public Order findOne(Long id) {
        return em.find(Order.class, id);
    }

    /**
     * JPA Criteria 동적쿼리
     * 얘도 사용 안함
     */
    public List<Order> findAllByString(OrderSearch orderSearch) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Order> cq = cb.createQuery(Order.class);
        Root<Order> o = cq.from(Order.class); // 앨리어싱
        Join<Object, Object> m = o.join("member", JoinType.INNER);

        List<Predicate> criteria = new ArrayList<>();

        if (orderSearch.getOrderStatus() != null) {
            Predicate status = cb.equal(o.get("status"), orderSearch.getOrderStatus());
            criteria.add(status);
        }

        if (StringUtils.hasText(orderSearch.getMemberName())) {
            Predicate name = cb.like(m.get("name"), "%" + orderSearch.getMemberName() + "%");
            criteria.add(name);
        }

        cq.where(cb.and(criteria.toArray(new Predicate[criteria.size()])));
        TypedQuery<Order> query = em.createQuery(cq).setMaxResults(100);
        return query.getResultList();
    }

    /**
     * fetch 조인
     * order를 조회하는 시점에 order의 member와 delivery의 LAZY설정을 무시하고 모두 가져옴
     * join fetch o.member m
     * join fetch o.delivery d
     */
    public List<Order> findAllWithMemberDelivery() {
        return em.createQuery("select o from Order o " +
                "join fetch o.member m " +
                "join fetch o.delivery d", Order.class)
                .getResultList();

    }

    /**
     * 일대다 관계 컬렉션 패치 조인
     * 일 쪽의 데이터가 다 쪽의 데이터의 수만큼 늘어나게 됨 (뻥튀기)
     * 아래 예에서 Order는 2개, OrderItem은 4개인데 그냥 패치 조인을 수행하면 Order가 4개로 늘어남
     * distinct 키워드로 해결 가능
     *
     * RDBMS에서의 distinct와 비슷하지만 조금 다름 (RDBMS의 경우 로우가 완전히 같은 행의 중복을 제거함)
     * JPA의 distinct는 id(=PK)값을 기준으로 중복을 제거해줌
     */
    public List<Order> findAllWithItem() {
        return em.createQuery("select distinct o from Order o "+
                    "join fetch o.member m "+
                    "join fetch o.delivery d "+
                    "join fetch o.orderItems oi "+
                    "join fetch oi.item i", Order.class)
                .getResultList();
    }

    public List<Order> findAllWithMemberDelivery(int offset, int limit) {
        return em.createQuery("select o from Order o " +
                "join fetch o.member m " +
                "join fetch o.delivery d", Order.class)
                .setFirstResult(offset)
                .setMaxResults(limit)
                .getResultList();
    }
}


    /**
     * jpql 문자열 동적쿼리
     * 문자열을 더해가는 것은 버그의 위험성이 너무 높음.
     * 코드의 길이도 너무 김 ==> 쓰지 않음.
     */
//    public List<Order> findAll(OrderSearch orderSearch) {
//
//        select * from order o join member m on o.member_id = m.id;
//        String jpql = "select o from Order o join o.member m";
//        boolean isFirstCondition = true;
//
//        if(orderSearch.getOrderStatus() != null) {
//            if(isFirstCondition) {
//                jpql += "where ";
//                isFirstCondition = false;
//            }else {
//                jpql += "and ";
//            }
//            jpql += "o.status = :status ";
//        }
//
//        if(StringUtils.hasText(orderSearch.getMemberName())) {
//            if(isFirstCondition) {
//                jpql += "where ";
//                isFirstCondition = false;
//            }else {
//                jpql += "and ";
//            }
//            jpql += "m.name like :name ";
//        }
//
//        TypedQuery<Order> query = em.createQuery(jpql, Order.class)
//                .setMaxResults(100);
//
//        if (orderSearch.getOrderStatus() != null)
//            query = query.setParameter("status", orderSearch.getOrderStatus());
//        if (StringUtils.hasText(orderSearch.getMemberName())) {
//            query = query.setParameter("name", orderSearch.getMemberName());
//        }
//
//        return query.getResultList();
//
//
//        정적쿼리
//        return em.createQuery("select o from Order o join o.member m + " +
//                "where o.status = :status +" +
//                "and m.name like :name", Order.class)
//                .setParameter("status", orderSearch.getOrderStatus())
//                .setParameter("name", orderSearch.getMemberName())
//                .setMaxResults(100)
//                .getResultList();
//    }

