package jpabook.jpashop.controller;

import jpabook.jpashop.domain.item.Book;
import jpabook.jpashop.domain.item.Item;
import jpabook.jpashop.service.ItemService;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class ItemController {

    private final ItemService itemService;

    @GetMapping("/items/new")
    public String form(Model model) {
        model.addAttribute("form", new BookForm());
        return "items/createForm";
    }

    @PostMapping("/items/new")
    public String create(BookForm form) {
        Book book = new Book();
        book.setName(form.getName());

        book.setStockQuantity(form.getStockQuantity());
        book.setPrice(form.getPrice());
        book.setIsbn(form.getIsbn());
        book.setName(form.getName());

        itemService.saveItem(book);

        return "redirect:/items";
    }

    @GetMapping("items")
    public String list(Model model) {
        List<Item> items = itemService.findItems();
        model.addAttribute("items", items);

        return "items/itemList";
    }

    @GetMapping("/items/{id}/edit")
    public String updateForm(@PathVariable Long id, Model model) {
        Item findItem = itemService.findItem(id);
        model.addAttribute("form", findItem);

        return "items/updateForm";
    }

    @PostMapping("/items/{id}/edit")
    public String update(@PathVariable Long id, BookForm bookForm) {
        itemService.updateItem(id, bookForm);
        return "redirect:/items";
    }
}
