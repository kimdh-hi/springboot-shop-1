package jpabook.jpashop.domain;

import lombok.Data;
import lombok.Getter;

import javax.persistence.Embeddable;

@Embeddable
@Getter
public class Address {

    private String city;
    private String street;
    private String zipcode;

    /*
    값 타입은 변경이 불가능하게 설계하는 것이 좋다.
    따라서 세터를 두지 않고 생성자로 초기화 된 후 변경되지 않도록 한다.

    JPS 스펙상 기본생성자가 필요하기 떄문에 그나마 안전한 protected기본 생성자를 열어둔다.
     */
    protected Address() {
    }

    public Address(String city, String street, String zipcode) {
        this.city = city;
        this.street = street;
        this.zipcode = zipcode;
    }
}
