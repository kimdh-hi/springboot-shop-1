package jpabook.jpashop.domain.item;

import jpabook.jpashop.domain.Category;
import jpabook.jpashop.exception.NotEnoughSotckException;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE) // 단일테이블전략
@DiscriminatorColumn(name = "dtype") // subtype을 구분하기 위함
@Getter @Setter
public abstract class Item {

    @Id
    @GeneratedValue
    @Column(name="item_id")
    private Long id;

    @ManyToMany(mappedBy = "items")
    private List<Category> categories = new ArrayList<>();

    private String name;
    private int price;
    private int stockQuantity;

    public void addStock(int quantity) {
        this.stockQuantity += quantity;
    }

    public void removeStock(int quantity) {
        int tmpQuantity = this.stockQuantity - quantity;
        if (tmpQuantity < 0){
            throw new NotEnoughSotckException("재고부족");
        }
        this.stockQuantity -= quantity;
    }
}
