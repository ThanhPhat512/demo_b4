package com.hutech.demo.controller;

import com.hutech.demo.model.Product;
import com.hutech.demo.service.CartService;
import com.hutech.demo.service.CategoryService;
import com.hutech.demo.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import jakarta.validation.Valid;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@Controller
@RequestMapping("/products")
public class ProductController {
    @Autowired
    private ProductService productService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private CartService cartService;

    private static final String UPLOAD_DIR = "uploads/";

    @GetMapping
    public String showProductList(Model model) {
        List<Product> products = productService.getAllProducts();
        model.addAttribute("products", products);
        model.addAttribute("cartItems", cartService.getCartItems());
        return "/products/product-list";
    }

    @GetMapping("/add")
    public String showAddForm(Model model) {
        model.addAttribute("product", new Product());
        model.addAttribute("categories", categoryService.getAllCategories());
        return "/products/add-product";
    }

    @PostMapping("/add")
    public String addProduct(@Valid @ModelAttribute("product") Product product, BindingResult result, @RequestParam("imageFile") MultipartFile imageFile) {
        if (result.hasErrors()) {
            return "/products/add-product";
        }
        if (!imageFile.isEmpty()) {
            try {
                Path uploadPath = Paths.get(UPLOAD_DIR);
                if (!Files.exists(uploadPath)) {
                    Files.createDirectories(uploadPath);
                }
                byte[] bytes = imageFile.getBytes();
                Path path = uploadPath.resolve(imageFile.getOriginalFilename());
                Files.write(path, bytes);
                product.setImage(imageFile.getOriginalFilename());
            } catch (IOException e) {
                e.printStackTrace();
                return "/products/add-product";
            }
        }
        productService.addProduct(product);
        return "redirect:/products";
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        Product product = productService.getProductById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid product Id:" + id));
        model.addAttribute("product", product);
        model.addAttribute("categories", categoryService.getAllCategories());
        return "/products/update-product";
    }

    @PostMapping("/update/{id}")
    public String updateProduct(@PathVariable Long id, @Valid Product product, BindingResult result,
                                @RequestParam(name = "imageFile", required = false) MultipartFile imageFile) {
        if (result.hasErrors()) {
            return "/products/update-product";
        }

        Product existingProduct = productService.getProductById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid product Id:" + id));

        // Update product details
        existingProduct.setName(product.getName());
        existingProduct.setPrice(product.getPrice());
        existingProduct.setCategory(product.getCategory());
        existingProduct.setDescription(product.getDescription());

        // Process image upload if a new image is provided
        if (imageFile != null && !imageFile.isEmpty()) {
            try {
                Path uploadPath = Paths.get(UPLOAD_DIR);
                if (!Files.exists(uploadPath)) {
                    Files.createDirectories(uploadPath);
                }
                byte[] bytes = imageFile.getBytes();
                Path imagePath = uploadPath.resolve(imageFile.getOriginalFilename());
                Files.write(imagePath, bytes);
                existingProduct.setImage(imageFile.getOriginalFilename());
            } catch (IOException e) {
                e.printStackTrace();
                return "/products/update-product";
            }
        }

        productService.updateProduct(existingProduct);
        return "redirect:/products";
    }

    @GetMapping("/delete/{id}")
    public String deleteProduct(@PathVariable Long id) {
        productService.deleteProductById(id);
        return "redirect:/products";
    }

    @PostMapping("/addToCart/{productId}")
    public String addToCart(@PathVariable Long productId, @RequestParam("quantity") int quantity, RedirectAttributes redirectAttributes) {
        cartService.addToCart(productId, quantity);
        redirectAttributes.addFlashAttribute("addedToCart", true);
        return "redirect:/products";
    }
}

