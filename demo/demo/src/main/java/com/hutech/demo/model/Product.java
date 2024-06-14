package com.hutech.demo.model;

import lombok.*;
import jakarta.persistence.*;

@Setter
@Getter
@RequiredArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "products")
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private double price;
    private String description;
    private String image;


    @ManyToOne
    @JoinColumn(name = "category_id")
    private Category category;
}





