package com.doldol.api.admin.product;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/products")
@RequiredArgsConstructor
public class AdminProductController {
    private final AdminProductService adminProductService;
    @GetMapping
    public List<AdminProductResponse>list(){return adminProductService.getProducts();}
    @GetMapping("/{id}")
    public AdminProductResponse detail(@PathVariable Long id){return adminProductService.getProduct(id);}
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AdminProductResponse create(@Valid @RequestBody AdminProductRequest request){return adminProductService.create(request);}
    @PutMapping("/{id}")
    public AdminProductResponse update(@PathVariable Long id, @Valid @RequestBody AdminProductRequest request){return adminProductService.update(id, request);}
    @PatchMapping("/{id}/status")
    public AdminProductResponse changeStatus(@PathVariable Long id, @Valid @RequestBody ChangeStatusRequest request){return adminProductService.changeStatus(id, request.status());
    }
}
