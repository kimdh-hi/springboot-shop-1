package jpabook.jpashop.controller;

import lombok.Data;

@Data
public class BookForm {

    private Long id;    // 수정을 위함

    private String name;

    private int price;

    private int stockQuantity;

    private String author;

    private String isbn;
}
