package jpabook.jpashop.service;

import jpabook.jpashop.controller.BookForm;
import jpabook.jpashop.domain.item.Book;
import jpabook.jpashop.domain.item.Item;
import jpabook.jpashop.repository.ItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ItemService {

    private final ItemRepository itemRepository;

    @Transactional
    public void saveItem(Item item) {
        itemRepository.save(item);
    }

    public Item findItem(Long id) {
        return itemRepository.findOne(id);
    }

    public List<Item> findItems() {
        return itemRepository.findAll();
    }

    /**
     * 변경시 각 필드의 세터로 하나하나 접근하는 것은 좋은 방법이 아님.
     * 엔티티 클래스에 변경에 대한 메서드를 정의하여 변경 지점을 엔티티로 하는 것이 좋음.
     * findItem.changeItem(bookForm.getName, bookFOrm.getIsbn... );
     */
    @Transactional
    public void updateItem(Long id, BookForm bookForm) {
        Book findItem = (Book) itemRepository.findOne(id);
        findItem.setName(bookForm.getName());
        findItem.setIsbn(bookForm.getIsbn());
        findItem.setPrice(bookForm.getPrice());
        findItem.setStockQuantity(bookForm.getStockQuantity());
    }
}
