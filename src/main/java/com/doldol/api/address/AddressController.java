package com.doldol.api.address;

import com.doldol.api.security.LoginMember;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/addresses")
@RequiredArgsConstructor
public class AddressController {
    private final AddressService addressService;
    @GetMapping
    public List<AddressResponse> list(@AuthenticationPrincipal LoginMember loginMember){
        return addressService.list(loginMember.id());
    }
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AddressResponse create(@AuthenticationPrincipal LoginMember loginMember, @Valid @RequestBody AddressRequest request){
        return addressService.create(loginMember.id(), request);
    }
    @PutMapping("/{id}")
    public AddressResponse update(@AuthenticationPrincipal LoginMember loginMember, @PathVariable Long id, @Valid @RequestBody AddressRequest request){
        return addressService.update(loginMember.id(), id, request);
    }
    @PatchMapping("/{id}/default")
    public AddressResponse makeDefault(@AuthenticationPrincipal LoginMember loginMember, @PathVariable Long id){
        return addressService.makeDefault(loginMember.id(), id);
    }
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@AuthenticationPrincipal LoginMember loginMember, @PathVariable Long id){
        addressService.delete(loginMember.id(), id);
    }
}