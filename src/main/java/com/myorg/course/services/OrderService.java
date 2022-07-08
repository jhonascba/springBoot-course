package com.myorg.course.services;

import com.myorg.course.entities.Order;
import com.myorg.course.entities.OrderItem;
import com.myorg.course.entities.Product;
import com.myorg.course.repositories.OrderItemRepository;
import com.myorg.course.repositories.OrderRepository;
import com.myorg.course.repositories.ProductRepository;
import com.myorg.course.resources.dtos.IdQuantidadePrecoDTO;
import com.myorg.course.services.exceptions.DatabaseException;
import com.myorg.course.services.exceptions.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class OrderService {

    @Autowired
    private OrderRepository repository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

    public List<Order> findAll() {
        return repository.findAll();
    }

    public Order findById(Long id) {
        Optional<Order> obj = repository.findById(id);
        return obj.orElseThrow(() -> {
            throw new ResourceNotFoundException(id);
        });
    }

    public Order insert(Order obj) {
        return repository.save(obj);
    }

    @Transactional
    public Order update(Long id, Set<IdQuantidadePrecoDTO> orderItemsDto) {
        Optional<Order> optionalOrder = repository.findById(id);
        if (optionalOrder.isPresent()) {
            Order order = optionalOrder.get();
            Set<OrderItem> itemsAntigos = order.getItems();
            orderItemRepository.deleteAll(itemsAntigos);
            order.setItems(new HashSet<>());

            List<Long> productIds = orderItemsDto.stream().map(IdQuantidadePrecoDTO::getId).collect(Collectors.toList());
            List<Product> products = productRepository.findAllById(productIds);
            Map<Long, Product> productsById = products.stream().collect(Collectors.toMap(product -> product.getId(), product -> product));
            Set<OrderItem> orderItems = orderItemsDto.stream()
                    .map(dto -> {
                        Product product = productsById.get(dto.getId());
                        return new OrderItem(order, product, dto.getQuantity(), dto.getPrice());
                    })
                    .collect(Collectors.toSet());

            order.setItems(orderItems);
            return repository.save(order);
        }
        throw new ResourceNotFoundException(id);
    }


    public void delete(Long id) {
        try {
            repository.deleteById(id);
        } catch (EmptyResultDataAccessException e) {
            throw new ResourceNotFoundException(id);
        } catch (DataIntegrityViolationException e) {
            throw new DatabaseException(e.getMessage());
        }
    }
}
